/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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

import java.util.* ;
import java.net.*;
import org.apache.axis.encoding.ServiceDescription;
import org.apache.axis.message.*;
import org.apache.axis.handlers.* ;
import org.apache.axis.utils.* ;
import org.apache.axis.* ;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.registries.HandlerRegistry;
import org.apache.axis.message.DebugHeader;

import org.w3c.dom.* ;

import java.io.*;
import org.apache.axis.encoding.SerializationContext;

/**
 * Allows an Axis service to be invoked from the client side.
 * Contains message and session state which may be reused
 * across multiple invocations on the same ServiceClient.
 *
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Doug Davis (dug@us.ibm.com)
 * @author Glen Daniels (gdaniels@macromedia.com)
 */


// Need to add proxy, ssl.... other cool things - but it's a start
// Only supports String

public class ServiceClient {
    // keep prop hashtable small
    private Hashtable properties = new Hashtable(10);
    protected String encodingStyleURI = null ;
    
    // For testing
    private static Handler localServer = null ;
    public  boolean doLocal = false ;
    private static final boolean DEBUG_LOG = false;
    
    // Our AxisClient
    private AxisClient engine;
    
    // The description of our service
    private ServiceDescription serviceDesc;
    
    // The message context we use across invocations
    private MessageContext msgContext;
    
    // Our Transport, if any
    private Transport transport;
    
    /**
     * Construct a ServiceClient with no properties.
     * Set it up yourself!
     */
    public ServiceClient () {
        engine = new AxisClient();
        msgContext = new MessageContext(engine);
    }
    
