/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

package org.apache.axis ;

import org.apache.axis.attachments.Attachments;
import org.apache.axis.client.AxisClient;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.enum.Style;
import org.apache.axis.enum.Use;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.schema.SchemaVersion;
import org.apache.axis.session.Session;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.LockableHashtable;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A MessageContext is the Axis implementation of the javax
 * SOAPMessageContext class, and is core to message processing
 * in handlers and other parts of the system.
 *
 * This class also contains constants for accessing some
 * well-known properties. Using a hierarchical namespace is
 * strongly suggested in order to lower the chance for
 * conflicts.
 *
 * (These constants should be viewed as an explicit list of well
 *  known and widely used context keys, there's nothing wrong
 *  with directly using the key strings. This is the reason for
 *  the hierarchical constant namespace.
 *
 *  Actually I think we might just list the keys in the docs and
 *  provide no such constants since they create yet another
 *  namespace, but we'd have no compile-time checks then.
 *
 *  Whaddya think? - todo by Jacek)
 *
 *
 * @author Doug Davis (dug@us.ibm.com)
 * @author Jacek Kopecky (jacek@idoox.com)
 */
public class MessageContext implements SOAPMessageContext {
    protected static Log log =
            LogFactory.getLog(MessageContext.class.getName());

    /**
     * The request message.  If we're on the client, this is the outgoing
     * message heading to the server.  If we're on the server, this is the
     * incoming message we've received from the client.
     */
    private Message      requestMessage;

    /**
     * The response message.  If we're on the server, this is the outgoing
     * message heading back to the client.  If we're on the client, this is the
     * incoming message we've received from the server.
     */
    private Message      responseMessage;

    /**
     * That unique key/name that the next router/dispatch handler should use
     * to determine what to do next.
     */
    private String       targetService;

    /**
     * The name of the Transport which this message was received on (or is
     * headed to, for the client).
     */
    private String       transportName;

    /**
     * The default classloader that this service should use
     */
    private ClassLoader  classLoader;

    /**
     * The AxisEngine which this context is involved with
     */
    private AxisEngine   axisEngine;

    /**
     * A Session associated with this request.
     */
    private Session      session;

    /**
     * Should we track session state, or not?
     * default is not.
     * Could potentially refactor this so that
     * maintainSession iff session != null...
     */
    private boolean      maintainSession = false;

    /**
     * Are we doing request stuff, or response stuff?
     */
    private boolean      havePassedPivot = false;

    /**
     * Maximum amount of time to wait on a request, in milliseconds.
     */
    private int          timeout = 0;

    /**
     * An indication of whether we require "high fidelity" recording of
     * deserialized messages for this interaction.  Defaults to true for
     * now, and can be set to false, usually at service-dispatch time.
     */
    private boolean      highFidelity = true;

    /**
     * Storage for an arbitrary bag of properties associated with this
     * MessageContext.
     */
    private LockableHashtable bag = new LockableHashtable();

    /**
     * These variables are logically part of the bag, but are separated
     * because they are used often and the Hashtable is more expensive.
     */
    private String  username       = null;
    private String  password       = null;
    private String  encodingStyle  = Use.ENCODED.getEncoding();
    private boolean useSOAPAction  = false;
    private String  SOAPActionURI  = null;

    /** Our SOAP namespaces and such */
    private SOAPConstants soapConstants = Constants.DEFAULT_SOAP_VERSION;

    /** Schema version information - defaults to 2001 */
    private SchemaVersion schemaVersion = SchemaVersion.SCHEMA_2001;

    /** what is our current operation */
    private OperationDesc currentOperation = null;

    /**
     * the current operation
     * @return the current operation; may be null
     */
    public  OperationDesc getOperation()
    {
        return currentOperation;
    }

    /**
     * set the current operation
     * @param operation
     */
    public void setOperation(OperationDesc operation)
    {
        currentOperation = operation;
    }

