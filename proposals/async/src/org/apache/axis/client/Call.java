/*
* The Apache Software License, Version 1.1
*
*
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Axis" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache",
*    nor may "Apache" appear in their name, without prior written
*    permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/

package org.apache.axis.client ;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.InternalException;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.enum.Style;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.SerializationContextImpl;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.BaseDeserializerFactory;
import org.apache.axis.encoding.ser.BaseSerializerFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.Listener;
import org.apache.axis.client.async.Mutex;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * NOTE: This is a modified version of the Axis Call object that
 * supports Async request/response operations using the proposed
 * pluggable async proc framework
 * 
 * 
 * Axis' JAXRPC Dynamic Invocation Interface implementation of the Call
 * interface.  This class should be used to actually invoke the Web Service.
 * It can be prefilled by a WSDL document (on the constructor to the Service
 * object) or you can fill in the data yourself.
 * <pre>
 * Standard properties defined by in JAX-RPC's javax..xml.rpc.Call interface:
 *     USERNAME_PROPERTY        - User name for authentication
 *     PASSWORD_PROPERTY        - Password for authentication
 *     SESSION_PROPERTY         - Participate in a session with the endpoint?
 *     OPERATION_STYLE_PROPERTY - "rpc" or "document"
 *     SOAPACTION_USE_PROPERTY  - Should SOAPAction be used?
 *     SOAPACTION_URI_PROPERTY  - If SOAPAction is used, this is that action
 *     ENCODING_STYLE_PROPERTY  - Default is SOAP 1.1:  "http://schemas.xmlsoap.org/soap/encoding/"
 *
 * AXIS properties:
 *     SEND_TYPE_ATTR - Should we send the XSI type attributes (true/false)
 *     TIMEOUT        - Timeout used by transport sender in seconds
 *     TRANSPORT_NAME - Name of transport handler to use
 *     ATTACHMENT_ENCAPSULATION_FORMAT- Send attachments as MIME the default, or DIME. 
 * </pre>
 *
 * @author Doug Davis (dug@us.ibm.com)
 */

public class Call implements javax.xml.rpc.Call {
    protected static Log log =
        LogFactory.getLog(Call.class.getName());
    private static Log tlog =
        LogFactory.getLog("org.apache.axis.TIME");

    // The enterprise category is for stuff that an enterprise product might
    // want to track, but in a simple environment (like the AXIS build) would
    // be nothing more than a nuisance.
    protected static Log entLog =
        LogFactory.getLog(Constants.ENTERPRISE_LOG_CATEGORY);

    public  static final byte ASYNC_READY = 0;
    public  static final byte ASYNC_RUNNING = 1;
    private byte current_async_status = ASYNC_READY;
    private Object returnValue;
    private AxisFault returnedFault;
    private Mutex mutex = new Mutex(); // used to control the waitForReturnValue operations
    
    private boolean            parmAndRetReq   = true ;
    private Service            service         = null ;
    private QName              portName        = null;
    private QName              operationName   = null ;
    private QName              returnType      = null ;

    private MessageContext     msgContext      = null ;

    // Collection of properties to store and put in MessageContext at
    // invoke() time.  Known ones are stored in actual variables for
    // efficiency/type-consistency.  Unknown ones are in myProperties.
    private Hashtable          callProperties  = new Hashtable();
    private Hashtable          scopedProperties= new Hashtable();
    private String             username        = null;
    private String             password        = null;
    private boolean            maintainSession = false;
    private Style              operationStyle  = Style.DEFAULT;
    private boolean            useSOAPAction   = false;
    private String             SOAPActionURI   = null;
    private Integer            timeout         = null;

    private OperationDesc      operation       = new OperationDesc();

    // Is this a one-way call?
    private boolean invokeOneWay               = false;
    private boolean isMsg                      = false;

    // Our Transport, if any
    private Transport          transport       = null ;
    private String             transportName   = null ;

    // A couple places to store output parameters.
    // As a HashMap, retrievable via QName (for getOutputParams).
    private HashMap            outParams       = null;
    // As a list, retrievable by index (for getOutputValues).
    private ArrayList          outParamsList   = null;

    // A place to store any client-specified headers
    private Vector             myHeaders       = null;

    // The desired return Java type, so we can do conversions if needed
    private Class              returnJavaType  = null;

    public static final String SEND_TYPE_ATTR    = "send_type_attr" ;
    public static final String TRANSPORT_NAME    = "transport_name" ;
    public static final String TRANSPORT_PROPERTY= "java.protocol.handler.pkgs";

    public static final String WSDL_SERVICE      = "wsdl.service";

    public static final String WSDL_PORT_NAME    = "wsdl.portName";

    // @deprecated use WSDL_SERVICE instead.
    public static final String JAXRPC_SERVICE    = WSDL_SERVICE;

    // @deprected use WSDL_PORT_NAME instead.
    public static final String JAXRPC_PORTTYPE_NAME = WSDL_PORT_NAME;

    // If true, the code will throw a fault if there is no
    // response message from the server.  Otherwise, the
    // invoke method will return a null.
    public static final boolean FAULT_ON_NO_RESPONSE = false;

    /**
     * Property for setting attachment format.
     */
    public static final String ATTACHMENT_ENCAPSULATION_FORMAT=
      "attachment_encapsulation_format";
    /**
     * Property value for setting attachment format as MIME.
     */
    public static final String ATTACHMENT_ENCAPSULATION_FORMAT_MIME=
      "axis.attachment.style.mime";
    /**
     * Property value for setting attachment format as DIME.
     */
    public static final String ATTACHMENT_ENCAPSULATION_FORMAT_DIME=
      "axis.attachment.style.dime";

    /**
     * A Hashtable mapping protocols (Strings) to Transports (classes)
     */
    private static Hashtable transports  = new Hashtable();

    /************************************************************************/
    /* Start of core JAX-RPC stuff                                          */
    /************************************************************************/

    /**
     * Default constructor - not much else to say.
     */
    public Call(Service service) {
        this.service = service ;
        msgContext = new MessageContext( service.getEngine() );
        maintainSession = service.getMaintainSession();
        initialize();
    }

    /**
     * Build a call from a URL string
     *
     * @param url the target endpoint URL
     * @exception MalformedURLException
     */
    public Call(String url) throws MalformedURLException {
        this(new Service());
        setTargetEndpointAddress(new URL(url));
    }

    /**
     * Build a call from a URL
     *
     * @param url the target endpoint URL
     */
    public Call(URL url) {
        this(new Service());
        setTargetEndpointAddress(url);
    }

    ////////////////////////////
    //
    // Properties and the shortcuts for common ones.
    //