    /**
     * Construct a ServiceClient with a given endpoint URL
     */
    public ServiceClient(String endpointURL)
    {
        this();
        
        try {
            URL url = new URL(endpointURL);
            String protocol = url.getProtocol();
            setTransport(engine.getTransportForProtocol(protocol));
            set(MessageContext.TRANS_URL, endpointURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Construct a ServiceClient with the given Transport.
     */
    public ServiceClient (Transport transport) {
        this();
        setTransport(transport);
    }
    
    public void setTransport(Transport transport) {
        this.transport = transport;
        
        // set up the message context with the transport
        try {
            transport.init(engine);
            
            transport.initMessageContext(msgContext, this, engine);
        } catch (AxisFault f) {
            // this will happen if there is no appropriate service
            // what?  system.err for now
            System.err.println("ServiceClient(Transport): Faulted when initializing message context: "+f);
        }
    }
    
    /**
     * Set property; pass through to MessageContext.
     * This works because the constants defined in Transport and its
     * subclasses are synonyms for MessageContext constants.
     */
    public void set (String name, String value) {
        if (value == null) return;
        
        msgContext.setProperty(name, value);
    }
    
    /**
     * Get property; pass through to MessageContext.
     * This works because the constants defined in Transport and its
     * subclasses are synonyms for MessageContext constants.
     */
    public String get (String name) {
        return (String)msgContext.getProperty(name);
    }
    
    public void setEncodingStyleURI( String uri ) {
        encodingStyleURI = uri ;
    }
    
    public String getEncodingStyleURI() {
        return( encodingStyleURI );
    }
    
    public void setTransportRequest(String handlerName) {
      msgContext.setProperty( MessageContext.TRANS_REQUEST, handlerName );
    }
  
    public void setTransportResponse(String handlerName) {
      msgContext.setProperty( MessageContext.TRANS_RESPONSE, handlerName );
    }
    
    public void setRequestMessage(Message msg) {
        msgContext.setRequestMessage(msg);
    }
    
    /**
     * pass through whether we are maintaining session state
     */
    public void setMaintainSession (boolean yesno) {
        msgContext.setMaintainSession(yesno);
    }
    
    /**
     * all-purpose accessor for fringe cases....
     */
    public MessageContext getMessageContext () {
        return msgContext;
    }
     
    public void setServiceDescription(ServiceDescription serviceDesc)
    {
        this.serviceDesc = serviceDesc;
    }
    
    public void addSerializer(Class _class, QName qName, Serializer serializer) {
        TypeMappingRegistry typeMap = msgContext.getTypeMappingRegistry();
        typeMap.addSerializer(_class, qName, serializer);
    }
    
    public void addDeserializerFactory(QName qName, Class _class,
                                       DeserializerFactory deserializerFactory) {
        TypeMappingRegistry typeMap = msgContext.getTypeMappingRegistry();
        typeMap.addDeserializerFactory(qName, _class, deserializerFactory);
    }
    
    public Object invoke( String namespace, String method, Object[] args ) throws AxisFault {
        Debug.Print( 1, "Enter: ServiceClient::invoke(ns, meth, args)" );
        RPCElement  body = new RPCElement(namespace, method, args, serviceDesc);
        Object ret = invoke( body );
        Debug.Print( 1, "Exit: ServiceClient::invoke(ns, meth, args)" );
        return ret;
    }
    
    public Object invoke( RPCElement body ) throws AxisFault {
        // quote = HTTPCall.invoke( "getQuote", Object[] { "IBM" } );
        Debug.Print( 1, "Enter: ServiceClient::invoke(RPCElement)" );
        SOAPEnvelope         reqEnv = new SOAPEnvelope();
        SOAPEnvelope         resEnv = null ;
        Message              reqMsg = new Message( reqEnv );
        Message              resMsg = null ;
        Vector               resBodies = null ;
        Vector               resArgs = null ;
        Object               result = null ;
        
        if ( encodingStyleURI != null )
            reqEnv.setEncodingStyleURI( encodingStyleURI );
        
        msgContext.setServiceDescription(serviceDesc);
        msgContext.setRequestMessage(reqMsg);
        reqEnv.setMessageType(ServiceDescription.REQUEST);
        
        reqEnv.addBodyElement(body);
        
        if ( body.getPrefix() == null )       body.setPrefix( "m" );
        if ( body.getNamespaceURI() == null ) {
            throw new AxisFault("ServiceClient.invoke", "Cannot invoke ServiceClient with null namespace URI for method "+body.getMethodName(),
                                null, null);
        }
        
        if (DEBUG_LOG) {
            try {
                SerializationContext ctx = new SerializationContext(new PrintWriter(System.out), msgContext);
                System.out.println("");
                System.out.println("**DEBUG**");
                reqEnv.output(ctx);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            } finally {
                System.out.println("");
                System.out.println("**DEBUG**");
                System.out.println("");
            }
        }
        
        try {
            invoke();
        }
        catch( Exception e ) {
            Debug.Print( 1, e );
            if ( !(e instanceof AxisFault ) ) e = new AxisFault( e );
            throw (AxisFault) e ;
        }
        
        resMsg = msgContext.getResponseMessage();
        
        if (resMsg == null)
            throw new AxisFault(new Exception("Null response message!"));
        
        /** This must happen before deserialization...
         */
        resMsg.setMessageType(ServiceDescription.RESPONSE);
        
        resEnv = (SOAPEnvelope)resMsg.getAsSOAPEnvelope();

        SOAPBodyElement respBody = resEnv.getFirstBody();
        if (respBody instanceof SOAPFaultElement) {
            throw ((SOAPFaultElement)respBody).getAxisFault();
        }
        
        body = (RPCElement)resEnv.getFirstBody();
        resArgs = body.getParams();
        
        if (resArgs != null && resArgs.size() > 0) {
            RPCParam param = (RPCParam)resArgs.get(0);
            result = param.getValue();
        }
        
        Debug.Print( 1, "Exit: ServiceClient::invoke(RPCElement)" );
        return( result );
    }
    
    /**
     * invoke this ServiceClient with its established MessageContext
     * (perhaps because you called this.setRequestMessage())
     */
    public void invoke() throws AxisFault {
        Debug.Print( 1, "Enter: ClientMessage::invoke(MessageContext)" );
        
        // If local is specified, override the transport handlers Once there
        // is a convenient way for the client to specify the transport
        // handler, we might want to consider removing this code and doLocal
        // entirely.
        if (doLocal) {
            msgContext.setProperty(MessageContext.TRANS_REQUEST, "LocalSender");
            msgContext.clearProperty(MessageContext.TRANS_RESPONSE);
        }

        // set up message context if there is a transport
        if (transport != null) {
            transport.setupMessageContext(msgContext, this, this.engine);
        }
        
        Message              inMsg = msgContext.getRequestMessage();
        
        SOAPEnvelope         reqEnv = null ;
        
        reqEnv = (SOAPEnvelope) inMsg.getAsSOAPEnvelope();
        if ( encodingStyleURI != null )
            reqEnv.setEncodingStyleURI( encodingStyleURI );
        
        //SOAPBody  body = new SOAPBody( (Document) inMsg.getAs("Document") );
        //!!!reqEnv.addBodyElement( body );
        
        // local (if null) or pre-existing transport (if !null)
        Message              reqMsg = new Message( reqEnv );
        
        if ( Debug.getDebugLevel() > 0  ) {
            DebugHeader  header = new DebugHeader(Debug.getDebugLevel());
            header.setActor( Constants.URI_NEXT_ACTOR );
            
            reqEnv.addHeader( header );
        }
        
        try {
            engine.invoke( msgContext );
            engine.cleanup();
        }
        catch( AxisFault fault ) {
            Debug.Print( 1,  fault );
            throw fault ;
        }
        
        // Message       resMsg = msgContext.getResponseMessage();
        //SOAPEnvelope  resEnv = (SOAPEnvelope) resMsg.getAs( "SOAPEnvelope" );
        // msgContext.setResponseMessage(resMsg);
        /*
         SOAPBody      resBody = null; //resEnv.getFirstBody();
        
         doc = XMLUtils.newDocument();
         Element  root = resBody.getRoot();
         if ( root.getOwnerDocument() != doc )
         doc.appendChild( doc.importNode( root, true ) );
         else
         doc.appendChild( root );
        
         mc.setResponseMessage( new Message(doc, "Document") );
         */
        
        Debug.Print( 1, "Exit: ServiceClient::invoke(MessageContext)" );
    }
    
}

