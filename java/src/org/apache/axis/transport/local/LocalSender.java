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

package org.apache.axis.transport.local;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFaultElement;
import org.apache.axis.server.AxisServer;
import org.apache.log4j.Category;

import java.net.URL;

/**
 * This is meant to be used on a SOAP Client to call a SOAP server.
 *
 * @author Sam Ruby <rubys@us.ibm.com>
 */
public class LocalSender extends BasicHandler {
    static Category category =
            Category.getInstance(LocalSender.class.getName());

    private volatile AxisServer server;

    /**
     * Allocate an embedded Axis server to process requests and initialize it.
     */
    public synchronized void init() {
        AxisServer server = new AxisServer(new FileProvider(Constants.SERVER_CONFIG_FILE));
        this.server=server;
    }

    public void invoke(MessageContext clientContext) throws AxisFault {
        category.debug("Enter: LocalSender::invoke" );

        AxisServer targetServer = (AxisServer)clientContext.
                                                            getProperty(LocalTransport.LOCAL_SERVER);
        category.debug( "LocalSender using server " + targetServer);

        if (targetServer == null) {
            // This should have already been done, but it doesn't appear to be
            // something that can be relied on.  Oh, well...
            if (server == null) init();
            targetServer = server;
        }

        // Define a new messageContext per request
        MessageContext serverContext = new MessageContext(targetServer);

        // copy the request, and force its format to String in order to
        // exercise the serializers.
        String msgStr = clientContext.getRequestMessage().getSOAPPart().getAsString();

        category.debug( "LocalSender sending XML:");
        category.debug( msgStr);

        serverContext.setRequestMessage(new Message(msgStr));
        serverContext.setTransportName("local");

        // Also copy authentication info if present
        String user = clientContext.getStrProp(MessageContext.USERID);
        if (user != null) {
            serverContext.setProperty(MessageContext.USERID, user);
            String pass = clientContext.getStrProp(MessageContext.PASSWORD);
            if (pass != null)
                serverContext.setProperty(MessageContext.PASSWORD, pass);
        }

        // set the realpath if possible
        String transURL = clientContext.getStrProp(MessageContext.TRANS_URL);
        if (transURL != null) {
            try {
                URL url = new URL(transURL);
                if (url.getProtocol().equals("file")) {
                    String file = url.getFile();
                    if (file.length()>0 && file.charAt(0)=='/') file = file.substring(1);
                    serverContext.setProperty(Constants.MC_REALPATH, file);
                }
            } catch (Exception e) {
            }
        }

        // invoke the request
        try {
            targetServer.invoke(serverContext);
        } catch (AxisFault fault) {
            Message respMsg = serverContext.getResponseMessage();
            if (respMsg == null) {
                respMsg = new Message(fault);
                serverContext.setResponseMessage(respMsg);
            } else {
                SOAPFaultElement faultEl = new SOAPFaultElement(fault);
                SOAPEnvelope env = respMsg.getSOAPPart().getAsSOAPEnvelope();
                env.clearBody();
                env.addBodyElement(faultEl);
            }
        }

        // copy back the response, and force its format to String in order to
        // exercise the deserializers.
        clientContext.setResponseMessage(serverContext.getResponseMessage());
        //clientContext.getResponseMessage().getAsString();

        category.debug("Exit: LocalSender::invoke" );
    }

    public void undo(MessageContext msgContext) {
        category.debug("Enter: LocalSender::undo" );
        category.debug("Exit: LocalSender::undo" );
    }
};