    /**
     * getPossibleOperationsByQName
     * Returns a list of operation descriptors that could may 
     * possibly match a body containing an element of the given QName.
     * For non-DOCUMENT, the list of operation descriptors that match
     * the name is returned.  For DOCUMENT, all the operations that have
     * qname as a parameter are returned
     * @param qname of the first element in the body
     * @return list of operation descriptions
     */
    public OperationDesc [] getPossibleOperationsByQName(QName qname) throws AxisFault
    {
        if (currentOperation != null) {
            return new OperationDesc [] { currentOperation };
        }

        OperationDesc [] possibleOperations = null;

        if (serviceHandler == null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug(Messages.getMessage("dispatching00",
                                                   qname.getNamespaceURI()));
                }

                // Try looking this QName up in our mapping table...
                setService(axisEngine.getConfig().
                           getServiceByNamespaceURI(qname.getNamespaceURI()));
            } catch (ConfigurationException e) {
                // Didn't find one...
            }

        }

        if (serviceHandler != null) {
            ServiceDesc desc = serviceHandler.getInitializedServiceDesc(this);

            if (desc != null) {
                if (desc.getStyle() != null) {
                    possibleOperations = desc.getOperationsByQName(qname);
                } else {
                    // SBFIX : What the hell is this?

                    // DOCUMENT Style
                    // Get all of the operations that have qname as
                    // a possible parameter QName
                    ArrayList allOperations = desc.getOperations();
                    ArrayList foundOperations = new ArrayList();
                    for (int i=0; i < allOperations.size(); i++ ) {
                        OperationDesc tryOp = 
                            (OperationDesc) allOperations.get(i);
                        if (tryOp.getParamByQName(qname) != null) {
                            foundOperations.add(tryOp);
                        }
                    }
                    if (foundOperations.size() > 0) {
                        possibleOperations = (OperationDesc[])
                            JavaUtils.convert(foundOperations, 
                                              OperationDesc[].class);
                    }
                }
            }
        }
        return possibleOperations;
    }

    /**
     * get the first possible operation that could match a
     * body containing an element of the given QName. Sets the currentOperation
     * field in the process; if that field is already set then its value
     * is returned instead
     * @param qname name of the message body
     * @return an operation or null
     * @throws AxisFault
     */
    public OperationDesc getOperationByQName(QName qname) throws AxisFault
    {
        if (currentOperation == null) {
            OperationDesc [] possibleOperations = getPossibleOperationsByQName(qname);
            if (possibleOperations != null && possibleOperations.length > 0) {
                currentOperation = possibleOperations[0];
            }
        }

        return currentOperation;
    }

    /**
     * Get the active message context.
     * @return the current active message context
     */
    public static MessageContext getCurrentContext() {
       return AxisEngine.getCurrentMessageContext();
    }

    /**
     * temporary directory to store attachments
     */
    protected static String systemTempDir= null;
    /**
     * set the temp dir
     * TODO: move this piece of code out of this class and into a utilities
     * class.
     */
    static {
        try {
            //get the temp dir from the engine
            systemTempDir=AxisProperties.getProperty(AxisEngine.ENV_ATTACHMENT_DIR);
        } catch(Throwable t) {
            systemTempDir= null;
        }

        if(systemTempDir== null) {
            try {
                //or create and delete a file in the temp dir to make
                //sure we have write access to it.
                File tf= File.createTempFile("Axis", "Axis");
                File dir= tf.getParentFile();
                if (tf.exists()) {
                    tf.delete();
                }
                if (dir != null) {
                  systemTempDir= dir.getCanonicalPath();
                }
            } catch(Throwable t) {
                log.debug("Unable to find a temp dir with write access");
                systemTempDir= null;
            }
        }
    }

    /**
     * Create a message context.
     * @param engine the controlling axis engine. Null is actually accepted here,
     * though passing a null engine in is strongly discouraged as many of the methods
     * assume that it is in fact defined.
     */
    public MessageContext(AxisEngine engine) {
        this.axisEngine = engine;

        if(null != engine){
            java.util.Hashtable opts= engine.getOptions();
            String attachmentsdir= null;
            if(null!=opts) {
                attachmentsdir= (String) opts.get(AxisEngine.PROP_ATTACHMENT_DIR);
            }
            if(null == attachmentsdir) {
                attachmentsdir= systemTempDir;
            }
            if(attachmentsdir != null){
                setProperty(ATTACHMENTS_DIR, attachmentsdir);
            }

            // If SOAP 1.2 has been specified as the default for the engine,
            // switch the constants over.
            String defaultSOAPVersion = (String)engine.getOption(
                                                 AxisEngine.PROP_SOAP_VERSION);
            if (defaultSOAPVersion != null && "1.2".equals(defaultSOAPVersion)) {
                setSOAPConstants(SOAPConstants.SOAP12_CONSTANTS);
            }
        }
    }

    /**
     * Mappings of QNames to serializers/deserializers (and therfore
     * to Java types).
     */
    private TypeMappingRegistry mappingRegistry = null;

    /**
     * replace the engine's type mapping registry with a local one
     * @param reg
     */
    public void setTypeMappingRegistry(TypeMappingRegistry reg) {
        mappingRegistry = reg;
    }

    /**
     * Get the currently in-scope type mapping registry.
     *
     * By default, will return a reference to the AxisEngine's TMR until
     * someone sets our local one (usually as a result of setting the
     * serviceHandler).
     *
     * @return the type mapping registry to use for this request.
     */
    public TypeMappingRegistry getTypeMappingRegistry() {
        if (mappingRegistry == null) {
            return axisEngine.getTypeMappingRegistry();
        }

        return mappingRegistry;
    }

    /**
     * Return the type mapping currently in scope for our encoding style
     */
    public TypeMapping getTypeMapping()
    {
        return (TypeMapping)getTypeMappingRegistry().
                getTypeMapping(encodingStyle);
    }

    /**
     * Transport
     */
    public String getTransportName()
    {
        return transportName;
    }

    public void setTransportName(String transportName)
    {
        this.transportName = transportName;
    }

    /**
     * SOAP constants
     */
    public SOAPConstants getSOAPConstants() {
        return soapConstants;
    }

    public void setSOAPConstants(SOAPConstants soapConstants) {
        // when changing SOAP versions, remember to keep the encodingURI
        // in synch.
        if (this.soapConstants.getEncodingURI().equals(encodingStyle)) {
            encodingStyle = soapConstants.getEncodingURI();
        }

        this.soapConstants = soapConstants;
    }

    /**
     * Schema version information
     */

    public SchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(SchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * Sessions
     */
    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    /**
     * Encoding
     */
    public boolean isEncoded() {
        return (getOperationUse() == Use.ENCODED);
        //return soapConstants.getEncodingURI().equals(encodingStyle);
    }

    /**
     * Set whether we are maintaining session state
     * @param yesno flag to set to true to maintain sessions
     */
    public void setMaintainSession (boolean yesno) {
        maintainSession = yesno;
    }

    /**
     * Are we maintaining session state?
     */
    public boolean getMaintainSession () {
        return maintainSession;
    }

    /**
     * Get the request message.
     *
     * @return the request message (may be null).
     */
    public Message getRequestMessage() {
        return requestMessage ;
    };

    /**
     * Set the request message, and make sure that message is associated
     * with this MessageContext.
     *
     * @param reqMsg the new request Message.
     */
    public void setRequestMessage(Message reqMsg) {
        requestMessage = reqMsg ;
        if (requestMessage != null) {
            requestMessage.setMessageContext(this);
        }
    };

    /**
     * Get the response message.
     *
     * @return the response message (may be null).
     */
    public Message getResponseMessage() { return responseMessage ; }

    /**
     * Set the response message, and make sure that message is associated
     * with this MessageContext.
     *
     * @param respMsg the new response Message.
     */
    public void setResponseMessage(Message respMsg) {
        responseMessage = respMsg;
        if (responseMessage != null) {
            responseMessage.setMessageContext(this);

            //if we have received attachments of a particular type
            // than that should be the default type to send.
            Message reqMsg = getRequestMessage();
            if (null != reqMsg) {
                Attachments reqAttch = reqMsg.getAttachmentsImpl();
                Attachments respAttch = respMsg.getAttachmentsImpl();
                if (null != reqAttch && null != respAttch) {
                    if (respAttch.getSendType() == Attachments.SEND_TYPE_NOTSET)
                        //only if not explicity set.
                        respAttch.setSendType(reqAttch.getSendType());
                }
            }
        }
    }

    /**
     * Return the current (i.e. request before the pivot, response after)
     * message.
     */
    public Message getCurrentMessage()
    {
        return (havePassedPivot ? responseMessage : requestMessage);
    }

    /**
     *  Gets the SOAPMessage from this message context
     *  @return Returns the SOAPMessage; returns null if no request
     *          SOAPMessage is present in this SOAPMessageContext
     */
    public javax.xml.soap.SOAPMessage getMessage() {
        return getCurrentMessage();
    }

    /**
     * Set the current (i.e. request before the pivot, response after)
     * message.
     */
    public void setCurrentMessage(Message curMsg)
    {
        curMsg.setMessageContext(this);

        if (havePassedPivot) {
            responseMessage = curMsg;
        } else {
            requestMessage = curMsg;
        }
    }

    /**
     *  Sets the SOAPMessage for this message context
     *  @param   message  Request SOAP message
     *  @throws java.lang.UnsupportedOperationException If this
     *     operation is not supported
     */
    public void setMessage(javax.xml.soap.SOAPMessage message) {
        setCurrentMessage((Message)message);
    }

    /**
     * Determine when we've passed the pivot
     */
    public boolean getPastPivot()
    {
        return havePassedPivot;
    }

    /**
     * Indicate when we've passed the pivot
     */
    public void setPastPivot(boolean pastPivot)
    {
        havePassedPivot = pastPivot;
    }

    /**
     * Set timeout in our MessageContext.
     *
     * @param value the maximum amount of time, in milliseconds
     */
    public void setTimeout (int value) {
        timeout = value;
    }

    /**
     * Get timeout from our MessageContext.
     *
     * @return value the maximum amount of time, in milliseconds
     */
    public int getTimeout () {
        return timeout;
    }

    /**
     * get the classloader, implicitly binding to the thread context
     * classloader if an override has not been supplied
     * @return
     */
    public ClassLoader getClassLoader() {
        if ( classLoader == null ) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return( classLoader );
    }

    /**
     * set a new classloader
     * @param cl
     */
    public void setClassLoader(ClassLoader cl ) {
        classLoader = cl ;
    }

    public String getTargetService() {
        return( targetService );
    }

    /**
     * get the axis engine. Will be null if the message was created outside
     * an engine
     * @return the current axis engine
     */
    public AxisEngine getAxisEngine()
    {
        return axisEngine;
    }

    /**
     * Set the target service for this message.
     *
     * This looks up the named service in the registry, and has
     * the side effect of setting our TypeMappingRegistry to the
     * service's.
     *
     * @param tServ the name of the target service.
     */
    public void setTargetService(String tServ) throws AxisFault {
        log.debug("MessageContext: setTargetService(" + tServ+")");

        if (tServ == null) {
            setService(null);
        }
        else {
            try {
                setService(getAxisEngine().getService(tServ));
            } catch (AxisFault fault) {
                // If we're on the client, don't throw this fault...
                if (!isClient()) {
                    throw fault;
                }
            }
        }
        targetService = tServ;
    }

    /** ServiceHandler is the handler that is the "service".  This handler
     * can (and probably will actually be a chain that contains the
     * service specific request/response/pivot point handlers
     */
    private SOAPService serviceHandler ;

    public SOAPService getService() {
        return( serviceHandler );
    }

    public void setService(SOAPService sh) throws AxisFault
    {
        log.debug("MessageContext: setServiceHandler("+sh+")");
        serviceHandler = sh;
        if (sh != null) {
            targetService = sh.getName();
            SOAPService service = sh;
            TypeMappingRegistry tmr = service.getTypeMappingRegistry();
            setTypeMappingRegistry(tmr);

            // styles are not "soap version aware" so compensate...
            setEncodingStyle(service.getUse().getEncoding());

            // This MessageContext should now defer properties it can't find
            // to the Service's options.
            bag.setParent(sh.getOptions());

            // Note that we need (or don't need) high-fidelity SAX recording
            // of deserialized messages according to the setting on the
            // new service.
            highFidelity = service.needsHighFidelityRecording();

            service.getInitializedServiceDesc(this);
        }
    }

    /**
     * Let us know whether this is the client or the server.
     */
    public boolean isClient()
    {
        return (axisEngine instanceof AxisClient);
    }

    /** Contains an instance of Handler, which is the
     *  ServiceContext and the entrypoint of this service.
     *
     *  (if it has been so configured - will our deployment
     *   tool do this by default?  - todo by Jacek)
     */
    public final static String ENGINE_HANDLER      = "engine.handler";

    /** This String is the URL that the message came to
     */
    public final static String TRANS_URL           = "transport.url";

    /** Has a quit been requested? Hackish... but useful... -- RobJ */
    public final static String QUIT_REQUESTED = "quit.requested";

    /** Place to store an AuthenticatedUser */
    public final static String AUTHUSER            = "authenticatedUser";

    /** If on the client - this is the Call object */
    public final static String CALL                = "call_object" ;

    /** Are we doing Msg vs RPC? - For Java Binding */
    public final static String IS_MSG              = "isMsg" ;

    /** The directory where in coming attachments are created. */
    public final static String ATTACHMENTS_DIR   = "attachments.directory" ;

    /** A boolean param, to control whether we accept missing parameters
     * as nulls or refuse to acknowledge them.
     */
    public final static String ACCEPTMISSINGPARAMS = "acceptMissingParams";

    /** The value of the property is used by service WSDL generation (aka ?WSDL)
     * For the service's interface namespace if not set TRANS_URL property is used.
     */
    public final static String WSDLGEN_INTFNAMESPACE      = "axis.wsdlgen.intfnamespace";

    /** The value of the property is used by service WSDL generation (aka ?WSDL)
     * For the service's location if not set TRANS_URL property is used.
     *  (helps provide support through proxies.
     */
    public final static String WSDLGEN_SERV_LOC_URL      = "axis.wsdlgen.serv.loc.url";

    /** The value of the property is used by service WSDL generation (aka ?WSDL)
     *  Set this property to request a certain level of HTTP.
     *  The values MUST use org.apache.axis.transport.http.HTTPConstants.HEADER_PROTOCOL_10
     *    for HTTP 1.0
     *  The values MUST use org.apache.axis.transport.http.HTTPConstants.HEADER_PROTOCOL_11
     *    for HTTP 1.1
     */
    public final static String HTTP_TRANSPORT_VERSION  = "axis.transport.version";

    /*
     * IMPORTANT.
     * If adding any new constants to this class. Make them final. The
     * ones above are left non-final for compatibility reasons.
     */

    /** Just a util so we don't have to cast the result
     */
    public String getStrProp(String propName) {
        return( (String) getProperty(propName) );
    }

    /**
     * Tests to see if the named property is set in the 'bag'.
     * If not there then 'false' is returned.
     * If there, then...
     *   if its a Boolean, we'll return booleanValue()
     *   if its an Integer,  we'll return 'false' if its '0' else 'true'
     *   if its a String, we'll return 'false' if its 'false' or '0' else 'true'
     *   All other types return 'true'
     */
    public boolean isPropertyTrue(String propName) {
        return isPropertyTrue(propName, false);
    }

    /**
     * Tests to see if the named property is set in the 'bag'.
     * If not there then 'defaultVal' will be returned.
     * If there, then...
     *   if its a Boolean, we'll return booleanValue()
     *   if its an Integer,  we'll return 'false' if its '0' else 'true'
     *   if its a String, we'll return 'false' if its 'false', 'no', or '0' - else 'true'
     *   All other types return 'true'
     */
    public boolean isPropertyTrue(String propName, boolean defaultVal) {
        return JavaUtils.isTrue(getProperty(propName), defaultVal);
    }

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
            return;
            // Is this right?  Shouldn't we throw an exception like:
            // throw new IllegalArgumentException(msg);
        }
        else if (name.equals(Call.USERNAME_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            setUsername((String) value);
        }
        else if (name.equals(Call.PASSWORD_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[] {
                        name, "java.lang.String", value.getClass().getName()}));
            }
            setPassword((String) value);
        }
        else if (name.equals(Call.SESSION_MAINTAIN_PROPERTY)) {
            if (!(value instanceof Boolean)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.Boolean",
                        value.getClass().getName()}));
            }
            setMaintainSession(((Boolean) value).booleanValue());
        }
        else if (name.equals(Call.SOAPACTION_USE_PROPERTY)) {
            if (!(value instanceof Boolean)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.Boolean",
                        value.getClass().getName()}));
            }
            setUseSOAPAction(((Boolean) value).booleanValue());
        }
        else if (name.equals(Call.SOAPACTION_URI_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.String",
                        value.getClass().getName()}));
            }
            setSOAPActionURI((String) value);
        }
        else if (name.equals(Call.ENCODINGSTYLE_URI_PROPERTY)) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                        Messages.getMessage("badProp00", new String[]
                        {name,
                        "java.lang.String",
                        value.getClass().getName()}));
            }
            setEncodingStyle((String) value);
        }
        else {
            bag.put(name, value);
        }
    } // setProperty

    /**
     *  Returns true if the MessageContext contains a property with the specified name.
     *  @param   name Name of the property whose presense is to be tested
     *  @return  Returns true if the MessageContext contains the
          property; otherwise false
     */
    public boolean containsProperty(String name) {
        Object propertyValue = getProperty(name);
        return (propertyValue != null);
    }

    /**
     *  Returns an Iterator view of the names of the properties in this MessageContext
     *  @return Iterator for the property names
     */
    public java.util.Iterator getPropertyNames() {
        return bag.keySet().iterator();
    }

    /**
     * Returns the value associated with the named property - or null if not
     * defined/set.
     *
     * @return Object value of the property - or null
     */
    public Object getProperty(String name) {
        if (name != null) {
            if (name.equals(Call.USERNAME_PROPERTY)) {
                return getUsername();
            }
            else if (name.equals(Call.PASSWORD_PROPERTY)) {
                return getPassword();
            }
            else if (name.equals(Call.SESSION_MAINTAIN_PROPERTY)) {
                return new Boolean(getMaintainSession());
            }
            else if (name.equals(Call.OPERATION_STYLE_PROPERTY)) {
                return (getOperationStyle() == null) ? null : getOperationStyle().getName();
            }
            else if (name.equals(Call.SOAPACTION_USE_PROPERTY)) {
                return new Boolean(useSOAPAction());
            }
            else if (name.equals(Call.SOAPACTION_URI_PROPERTY)) {
                return getSOAPActionURI();
            }
            else if (name.equals(Call.ENCODINGSTYLE_URI_PROPERTY)) {
                return getEncodingStyle();
            }
            else if (bag == null) {
                return null;
            }
            else {
                return bag.get(name);
            }
        }
        else {
            return null;
        }
    }

    public void setPropertyParent(Hashtable parent)
    {
        bag.setParent(parent);
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
     * Get the operation style.
     */
    public Style getOperationStyle() {
        if (currentOperation != null) {
            return currentOperation.getStyle();
        }

        if (serviceHandler != null) {
            return serviceHandler.getStyle();
        }

        return Style.RPC;
    } // getOperationStyle

    /**
     * Get the operation use.
     */
    public Use getOperationUse() {
        if (currentOperation != null) {
            return currentOperation.getUse();
        }

        if (serviceHandler != null) {
            return serviceHandler.getUse();
        }

        return Use.ENCODED;
    } // getOperationUse

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
        this.SOAPActionURI = SOAPActionURI;
    } // setSOAPActionURI

    /**
     * Get the soapAction URI.
     */
    public String getSOAPActionURI() {
        return SOAPActionURI;
    } // getSOAPActionURI

    /**
     * Sets the encoding style to the URI passed in.
     *
     * @param namespaceURI URI of the encoding to use.
     */
    public void setEncodingStyle(String namespaceURI) {
        if (namespaceURI == null) {
            namespaceURI = Constants.URI_LITERAL_ENC;
        }
        else if (Constants.isSOAP_ENC(namespaceURI)) {
            namespaceURI = soapConstants.getEncodingURI();
        }

        encodingStyle = namespaceURI;
    } // setEncodingStype

    /**
     * Returns the encoding style as a URI that should be used for the SOAP
     * message.
     *
     * @return String URI of the encoding style to use
     */
    public String getEncodingStyle() {
        return encodingStyle;
    } // getEncodingStyle

    public void removeProperty(String propName)
    {
        if (bag != null) {
            bag.remove(propName);
        }
    }

    public void reset()
    {
        if (bag != null) {
            bag.clear();
        }
        serviceHandler = null;
        havePassedPivot = false;
        currentOperation = null;
    }

    public boolean isHighFidelity() {
        return highFidelity;
    }

    public void setHighFidelity(boolean highFidelity) {
        this.highFidelity = highFidelity;
    }

    /**
     * <i>Not (yet) implemented method in the SOAPMessageContext interface</i>
     * 
     * Gets the SOAP actor roles associated with an execution of the HandlerChain and its contained Handler instances.
     * Note that SOAP actor roles apply to the SOAP node and are managed using HandlerChain.setRoles and
     * HandlerChain.getRoles. Handler instances in the HandlerChain use this information about the SOAP actor roles
     * to process the SOAP header blocks. Note that the SOAP actor roles are invariant during the processing of
     * SOAP message through the HandlerChain.
     *
     * @return Array of URIs for SOAP actor roles
     * @see javax.xml.rpc.handler.HandlerChain#setRoles(java.lang.String[]) HandlerChain.setRoles(java.lang.String[])
     * @see javax.xml.rpc.handler.HandlerChain#getRoles() HandlerChain.getRoles()
     */
    public String[] getRoles() {
        //TODO: Flesh this out.
        return null;
    }
}