    /**
     * Allows you to set a named property to the passed in value.
     * There are a few known properties (like username, password, etc)
     * that are variables in Call.  The rest of the properties are
     * stored in a Hashtable.  These common properties should be
     * accessed via the accessors for speed/type safety, but they may
     * still be obtained via this method.  It's up to one of the
     * Handlers (or the Axis engine itself) to go looking for
     * one of them.
     *
     * @param name  Name of the property
     * @param value Value of the property
     */
    public void setProperty(String name, Object value) {
        if (name == null || value == null) {
            throw new JAXRPCException(
                    Messages.getMessage(name == null ?
                                         "badProp03" : "badProp04"));
        } 
        else if (name.equals(AsyncCall.ASYNC_CALL_LISTENER)) {
            if (!(value instanceof Listener)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, Listener.class.getName(), value.getClass().getName()}));
            }
        }
        else if (name.equals(AsyncCall.ASYNC_CALL_PROPERTY)) {
            if (!(value instanceof Boolean)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.Boolean", value.getClass().getName()}));
            }
        }
        else if (name.equals(USERNAME_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            setUsername((String) value);
        }
        else if (name.equals(PASSWORD_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            setPassword((String) value);
        }
        else if (name.equals(SESSION_MAINTAIN_PROPERTY)) {
            if (!(value instanceof Boolean)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.Boolean",
                        value.getClass().getName()}));
            }
            setMaintainSession(((Boolean) value).booleanValue());
        }
        else if (name.equals(OPERATION_STYLE_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            setOperationStyle((String) value);
        }
        else if (name.equals(SOAPACTION_USE_PROPERTY)) {
            if (!(value instanceof Boolean)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.Boolean",
                        value.getClass().getName()}));
            }
            setUseSOAPAction(((Boolean) value).booleanValue());
        }
        else if (name.equals(SOAPACTION_URI_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.String",
                        value.getClass().getName()}));
            }
            setSOAPActionURI((String) value);
        }
        else if (name.equals(ENCODINGSTYLE_URI_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.String",
                        value.getClass().getName()}));
            }
            setEncodingStyle((String) value);
        }
        else if (name.equals(Stub.ENDPOINT_ADDRESS_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.String",
                        value.getClass().getName()}));
            }
            setTargetEndpointAddress((String) value);
        }
        else if ( name.equals(TRANSPORT_NAME) ) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            transportName = (String) value ;
            if (transport != null)
                transport.setTransportName((String) value);
        }
        else if ( name.equals(ATTACHMENT_ENCAPSULATION_FORMAT) ) {
            if (!(value instanceof String)) {
                throw new JAXRPCException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            if(!value.equals(ATTACHMENT_ENCAPSULATION_FORMAT_MIME ) && 
               !value.equals(ATTACHMENT_ENCAPSULATION_FORMAT_DIME ))
                throw new JAXRPCException(
                        Messages.getMessage("badattachmenttypeerr", new String[] {
                        (String) value, ATTACHMENT_ENCAPSULATION_FORMAT_MIME + " "
                        +ATTACHMENT_ENCAPSULATION_FORMAT_DIME  }));
        }
        else if (name.startsWith("java.") || name.startsWith("javax.")) {
            throw new JAXRPCException(
                    Messages.getMessage("badProp05", name));
        }
        callProperties.put(name, value);
    } // setProperty

    /**
     * Returns the value associated with the named property
     *
     * @return Object value of the property or null if the property is not set
     * @throws JAXRPCException if the requested property is not a supported property
     */
    public Object getProperty(String name) {
        if (name == null || !isPropertySupported(name)) {
            throw new JAXRPCException(name == null ?
                  Messages.getMessage("badProp03") :
                  Messages.getMessage("badProp05", name));
        }
        return callProperties.get(name);
    } // getProperty

    /**
      * Removes (if set) the named property.
      *
      * @param name name of the property to remove
      */
     public void removeProperty(String name) {
         if (name == null || !isPropertySupported(name)) {
            throw new JAXRPCException(name == null ?
                  Messages.getMessage("badProp03") :
                  Messages.getMessage("badProp05", name));
         }
         callProperties.remove(name);
     } // removeProperty

    public void setScopedProperty(String name, Object value) {
        if (name == null || value == null) {
            throw new JAXRPCException(
                    Messages.getMessage(name == null ?
                                         "badProp03" : "badProp04"));
        }
        scopedProperties.put(name, value);
    } // setScopedProperty

    public Object getScopedProperty(String name) {
        if (name != null) {
            return scopedProperties.get(name);
        }
        return null;
    } // getScopedProperty

    public void removeScopedProperty(String name) {
        if ( name == null || scopedProperties == null ) return ;
        scopedProperties.remove( name );
    } // removeScopedProperty

    /**
     * Configurable properties supported by this Call object.
     */
    private static ArrayList propertyNames = new ArrayList();
    static {
        propertyNames.add(USERNAME_PROPERTY);
        propertyNames.add(PASSWORD_PROPERTY);
        propertyNames.add(SESSION_MAINTAIN_PROPERTY);
        propertyNames.add(ATTACHMENT_ENCAPSULATION_FORMAT);
        propertyNames.add(OPERATION_STYLE_PROPERTY);
        propertyNames.add(SOAPACTION_USE_PROPERTY);
        propertyNames.add(SOAPACTION_URI_PROPERTY);
        propertyNames.add(ENCODINGSTYLE_URI_PROPERTY);
        propertyNames.add(TRANSPORT_NAME);
        propertyNames.add(ATTACHMENT_ENCAPSULATION_FORMAT);
        propertyNames.add(AsyncCall.ASYNC_CALL_PROPERTY);
        propertyNames.add(AsyncCall.ASYNC_CALL_LISTENER);
    }

    public Iterator getPropertyNames() {
        return propertyNames.iterator();
    }

    public boolean isPropertySupported(String name) {
        return propertyNames.contains(name) || (!name.startsWith("java.")
               && !name.startsWith("javax."));
    }

    /**
     * Set the username.
     */
    public void setUsername(String username) {
        this.username = username;
    } // setUsername

    /**
     * Get the user name
     */
    public String getUsername() {
        return username;
    } // getUsername

    /**
     * Set the password.
     */
    public void setPassword(String password) {
        this.password = password;
    } // setPassword

    /**
     * Get the password
     */
    public String getPassword() {
        return password;
    } // getPassword

    /**
     * Determine whether we'd like to track sessions or not.  This
     * overrides the default setting from the service.
     * This just passes through the value into the MessageContext.
     * Note: Not part of JAX-RPC specification.
     *
     * @param yesno true if session state is desired, false if not.
     */
    public void setMaintainSession(boolean yesno) {
        maintainSession = yesno;
    }

    /**
     * Get the value of maintainSession flag.
     */
    public boolean getMaintainSession() {
        return maintainSession;
    }

    /**
     * Set the operation style.  IllegalArgumentException is thrown if operationStyle
     * is not "rpc" or "document".
     *
     * @exception IllegalArgumentException if operationStyle is not "rpc" or "document".
     */
    public void setOperationStyle(String operationStyle) {
        this.operationStyle = Style.getStyle(operationStyle, Style.DEFAULT);

/*  Not being used for now... --GD
        throw new IllegalArgumentException(Messages.getMessage(
                "badProp01",
                new String[] {OPERATION_STYLE_PROPERTY,
                              "\"rpc\", \"document\"", operationStyle}));
*/
    } // setOperationStyle

    /**
     * Get the operation style.
     */
    public Style getOperationStyle() {
        return operationStyle;
    } // getOperationStyle

    /**
     * Should soapAction be used?
     */
    public void setUseSOAPAction(boolean useSOAPAction) {
        this.useSOAPAction = useSOAPAction;
    } // setUseSOAPAction

    /**
     * Are we using soapAction?
     */
    public boolean useSOAPAction() {
        return useSOAPAction;
    } // useSOAPAction

    /**
     * Set the soapAction URI.
     */
    public void setSOAPActionURI(String SOAPActionURI)
            throws IllegalArgumentException {
        useSOAPAction = true;
        this.SOAPActionURI = SOAPActionURI;
    } // setSOAPActionURI

    /**
     * Get the soapAction URI.
     */
    public String getSOAPActionURI() {
        return SOAPActionURI;
    } // getSOAPActionURI

    /**
     * Sets the encoding style to the URL passed in.
     *
     * @param namespaceURI URI of the encoding to use.
     */
    public void setEncodingStyle(String namespaceURI) {
        msgContext.setEncodingStyle(namespaceURI);
    }

    /**
     * Returns the encoding style as a URI that should be used for the SOAP
     * message.
     *
     * @return String URI of the encoding style to use
     */
    public String getEncodingStyle() {
        return msgContext.getEncodingStyle();
    }

    /**
     * Sets the endpoint address of the target service port. This address must
     * correspond to the transport specified in the binding for this Call
     * instance.
     *
     * @param address - Endpoint address of the target service port; specified
     *                  as URI
     */
    public void setTargetEndpointAddress(String address) {
        URL urlAddress;
        try {
            urlAddress = new URL(address);
        }
        catch (MalformedURLException mue) {
            throw new JAXRPCException(mue);
        }
        setTargetEndpointAddress(urlAddress);
    }

    /**
     * Sets the URL of the target Web Service.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param address URL of the target Web Service
     */
    public void setTargetEndpointAddress(java.net.URL address) {
        try {
            if ( address == null ) {
                setTransport(null);
                return ;
            }

            String protocol = address.getProtocol();

            // Handle the case where the protocol is the same but we
            // just want to change the URL - if so just set the URL,
            // creating a new Transport object will drop all session
            // data - and we want that stuff to persist between invoke()s.
            // Technically the session data should be in the message
            // context so that it can be persistent across transports
            // as well, but for now the data is in the Transport object.
            ////////////////////////////////////////////////////////////////
            if ( this.transport != null ) {
                String oldAddr = this.transport.getUrl();
                if ( oldAddr != null && !oldAddr.equals("") ) {
                    URL     tmpURL   = new URL( oldAddr );
                    String  oldProto = tmpURL.getProtocol();
                    if ( protocol.equals(oldProto) ) {
                        this.transport.setUrl( address.toString() );
                        return ;
                    }
                }
            }

            // Do we already have a transport for this address?
            Transport transport = service.getTransportForURL(address);
            if (transport != null) {
                setTransport(transport);
            }
            else {
            // We don't already have a transport for this address.  Create one.
                transport = getTransportForProtocol(protocol);
                if (transport == null)
                    throw new AxisFault("Call.setTargetEndpointAddress",
                                 Messages.getMessage("noTransport01",
                                 protocol), null, null);
                transport.setUrl(address.toString());
                setTransport(transport);
                service.registerTransportForURL(address, transport);
            }
        }
        catch( Exception exp ) {
            log.error(Messages.getMessage("exception00"), exp);
            // do what?
            // throw new AxisFault("Call.setTargetEndpointAddress",
            //"Malformed URL Exception: " + e.getMessage(), null, null);
        }
    }

    /**
     * Returns the URL of the target Web Service.
     *
     * @return URL URL of the target Web Service
     */
    public String getTargetEndpointAddress() {
        try {
            if ( transport == null ) return( null );
            return( transport.getUrl() );
        }
        catch( Exception exp ) {
            return( null );
        }
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    //
    // end properties code.
    //
    ////////////////////////////

    /**
     * Is the caller required to provide the parameter and return type
     * specification? 
     * If true, then
     *  addParameter and setReturnType MUST be called to provide the meta data.
     * If false, then
     *  addParameter and setReturnType SHOULD NOT be called because the 
     *  Call object already has the meta data describing the 
     *  parameters and return type. If addParameter is called, the specified 
     *  parameter is added to the end of the list of parameters.
     */
    public boolean isParameterAndReturnSpecRequired(QName operationName) {
        return parmAndRetReq;
    } // isParameterAndReturnSpecRequired

    /**
     * Adds the specified parameter to the list of parameters for the
     * operation associated with this Call object.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param paramName Name that will be used for the parameter in the XML
     * @param xmlType XMLType of the parameter
     * @param parameterMode one of IN, OUT or INOUT
     */
    public void addParameter(QName paramName, QName xmlType,
            ParameterMode parameterMode) {
        Class javaType = null;
        TypeMapping tm = getTypeMapping();
        if (tm != null) {
            javaType = tm.getClassForQName(xmlType);
        }
        addParameter(paramName, xmlType, javaType, parameterMode);
    }

    /**
     * Adds the specified parameter to the list of parameters for the
     * operation associated with this Call object.
     *
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param paramName Name that will be used for the parameter in the XML
     * @param xmlType XMLType of the parameter
     * @param javaType The Java class of the parameter
     * @param parameterMode one of IN, OUT or INOUT
     */
    public void addParameter(QName paramName, QName xmlType,
            Class javaType, ParameterMode parameterMode) {
        // In order to allow any Call to be re-used, Axis
        // chooses to allow parameters to be added when
        // parmAndRetReq==false.  This does not conflict with
        // JSR 101 which indicates an exception MAY be thrown.

        //if (parmAndRetReq) {
        ParameterDesc param = new ParameterDesc();
        param.setQName( paramName );
        param.setTypeQName( xmlType );
        param.setJavaType( javaType );
        byte mode = ParameterDesc.IN;
        if (parameterMode == ParameterMode.INOUT) {
            mode = ParameterDesc.INOUT;
        } else if (parameterMode == ParameterMode.OUT) {
            mode = ParameterDesc.OUT;
        }
        param.setMode(mode);
        
        operation.addParameter(param);
        parmAndRetReq = true;
        //}
        //else {
        //throw new JAXRPCException(Messages.getMessage("noParmAndRetReq"));
        //}
    }

    /**
     * Adds the specified parameter to the list of parameters for the
     * operation associated with this Call object.
     *
     * @param paramName      Name that will be used for the parameter in the XML
     * @param xmlType      XMLType of the parameter
     * @param parameterMode  one of IN, OUT or INOUT
     */
    public void addParameter(String paramName, QName xmlType,
            ParameterMode parameterMode) {
        Class javaType = null;
        TypeMapping tm = getTypeMapping();
        if (tm != null) {
            javaType = tm.getClassForQName(xmlType);
        }
        addParameter(new QName("", paramName), xmlType, javaType, parameterMode);
    }

    /**
     * Adds a parameter type and mode for a specific operation. Note that the
     * client code is not required to call any addParameter and setReturnType
     * methods before calling the invoke method. A Call implementation class
     * can determine the parameter types by using the Java reflection and
     * configured type mapping registry.
     *
     * @param paramName - Name of the parameter
     * @param xmlType - XML datatype of the parameter
     * @param javaType - The Java class of the parameter
     * @param parameterMode - Mode of the parameter-whether IN, OUT or INOUT
     * @exception JAXRPCException - if isParameterAndReturnSpecRequired returns
     *                              false, then addParameter MAY throw
     *                              JAXRPCException....actually Axis allows
     *                              modification in such cases
     */
    public void addParameter(String paramName, QName xmlType,
                             Class javaType, ParameterMode parameterMode) {
        addParameter(new QName("", paramName), xmlType, javaType, parameterMode);
    }

    /**
     * Return the QName of the type of the parameters with the given name.
     *
     * @param  paramName  name of the parameter to return
     * @return XMLType    XMLType of paramName, or null if not found.
     */
    public QName getParameterTypeByName(String paramName) {
        QName paramQName = new QName("", paramName);

        return getParameterTypeByQName(paramQName);
    }

    /**
     * Return the QName of the type of the parameters with the given name.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param  paramQName  QName of the parameter to return
     * @return XMLType    XMLType of paramQName, or null if not found.
     */
    public QName getParameterTypeByQName(QName paramQName) {
        ParameterDesc param = operation.getParamByQName(paramQName);
        if (param != null) {
            return param.getTypeQName();
        }
        return( null );
    }

    /**
     * Sets the return type of the operation associated with this Call object.
     *
     * @param type QName of the return value type.
     */
    public void setReturnType(QName type) {
        // In order to allow any Call to be re-used, Axis
        // chooses to allow setReturnType to be changed when
        // parmAndRetReq==false.  This does not conflict with
        // JSR 101 which indicates an exception MAY be thrown.

        //if (parmAndRetReq) {
        returnType = type ;
        operation.setReturnType(type);
        TypeMapping tm = getTypeMapping();
        operation.setReturnClass(tm.getClassForQName(type));
        parmAndRetReq = true;
        //}
        //else {
        //throw new JAXRPCException(Messages.getMessage("noParmAndRetReq"));
        //}
    }

    /**
     * Sets the return type for a specific operation.
     *
     * @param xmlType - QName of the data type of the return value
     * @param javaType - Java class of the return value
     * @exception JAXRPCException - if isParameterAndReturnSpecRequired returns
     * false, then setReturnType MAY throw JAXRPCException...Axis allows 
     * modification without throwing the exception.
     */
    public void setReturnType(QName xmlType, Class javaType) {
        setReturnType(xmlType);
        returnJavaType = javaType;
        operation.setReturnClass(javaType);  // Use specified type as the operation return
    }

    /**
     * Returns the QName of the type of the return value of this Call - or null if
     * not set.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @return the XMLType specified for this Call (or null).
     */
    public QName getReturnType() {
        return( returnType );
    }

    /**
     * Sets the desired return Java Class.  This is a convenience method
     * which will cause the Call to automatically convert return values
     * into a desired class if possible.  For instance, we return object
     * arrays by default now for SOAP arrays - you could specify:
     *
     * setReturnClass(Vector.class)
     *
     * and you'd get a Vector back from invoke() instead of having to do
     * the conversion yourself.
     *
     * Note: Not part of JAX-RPC specification.  To be JAX-RPC compliant,
     *       use setReturnType(QName, Class).
     *
     * @param cls the desired return class.
     */
    public void setReturnClass(Class cls) {
        returnJavaType = cls;
    }

    /**
     * Clears the list of parameters.
     * @exception JAXRPCException - if isParameterAndReturnSpecRequired returns false, then
     * removeAllParameters MAY throw JAXRPCException...Axis allows modification to
     * the Call object without throwing an exception.
     */
    public void removeAllParameters() {
        //if (parmAndRetReq) {
        operation = new OperationDesc();
        parmAndRetReq = true;
        //}
        //else {
        //throw new JAXRPCException(Messages.getMessage("noParmAndRetReq"));
        //}
    }

    /**
     * Returns the operation name associated with this Call object.
     *
     * @return String Name of the operation or null if not set.
     */
    public QName getOperationName() {
        return( operationName );
    }

    /**
     * Sets the operation name associated with this Call object.  This will
     * not check the WSDL (if there is WSDL) to make sure that it's a valid
     * operation name.
     *
     * @param opName Name of the operation.
     */
    public void setOperationName(QName opName) {
        operationName = opName ;
    }

    /**
     * This is a convenience method.  If the user doesn't care about the QName of the operation, the
     * user can call this method, which converts a String operation name to a QName.
     */
    public void setOperationName(String opName) {
        operationName = new QName(opName);
    }

    public void setOperation(QName portName, String opName) {
        if ( service == null )
            throw new JAXRPCException( Messages.getMessage("noService04") );

        // Make sure we're making a fresh start.
        this.setPortName( portName );
        this.setOperationName( opName );
        this.setTargetEndpointAddress( (URL) null );
        this.setEncodingStyle( null );
        this.setReturnType( null );
        this.removeAllParameters();

        javax.wsdl.Service wsdlService = service.getWSDLService();
        // Nothing to do is the WSDL is not already set.
        if(wsdlService == null)
            return;

        Port port = wsdlService.getPort( portName.getLocalPart() );
        if ( port == null )
            throw new JAXRPCException( Messages.getMessage("noPort00", "" + portName) );

        Binding   binding  = port.getBinding();
        PortType  portType = binding.getPortType();
        if ( portType == null )
            throw new JAXRPCException( Messages.getMessage("noPortType00", "" + portName) );

        List operations = portType.getOperations();
        if ( operations == null )
            throw new JAXRPCException( Messages.getMessage("noOperation01", opName) );

        Operation op = null ;
        for ( int i = 0 ; i < operations.size() ; i++, op=null ) {
            op = (Operation) operations.get( i );
            if ( opName.equals( op.getName() ) ) break ;
        }
        if ( op == null )
            throw new JAXRPCException( Messages.getMessage("noOperation01", opName) );

        // Get the URL
        ////////////////////////////////////////////////////////////////////
        List list = port.getExtensibilityElements();
        for ( int i = 0 ; list != null && i < list.size() ; i++ ) {
            Object obj = list.get(i);
            if ( obj instanceof SOAPAddress ) {
                try {
                    SOAPAddress addr = (SOAPAddress) obj ;
                    URL         url  = new URL(addr.getLocationURI());
                    this.setTargetEndpointAddress(url);
                }
                catch(Exception exp) {
                    throw new JAXRPCException(
                            Messages.getMessage("cantSetURI00", "" + exp) );
                }
            }
        }

        // Get the SOAPAction
        ////////////////////////////////////////////////////////////////////
        BindingOperation bop = binding.getBindingOperation(opName,
                                                           null, null);
        if ( bop == null )
            throw new JAXRPCException( Messages.getMessage("noOperation02",
                                                            opName ));
        list = bop.getExtensibilityElements();
        for ( int i = 0 ; list != null && i < list.size() ; i++ ) {
            Object obj = list.get(i);
            if ( obj instanceof SOAPOperation ) {
                SOAPOperation sop    = (SOAPOperation) obj ;
                String        action = sop.getSoapActionURI();
                if ( action != null ) {
                    setUseSOAPAction(true);
                    setSOAPActionURI(action);
                }
                else {
                    setUseSOAPAction(false);
                    setSOAPActionURI(null);
                }
                break ;
            }
        }

        // Get the body's namespace URI and encoding style
        ////////////////////////////////////////////////////////////////////
        BindingInput bIn = bop.getBindingInput();
        if ( bIn != null ) {
            list = bIn.getExtensibilityElements();
            for ( int i = 0 ; list != null && i < list.size() ; i++ ) {
                Object obj = list.get(i);
                if( obj instanceof javax.wsdl.extensions.mime.MIMEMultipartRelated){
                  javax.wsdl.extensions.mime.MIMEMultipartRelated mpr=
                  (javax.wsdl.extensions.mime.MIMEMultipartRelated) obj;
                  Object part= null;
                  List l=  mpr.getMIMEParts();
                  for(int j=0; l!= null && j< l.size() && part== null; j++){
                     javax.wsdl.extensions.mime.MIMEPart mp
                     = (javax.wsdl.extensions.mime.MIMEPart)l.get(j);
                     List ll= mp.getExtensibilityElements();
                     for(int k=0; ll!= null && k< ll.size() && part== null; k++){
                       part= ll.get(k);
                       if ( !(part instanceof SOAPBody)) part = null;
                     }
                  }
                  if(null != part) obj= part;
                }

                if ( obj instanceof SOAPBody ) {
                    SOAPBody sBody  = (SOAPBody) obj ;
                    list = sBody.getEncodingStyles();
                    if ( list != null && list.size() > 0 )
                        this.setEncodingStyle( (String) list.get(0) );
                    String ns = sBody.getNamespaceURI();
                    if (ns != null && !ns.equals(""))
                      setOperationName( new QName( ns, opName ) );
                    break ;
                }
            }
        }

        // Get the parameters
        ////////////////////////////////////////////////////////////////////
        List    paramOrder = op.getParameterOrdering();
        Input   input      = op.getInput();
        javax.wsdl.Message message    = null ;
        List    parts      = null ;

        if ( input   != null ) message = input.getMessage();
        if ( message != null ) parts   = message.getOrderedParts( paramOrder );
        if ( parts != null ) {
            for ( int i = 0 ; i < parts.size() ; i++ ) {
                Part    part = (Part) parts.get(i);
                if ( part == null ) continue ;

                String name = part.getName();
                QName  type = part.getTypeName();

                if ( type == null ) {
                    type = part.getElementName();
                    if ( type != null )
                      type = new QName("java","org.w3c.dom.Element");
                    else
                      throw new JAXRPCException(
                                  Messages.getMessage("typeNotSet00", name) );
                }

                QName qname = new QName(type.getNamespaceURI(),
                        type.getLocalPart());
                ParameterMode mode = ParameterMode.IN;
                this.addParameter( name, qname, mode );
            }
        }


        // Get the return type
        ////////////////////////////////////////////////////////////////////
        Output   output  = op.getOutput();
        message = null ;

        if ( output  != null ) message = output.getMessage();
        if ( message != null ) parts   = message.getOrderedParts(null);

//      if (null != paramTypes) // attachments may have no parameters.
        if (operation != null && operation.getNumParams() > 0) // attachments may have no parameters.
          this.setReturnType( XMLType.AXIS_VOID );
        if ( parts != null ) {
            for( int i = 0 ;i < parts.size() ; i++ ) {
                Part part  = (Part) parts.get( i );

                if (paramOrder != null && paramOrder.contains(part.getName()))
                        continue ;

                QName type  = part.getTypeName();
                if ( type == null ) {
                    type = part.getElementName();
                    if ( type != null )
                      type = new QName("java","org.w3c.dom.Element");
                    else
                      throw new JAXRPCException(
                            Messages.getMessage("typeNotSet00", "<return>") );
                }
                QName qname = new QName(type.getNamespaceURI(),
                        type.getLocalPart());
                this.setReturnType( qname );
                break ;
            }
        }

        // Indicate that the parameters and return no longer
        // need to be specified with addParameter calls.
        parmAndRetReq = false;
        return;
    }

    /**
     * Returns the fully qualified name of the port for this Call object
     * (if there is one).
     *
     * @return QName Fully qualified name of the port (or null if not set)
     */
    public QName getPortName() {
        return( portName );
    } // getPortName

    /**
     * Sets the port name of this Call object.  This call will not set
     * any additional fields, nor will it do any checking to verify that
     * this port name is actually defined in the WSDL - for now anyway.
     *
     * @param portName Fully qualified name of the port
     */
    public void setPortName(QName portName) {
        this.portName = portName;
    } // setPortName

    /**
     * Returns the fully qualified name of the port for this Call object
     * (if there is one).
     *
     * @return QName Fully qualified name of the port
     *
     * @deprecated This is really the service's port name, not portType name.
     *            Use getPortName instead.
     */
    public QName getPortTypeName() {
        return portName == null ? new QName("") : portName;
    }

    /**
     * Sets the port name of this Call object.  This call will not set
     * any additional fields, nor will it do any checking to verify that
     * this port type is actually defined in the WSDL - for now anyway.
     *
     * @param portType Fully qualified name of the portType
     *
     * @deprecated This is really the service's port name, not portType name.
     *            Use setPortName instead.
     */
    public void setPortTypeName(QName portType) {
        setPortName(portType);
    }

    /**
     * Allow the user to set the default SOAP version.  For SOAP 1.2, pass
     * SOAPConstants.SOAP12_CONSTANTS.
     *
     * @param soapConstants the SOAPConstants object representing the correct
     *                      version
     */
    public void setSOAPVersion(SOAPConstants soapConstants) {
        msgContext.setSOAPConstants(soapConstants);
    }

    /**
     * Invokes a specific operation using a synchronous request-response interaction mode. The invoke method takes
     * as parameters the object values corresponding to these defined parameter types. Implementation of the invoke
     * method must check whether the passed parameter values correspond to the number, order and types of parameters
     * specified in the corresponding operation specification.
     *
     * @param operationName - Name of the operation to invoke
     * @param params  - Parameters for this invocation
     *
     * @return the value returned from the other end.
     *
     * @throws java.rmi.RemoteException - if there is any error in the remote method invocation or if the Call
     * object is not configured properly.
     */
    public Object invoke(QName operationName, Object[] params)
      throws java.rmi.RemoteException {
        QName origOpName = this.operationName;
        this.operationName = operationName;
        try {
            return this.invoke(params);
        }
        catch (java.rmi.RemoteException re) {
            this.operationName = origOpName;
            throw re;
        }
        catch (RuntimeException re) {
            this.operationName = origOpName;
            throw re;
        }
        catch (Error e) {
            this.operationName = origOpName;
            throw e;
        }
    } // invoke

    /**
     * Invokes the operation associated with this Call object using the
     * passed in parameters as the arguments to the method.
     *
     * For Messaging (ie. non-RPC) the params argument should be an array
     * of SOAPBodyElements.  <b>All</b> of them need to be SOAPBodyElements,
     * if any of them are not this method will default back to RPC.  In the
     * Messaging case the return value will be a vector of SOAPBodyElements.
     *
     * @param  params Array of parameters to invoke the Web Service with
     * @return Object Return value of the operation/method - or null
     * @throws java.rmi.RemoteException if there's an error
     */
    public Object invoke(Object[] params) throws java.rmi.RemoteException {
        long t0=0, t1=0;
        if( tlog.isDebugEnabled() ) {
            t0=System.currentTimeMillis();
        }
        /* First see if we're dealing with Messaging instead of RPC.        */
        /* If ALL of the params are SOAPBodyElements then we're doing       */
        /* Messaging, otherwise just fall through to normal RPC processing. */
        /********************************************************************/
        SOAPEnvelope  env = null ;
        int i ;

        for ( i = 0 ; params != null && i < params.length ; i++ )
            if ( !(params[i] instanceof SOAPBodyElement) ) break ;

        if ( params != null && params.length > 0 && i == params.length ) {
            /* ok, we're doing Messaging, so build up the message */
            /******************************************************/
            isMsg = true ;
            env = new SOAPEnvelope(msgContext.getSOAPConstants());

            if ( !(params[0] instanceof SOAPEnvelope) )
                for ( i = 0 ; i < params.length ; i++ )
                    env.addBodyElement( (SOAPBodyElement) params[i] );

            Message msg = new Message( env );
            setRequestMessage(msg);

            invoke();
            if (getAsyncStatus() != ASYNC_READY) return null;
            msg = msgContext.getResponseMessage();
            if (msg == null) {
              if (FAULT_ON_NO_RESPONSE) {
                throw new AxisFault(Messages.getMessage("nullResponse00"));
              } else {
                return null;
              }
            }
            env = msg.getSOAPEnvelope();
            return( env.getBodyElements() );
        }


        if ( operationName == null )
            throw new AxisFault( Messages.getMessage("noOperation00") );
        try {
            Object res=this.invoke(operationName.getNamespaceURI(),
                    operationName.getLocalPart(), params);
            if (getAsyncStatus() != ASYNC_READY) return null;
            if( tlog.isDebugEnabled() ) {
                t1=System.currentTimeMillis();
                tlog.debug("axis.Call.invoke: " + (t1-t0)  + " " + operationName);
            }
            return res;
        }
        catch( AxisFault af) {
            throw af;
        }
        catch( Exception exp ) {
            //if ( exp instanceof AxisFault ) throw (AxisFault) exp ;
            entLog.info(Messages.getMessage("toAxisFault00"), exp);
            throw new AxisFault( Messages.getMessage("errorInvoking00", "\n" + exp) );
        }
    }

    /**
     * Invokes the operation associated with this Call object using the passed
     * in parameters as the arguments to the method.  This will return
     * immediately rather than waiting for the server to complete its
     * processing.
     *
     * NOTE: the return immediately part isn't implemented yet
     *
     * @param  params Array of parameters to invoke the Web Service with
     * @throws JAXRPCException is there's an error
     */
    public void invokeOneWay(Object[] params) {
        try {
            invokeOneWay = true;
            invoke( params );
        } catch( Exception exp ) {
            throw new JAXRPCException( exp.toString() );
        } finally {
            invokeOneWay = false;
        }
    }

    /************************************************************************/
    /* End of core JAX-RPC stuff                                            */
    /************************************************************************/

    /** Invoke the service with a custom SOAPEnvelope.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param env a SOAPEnvelope to send.
     * @exception AxisFault
     */
    public SOAPEnvelope invoke(SOAPEnvelope env)
                                  throws java.rmi.RemoteException {
        try {
            Message msg = null ;

            msg = new Message( env );
            setRequestMessage( msg );
            invoke();
            if (getAsyncStatus() != ASYNC_READY) return null;
            msg = msgContext.getResponseMessage();
            if (msg == null) {
              if (this.FAULT_ON_NO_RESPONSE) {
                throw new AxisFault(Messages.getMessage("nullResponse00"));
              } else {
                return null;
              }
            }
            return( msg.getSOAPEnvelope() );
        }
        catch( Exception exp ) {
            if ( exp instanceof AxisFault ) throw (AxisFault) exp ;

            entLog.info(Messages.getMessage("toAxisFault00"), exp);
            throw new AxisFault( Messages.getMessage("errorInvoking00", "\n" + exp) );
        }
    }


    /** Register a Transport that should be used for URLs of the specified
     * protocol.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param protocol the URL protocol (i.e. "tcp" for "tcp://" urls)
     * @param transportClass the class of a Transport type which will be used
     *                       for matching URLs.
     */
    public static void setTransportForProtocol(String protocol,
                                               Class transportClass) {
        if (Transport.class.isAssignableFrom(transportClass))
            transports.put(protocol, transportClass);
        else
            throw new InternalException(transportClass.toString());
    }

    /**
     * Set up the default transport URL mappings.
     *
     * This must be called BEFORE doing non-standard URL parsing (i.e. if you
     * want the system to accept a "local:" URL).  This is why the Options class
     * calls it before parsing the command-line URL argument.
     *
     * Note: Not part of JAX-RPC specification.
     */
    public static synchronized void initialize() {
        addTransportPackage("org.apache.axis.transport");

        setTransportForProtocol("java",
                org.apache.axis.transport.java.JavaTransport.class);
        setTransportForProtocol("local",
                org.apache.axis.transport.local.LocalTransport.class);
        setTransportForProtocol("http", HTTPTransport.class);
        setTransportForProtocol("https", HTTPTransport.class);
    }

    /**
     * Cache of transport packages we've already added to the system
     * property.
     */
    private static ArrayList transportPackages = null;

    /** Add a package to the system protocol handler search path.  This
     * enables users to create their own URLStreamHandler classes, and thus
     * allow custom protocols to be used in Axis (typically on the client
     * command line).
     *
     * For instance, if you add "samples.transport" to the packages property,
     * and have a class samples.transport.tcp.Handler, the system will be able
     * to parse URLs of the form "tcp://host:port..."
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param packageName the package in which to search for protocol names.
     */
    public static synchronized void addTransportPackage(String packageName) {
        if (transportPackages == null) {
            transportPackages = new ArrayList();
            String currentPackages = AxisProperties.getProperty(TRANSPORT_PROPERTY);
            if (currentPackages != null) {
                StringTokenizer tok = new StringTokenizer(currentPackages,
                                                          "|");
                while (tok.hasMoreTokens()) {
                    transportPackages.add(tok.nextToken());
                }
            }
        }

        if (transportPackages.contains(packageName))
            return;

        transportPackages.add(packageName);
        
        StringBuffer currentPackages = new StringBuffer();
        for (Iterator i = transportPackages.iterator(); i.hasNext();) {
            String thisPackage = (String) i.next();
            currentPackages.append(thisPackage);
            currentPackages.append('|');
        }

        System.setProperty(TRANSPORT_PROPERTY, currentPackages.toString());
    }

    /**
     * Convert the list of objects into RPCParam's based on the paramNames,
     * paramXMLTypes and paramModes variables.  If those aren't set then just
     * return what was passed in.
     *
     * @param  params   Array of parameters to pass into the operation/method
     * @return Object[] Array of parameters to pass to invoke()
     */
    private Object[] getParamList(Object[] params) {
        int  numParams = 0 ;
        int  i ;

        // If we never set-up any names... then just return what was passed in
        //////////////////////////////////////////////////////////////////////
        log.debug( "operation=" + operation);
        if(operation != null) log.debug("operation.getNumParams()=" +operation.getNumParams());
        if ( operation.getNumParams() == 0 ) return( params );

        // Count the number of IN and INOUT params, this needs to match the
        // number of params passed in - if not throw an error
        /////////////////////////////////////////////////////////////////////
        numParams = operation.getNumInParams();

        if ( params == null || numParams != params.length )
            throw new JAXRPCException(
                    Messages.getMessage("parmMismatch00",
                    "" + params.length, "" + numParams) );

        log.debug( "getParamList number of params: " + params.length);

        // All ok - so now produce an array of RPCParams
        //////////////////////////////////////////////////
        Vector result = new Vector();
        int    j = 0 ;
        ArrayList parameters = operation.getParameters();

        for ( i = 0 ; i < parameters.size() ; i++ ) {
            ParameterDesc param = (ParameterDesc)parameters.get(i);
            if (param.getMode() == ParameterDesc.OUT)
                continue ;

            QName paramQName = param.getQName();
            RPCParam rpcParam = null;
            Object p = params[j++];
            if(p instanceof RPCParam) {
                rpcParam = (RPCParam)p;
            } else {
                rpcParam = new RPCParam(paramQName.getNamespaceURI(),
                                      paramQName.getLocalPart(),
                                      p );
            }
            // Attach the ParameterDescription to the RPCParam
            // so that the serializer can use the (javaType, xmlType)
            // information.
            rpcParam.setParamDesc(param);
            result.add( rpcParam );
        }

        return( result.toArray() );
    }

    /**
     * Set the Transport
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param trans the Transport object we'll use to set up
     *                  MessageContext properties.
     */
    public void setTransport(Transport trans) {
        transport = trans;
        if (log.isDebugEnabled())
            log.debug(Messages.getMessage("transport00", "" + transport));
    }

    /** Get the Transport registered for the given protocol.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param protocol a protocol such as "http" or "local" which may
     *                 have a Transport object associated with it.
     * @return the Transport registered for this protocol, or null if none.
     */
    public Transport getTransportForProtocol(String protocol)
    {
        Class transportClass = (Class)transports.get(protocol);
        Transport ret = null;
        if (transportClass != null) {
            try {
                ret = (Transport)transportClass.newInstance();
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return ret;
    }

    /**
     * Directly set the request message in our MessageContext.
     *
     * This allows custom message creation.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param msg the new request message.
     */
    public void setRequestMessage(Message msg) {
         String attachformat= (String)getProperty(
           ATTACHMENT_ENCAPSULATION_FORMAT);

         if(null != attachformat){
              org.apache.axis.attachments.Attachments attachments=
                msg.getAttachmentsImpl();
              if(null != attachments) {
                  if( null != attachformat && attachformat.equals(
                    ATTACHMENT_ENCAPSULATION_FORMAT_MIME)) 
                    attachments.setSendType(attachments.SEND_TYPE_MIME);
                  else if( null != attachformat && attachformat.equals(
                      ATTACHMENT_ENCAPSULATION_FORMAT_DIME)) {
                    attachments.setSendType(attachments.SEND_TYPE_DIME);
                  }
              }
         }

        if(null != attachmentParts && !attachmentParts.isEmpty()){
            try{
            org.apache.axis.attachments.Attachments attachments= msg.getAttachmentsImpl();
            if(null == attachments) {
              throw new RuntimeException(
                      Messages.getMessage("noAttachments"));
            }

            attachments.setAttachmentParts(attachmentParts);
            }catch(org.apache.axis.AxisFault ex){
              log.info(Messages.getMessage("axisFault00"), ex);
              throw  new RuntimeException(ex.getMessage());
            }
        }

        msgContext.setRequestMessage(msg);
        attachmentParts.clear();
    }

    /**
     * Directly get the response message in our MessageContext.
     *
     * Shortcut for having to go thru the msgContext
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @return the response Message object in the msgContext
     */
    public Message getResponseMessage() {
        return msgContext.getResponseMessage();
    }

    /**
     * Obtain a reference to our MessageContext.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @return the MessageContext.
     */
    public MessageContext getMessageContext () {
        return msgContext;
    }

    /**
     * Add a header which should be inserted into each outgoing message
     * we generate.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param header a SOAPHeaderElement to be inserted into messages
     */
    public void addHeader(SOAPHeaderElement header)
    {
        if (myHeaders == null) {
            myHeaders = new Vector();
        }
        myHeaders.add(header);
    }

    /**
     * Clear the list of headers which we insert into each message
     *
     * Note: Not part of JAX-RPC specification.
     */
    public void clearHeaders()
    {
        myHeaders = null;
    }

    public TypeMapping getTypeMapping()
    {
        // Get the TypeMappingRegistry
        TypeMappingRegistry tmr = msgContext.getTypeMappingRegistry();

        // If a TypeMapping is not available, add one.
        TypeMapping tm = (TypeMapping) tmr.getTypeMapping(getEncodingStyle());
        TypeMapping defaultTM = (TypeMapping) tmr.getDefaultTypeMapping();
        if (tm == null || tm == defaultTM ) {
            tm = (TypeMapping) tmr.createTypeMapping();
            tm.setSupportedEncodings(new String[] {getEncodingStyle()});
            tmr.register(getEncodingStyle(), tm);
        }
        return tm;
    }

    /**
     * Register type mapping information for serialization/deserialization
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param javaType is  the Java class of the data type.
     * @param xmlType the xsi:type QName of the associated XML type.
     * @param sf/df are the factories (or the Class objects of the factory).
     */
    public void registerTypeMapping(Class javaType, QName xmlType,
                                    SerializerFactory sf,
                                    DeserializerFactory df) {
        registerTypeMapping(javaType, xmlType, sf, df, true);
    }

    /**
     * Register type mapping information for serialization/deserialization
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param javaType is  the Java class of the data type.
     * @param xmlType the xsi:type QName of the associated XML type.
     * @param sf/df are the factories (or the Class objects of the factory).
     * @param force Indicates whether to add the information if already registered.
     */
    public void registerTypeMapping(Class javaType, QName xmlType,
                                    SerializerFactory sf,
                                    DeserializerFactory df,
                                    boolean force) {
        TypeMapping tm = getTypeMapping();
        if (!force && tm.isRegistered(javaType, xmlType))
            return;

        // Register the information
        tm.register(javaType, xmlType, sf, df);
    }

    public void registerTypeMapping(Class javaType, QName xmlType,
                                    Class sfClass, Class dfClass) {
        registerTypeMapping(javaType, xmlType, sfClass, dfClass, true);
    }
    public void registerTypeMapping(Class javaType, QName xmlType,
                                    Class sfClass, Class dfClass, boolean force){
        // Instantiate the factory using introspection.
        SerializerFactory   sf = BaseSerializerFactory.createFactory  (sfClass, javaType, xmlType);
        DeserializerFactory df = BaseDeserializerFactory.createFactory(dfClass, javaType, xmlType);
        if (sf != null || df != null) {
            registerTypeMapping(javaType, xmlType, sf, df, force);
        }
    }

    /************************************************
     * Invocation
     */

    /** Invoke an RPC service with a method name and arguments.
     *
     * This will call the service, serializing all the arguments, and
     * then deserialize the return value.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param namespace the desired namespace URI of the method element
     * @param method the method name
     * @param args an array of Objects representing the arguments to the
     *             invoked method.  If any of these objects are RPCParams,
     *             Axis will use the embedded name of the RPCParam as the
     *             name of the parameter.  Otherwise, we will serialize
     *             each argument as an XML element called "arg<n>".
     * @return a deserialized Java Object containing the return value
     * @exception AxisFault
     */
    public Object invoke(String namespace, String method, Object[] args)
                    throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: Call::invoke(ns, meth, args)");
        }

        /**
         * Since JAX-RPC requires us to specify all or nothing, if setReturnType
         * was called (returnType != null) and we have args but addParameter
         * wasn't called (paramXMLTypes == null), then toss a fault.
         */
        if (returnType != null && args != null && args.length != 0
                && operation == null) {
            throw new AxisFault(Messages.getMessage("mustSpecifyParms"));
        }

        RPCElement  body = new RPCElement(namespace, method, getParamList(args));

        Object ret = invoke( body );

        if (log.isDebugEnabled()) {
            log.debug("Exit: Call::invoke(ns, meth, args)");
        }

        return ret;
    }

    /** Convenience method to invoke a method with a default (empty)
     * namespace.  Calls invoke() above.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param method the method name
     * @param args an array of Objects representing the arguments to the
     *             invoked method.  If any of these objects are RPCParams,
     *             Axis will use the embedded name of the RPCParam as the
     *             name of the parameter.  Otherwise, we will serialize
     *             each argument as an XML element called "arg<n>".
     * @return a deserialized Java Object containing the return value
     * @exception AxisFault
     */
    public Object invoke( String method, Object [] args ) throws AxisFault
    {
        return invoke("", method, args);
    }

    /** Invoke an RPC service with a pre-constructed RPCElement.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @param body an RPCElement containing all the information about
     *             this call.
     * @return a deserialized Java Object containing the return value
     * @exception AxisFault
     */
    public Object invoke( RPCElement body ) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: Call::invoke(RPCElement)");
        }

        /**
         * Since JAX-RPC requires us to specify a return type if we've set
         * parameter types, check for this case right now and toss a fault
         * if things don't look right.
         */
        if (!invokeOneWay && operation.getNumParams() > 0 && returnType == null) {
            // TCK:
            // Issue an error if the return type was not set, but continue processing.
            //throw new AxisFault(Messages.getMessage("mustSpecifyReturnType"));
            log.error(Messages.getMessage("mustSpecifyReturnType"));
        }

        SOAPEnvelope         reqEnv = new SOAPEnvelope(msgContext.getSOAPConstants());
        SOAPEnvelope         resEnv = null ;
        Message              reqMsg = new Message( reqEnv );
        Message              resMsg = null ;
        Vector               resArgs = null ;
        Object               result = null ;

        // Clear the output params
        outParams = new HashMap();
        outParamsList = new ArrayList();

        // Set both the envelope and the RPCElement encoding styles
        try {
            body.setEncodingStyle(getEncodingStyle());

            setRequestMessage(reqMsg);

            reqEnv.addBodyElement(body);
            reqEnv.setMessageType(Message.REQUEST);

            invoke();
        } catch (Exception e) {
            entLog.info(Messages.getMessage("toAxisFault00"), e);
            throw AxisFault.makeFault(e);
        }

        if (getAsyncStatus() != ASYNC_READY) return null;
        
        resMsg = msgContext.getResponseMessage();

        if (resMsg == null) {
          if (FAULT_ON_NO_RESPONSE) {
            throw new AxisFault(Messages.getMessage("nullResponse00"));
          } else {
            return null;
          }
        }

        resEnv = (SOAPEnvelope)resMsg.getSOAPEnvelope();
        SOAPBodyElement bodyEl = resEnv.getFirstBody();
        if (bodyEl instanceof RPCElement) {
            try {
                resArgs = ((RPCElement) bodyEl).getParams();
            } catch (Exception e) {
                log.error(Messages.getMessage("exception00"), e);
                throw AxisFault.makeFault(e);
            }

            if (resArgs != null && resArgs.size() > 0) {

                // If there is no return, then we start at index 0 to create the outParams Map.
                // If there IS a return, then we start with 1.
                int outParamStart = 0;

                // If we have resArgs and the returnType is specified, then the first
                // resArgs is the return.  If we have resArgs and neither returnType
                // nor paramXMLTypes are specified, then we assume that the caller is
                // following the non-JAX-RPC AXIS shortcut of not having to specify
                // the return, in which case we again assume the first resArgs is
                // the return.
                // NOTE 1:  the non-JAX-RPC AXIS shortcut allows a potential error
                // to escape notice.  If the caller IS NOT following the non-JAX-RPC
                // shortcut but instead intentionally leaves returnType and params
                // null (ie., a method that takes no parameters and returns nothing)
                // then, if we DO receive something it should be an error, but this
                // code passes it through.  The ideal solution here is to require
                // this caller to set the returnType to void, but there's no void
                // type in XML.
                // NOTE 2:  we should probably verify that the resArgs element
                // types match the expected returnType and paramXMLTypes, but I'm not
                // sure how to do that since the resArgs value is a Java Object
                // and the returnType and paramXMLTypes are QNames.

                // GD 03/15/02 : We're now checking for invalid metadata
                // config at the top of this method, so don't need to do it
                // here.  Check for void return, though.
                if (!XMLType.AXIS_VOID.equals(returnType)) {
                    RPCParam param = (RPCParam)resArgs.get(0);
                    result = param.getValue();
                    outParamStart = 1;
                }

                for (int i = outParamStart; i < resArgs.size(); i++) {
                    RPCParam param = (RPCParam) resArgs.get(i);

                    Class javaType = getJavaTypeForQName(param.getQName());
                    Object value = param.getValue();

                    // Convert type if needed
                    if (javaType != null &&
                           !javaType.isAssignableFrom(value.getClass())) {
                        value = JavaUtils.convert(value, javaType);
                    }

                    outParams.put(param.getQName(), value);
                    outParamsList.add(value);
                }
            }
        } else {
            // This is a SOAPBodyElement, try to treat it like a return value
            try {
                result = bodyEl.getValueAsType(returnType);
            } catch (Exception e) {
                // just return the SOAPElement
                result = bodyEl;
            }

        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: Call::invoke(RPCElement)");
        }

        // Convert type if needed
        if (returnJavaType != null) {
            result = JavaUtils.convert(result, returnJavaType);
        }

        return( result );
    }

    /**
     * Get the javaType for a given parameter.
     *
     */
    private Class getJavaTypeForQName(QName name) {
        ParameterDesc param = operation.getOutputParamByQName(name);
        return param == null ? null : param.getJavaType();
    }

    /**
     * Set engine option.
     *
     * Note: Not part of JAX-RPC specification.
     */
    public void setOption(String name, Object value) {
        service.getEngine().setOption(name, value);
    }

    /**
     * Invoke this Call with its established MessageContext
     * (perhaps because you called this.setRequestMessage())
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @exception AxisFault
     */
    public void invoke() throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: Call::invoke()");
        }

        Message      reqMsg  = null ;
        SOAPEnvelope reqEnv  = null ;

        msgContext.reset();
        msgContext.setResponseMessage(null);
        msgContext.setProperty( MessageContext.CALL, this );
        msgContext.setProperty( WSDL_SERVICE, service );
        msgContext.setProperty( WSDL_PORT_NAME, getPortName() );
        if ( isMsg ) 
          msgContext.setProperty( MessageContext.IS_MSG, "true" );

        if (username != null) {
            msgContext.setUsername(username);
        }
        if (password != null) {
            msgContext.setPassword(password);
        }
        msgContext.setMaintainSession(maintainSession);

        msgContext.setOperation(operation);

        operation.setStyle(getOperationStyle());
        msgContext.setOperationStyle(getOperationStyle());

        if (useSOAPAction) {
            msgContext.setUseSOAPAction(true);
        }
        if (SOAPActionURI != null) {
            msgContext.setSOAPActionURI(SOAPActionURI);
        }
        if (timeout != null) {
            msgContext.setTimeout(timeout.intValue());
        }

        // Determine client target service
        if (myService != null) {
            // If we have a SOAPService kicking around, use that directly
            msgContext.setService(myService);
        } else {
            if (portName != null) {
                // No explicit service.  If we have a target service name,
                // try that.
                msgContext.setTargetService(portName.getLocalPart());
            } else {
                // No direct config, so try the namespace of the first body.
                reqMsg = msgContext.getRequestMessage();
                reqEnv = reqMsg.getSOAPEnvelope();

                // If we have headers to insert, do so now.
                for (int i = 0 ; myHeaders != null && i < myHeaders.size() ; i++ )
                    reqEnv.addHeader((SOAPHeaderElement)myHeaders.get(i));

                SOAPBodyElement body = reqEnv.getFirstBody();

                // Does this make any sense to anyone?  If not, we should remove it.
                // --Glen 03/16/02
                //if ( body.getPrefix() == null )       body.setPrefix( "m" );
                if ( body.getNamespaceURI() == null ) {
                    throw new AxisFault("Call.invoke",
                                        Messages.getMessage("cantInvoke00", body.getName()),
                                        null, null);
                } else {
                    msgContext.setTargetService(body.getNamespaceURI());
                }
            }

            SOAPService svc = msgContext.getService();
            if (svc != null) {
                svc.setPropertyParent(scopedProperties);
            } else {
                msgContext.setPropertyParent(scopedProperties);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("targetService",
                    msgContext.getTargetService()));
        }

        // set up transport if there is one
        if (transport != null) {
            transport.setupMessageContext(msgContext, this, service.getEngine());
        }
        else
            msgContext.setTransportName( transportName );

        // For debugging - print request message
        if (log.isDebugEnabled()) {
            StringWriter writer = new StringWriter();
            try {
                SerializationContext ctx = new SerializationContextImpl(writer,
                                                                   msgContext);
                reqEnv.output(ctx);
                writer.close();
            } catch (Exception e) {
                log.debug(Messages.getMessage("exceptionPrinting"), e);
            } finally {
                log.debug(writer.getBuffer().toString());
            }
        }

        if(!invokeOneWay) {
            invokeEngine(msgContext);
        } else {
            invokeEngineOneWay(msgContext);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: Call::invoke()");
        }
    }

    private void invokeEngine(MessageContext msgContext) throws AxisFault {
      Boolean async = (Boolean)getProperty(AsyncCall.ASYNC_CALL_PROPERTY);
      if (async == null || !async.booleanValue()) {
        service.getEngine().invoke( msgContext );

        if (transport != null)
            transport.processReturnedMessageContext(msgContext);

        Message resMsg = msgContext.getResponseMessage();

        if (resMsg == null) {
          if (this.FAULT_ON_NO_RESPONSE) {
            throw new AxisFault(Messages.getMessage("nullResponse00"));
          } else {
            return;
          }
        }

        /** This must happen before deserialization...
         */
        resMsg.setMessageType(Message.RESPONSE);

        SOAPEnvelope resEnv = (SOAPEnvelope)resMsg.getSOAPEnvelope();

        SOAPBodyElement respBody = resEnv.getFirstBody();
        if (respBody instanceof SOAPFault) {
            throw ((SOAPFault)respBody).getFault();
        }
      } else {
        if (getAsyncStatus() == ASYNC_RUNNING) {
          // TODO: we need to throw an error 
          // We can't have two async operations messing with this
          // single Call object
        }
        Listener listener = (Listener)getProperty(AsyncCall.ASYNC_CALL_LISTENER);
        if (listener == null) {
          listener = new Listener() { // Create default RPC listener
            public void invokeComplete() {
              setAsyncStatus(ASYNC_READY);
            }
            public void onResponse(final MessageContext context)
              throws java.rmi.RemoteException {
              setReturnedValue(context);
            }
            public void onFault(final AxisFault fault) {
              log.debug(Messages.getMessage("exceptionPrinting"), fault);
              setReturnedFault(fault);
            }
          };
        }
        invokeEngineAsync(msgContext, listener);
      }
    }

    private void invokeEngineOneWay(final MessageContext msgContext) {
        Listener listener = (Listener)getProperty(AsyncCall.ASYNC_CALL_LISTENER);
        if (listener == null) {
          listener = new Listener() { // Create a non-op listener            
              public void invokeComplete() {
                setAsyncStatus(ASYNC_READY);
              }
              public void onResponse(MessageContext context) {
                // since this is a "one way" operation, we're going
                // to ignore any response messages.  If the user 
                // wants a response, they should use regular invoke
                // with the async property turned on.
              }
              public void onFault(AxisFault fault) {
                log.debug(Messages.getMessage("exceptionPrinting"), fault);
                setReturnedFault(fault);
              }
          };
        }
        invokeEngineAsync(msgContext, listener);
    }

    private final synchronized void setReturnedFault(AxisFault fault) {
      this.returnedFault = fault;
    }
    
    private final synchronized void setReturnedValue(MessageContext msgContext)
      throws AxisFault {
      try {
        Message resMsg      = msgContext.getResponseMessage();
      
        if (resMsg != null) {
          Object result          = null;
          SOAPEnvelope resEnv    = (SOAPEnvelope)resMsg.getSOAPEnvelope();
          SOAPBodyElement bodyEl = resEnv.getFirstBody();
          Vector resArgs         = null;
          if (bodyEl instanceof RPCElement) {
            try {
              resArgs = ((RPCElement) bodyEl).getParams();
            } catch (Exception e) {
              log.error(Messages.getMessage("exception00"), e);
                throw AxisFault.makeFault(e);
            } 
        
            if (resArgs != null && resArgs.size() > 0) {
              int outParamStart = 0;
              if (!XMLType.AXIS_VOID.equals(returnType)) {
                RPCParam param = (RPCParam)resArgs.get(0);
                result = param.getValue();
                outParamStart = 1;
              }
        
              for (int i = outParamStart; i < resArgs.size(); i++) {
                RPCParam param = (RPCParam) resArgs.get(i);
                Class javaType = getJavaTypeForQName(param.getQName());
                Object value = param.getValue();
                if (javaType != null &&
                  !javaType.isAssignableFrom(value.getClass())) {
                  value = JavaUtils.convert(value, javaType);
                }
      
                outParams.put(param.getQName(), value);
                      outParamsList.add(value);
              }
            }
          } else {
            try {
              result = bodyEl.getValueAsType(returnType);
            } catch (Exception e) {
              result = bodyEl;
            }
          }
          if (result instanceof org.apache.axis.message.SOAPFault) {
            this.returnedFault = ((org.apache.axis.message.SOAPFault)result).getFault();
          } else {
            this.returnValue = result;
          }
        }
      } catch (Exception e) {
        log.error(Messages.getMessage("exception00"), e);
        throw AxisFault.makeFault(e);
      }
    }

    private final void invokeEngineAsync(
      final MessageContext msgContext,
      final Listener listener) {
      AsyncCall asyncCall = AsyncCall.instance(service.getEngine());
      if (asyncCall == null) {
        // TODO: throw an error
      }
      try {
        mutex.acquire();
      } catch (Exception e) {}
      setAsyncStatus(ASYNC_RUNNING);
      asyncCall.invoke(msgContext,listener); // this MUST be non-blocking
    }

    public final Object waitForReturnValue()
      throws Throwable {
        return waitForReturnValue(Long.MAX_VALUE);
    }
    
    public final Object waitForReturnValue(long timeout)
      throws Throwable {
      if (mutex.attempt(timeout)) mutex.release();
      if (this.returnedFault != null) throw this.returnedFault;
      return this.returnValue;
    }

    public final synchronized Object getReturnValue() {
      return this.returnValue;
    }

    public final synchronized AxisFault getReturnedFault() {
      return this.returnedFault;
    }
    
    private final synchronized void setAsyncStatus(byte status) {
      this.current_async_status = status;
      if (status == ASYNC_READY) mutex.release();
    }

    public final synchronized byte getAsyncStatus() {
      return current_async_status;
    }

    /**
     * Get the output parameters (if any) from the last invocation.
     *
     * NOTE that the params returned are all RPCParams, containing
     * name and value - if you want the value, you'll need to call
     * param.getValue().
     *
     * @return Vector of RPCParams
     */
    public Map getOutputParams()
    {
        return this.outParams;
    }

    /**
     * Returns a List values for the output parameters of the last
     * invoked operation.
     *
     * @return Values for the output parameters. An empty List is
     *         returned if there are no output values.
     *
     * @throws JAXRPCException - If this method is invoked for a
     *                           one-way operation or is invoked
     *                           before any invoke method has been called.
     */
    public List getOutputValues() {
        return outParamsList;
    }

    /**
     * Get the Service object associated with this Call object.
     *
     * Note: Not part of JAX-RPC specification.
     *
     * @return Service the Service object this Call object is associated with
     */
    public Service getService()
    {
        return this.service;
    }

    private SOAPService myService = null;

    /**
     *
     */
    public void setSOAPService(SOAPService service)
    {
        myService = service;
        if (service != null) {
            // Set the service so that it defers missing property gets to the
            // Call.  So when client-side Handlers get at the MessageContext,
            // the property scoping will be MC -> SOAPService -> Call
            service.setPropertyParent(scopedProperties);
            service.setEngine(this.service.getAxisClient());
        }
    }

    /**
     * Sets the client-side request and response Handlers.  This is handy
     * for programatically setting up client-side work without deploying
     * via WSDD or the EngineConfiguration mechanism.
     */
    public void setClientHandlers(Handler reqHandler, Handler respHandler)
    {
        // Create a SOAPService which will be used as the client-side service
        // handler.
        setSOAPService(new SOAPService(reqHandler, null, respHandler));
    }

    protected java.util.Vector attachmentParts= new java.util.Vector();

    /**
     * This method adds an attachment.
     *
     * Note: Not part of JAX-RPC specification.
     * @exception RuntimeException if there is no support for attachments.
     *
     */
     public void addAttachmentPart( Object attachment){
         attachmentParts.add(attachment);
     }
}
