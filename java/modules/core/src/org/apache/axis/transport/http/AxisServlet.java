/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis.transport.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis.util.Utils;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet {
    /**
     * Field engineRegistry
     */

    private ConfigurationContext engineContext;

    /**
     * Method init
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            engineContext = erfac.buildEngineContext(repoDir);
            engineContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Method doGet
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse)
        throws ServletException, IOException {

        try {
            String operation =
                httpServletRequest.getParameter(Constants.REST_WITH_GET.GET_PARAMETER_OPERATION);
            if (operation != null) {
                SOAPFactory soapFactory = new SOAP11Factory();
                SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();

                OMNamespace omNs =
                    soapFactory.createOMNamespace(
                        Constants.REST_WITH_GET.GET_PARAMETER_URL,
                        "rest");
                OMElement opElement =
                    soapFactory.createOMElement(
                        Constants.REST_WITH_GET.GET_PARAMETER_OPERATION,
                        omNs);

                Enumeration enu = httpServletRequest.getParameterNames();
                while (enu.hasMoreElements()) {
                    String name = (String) enu.nextElement();
                    if (!Constants.REST_WITH_GET.GET_PARAMETER_OPERATION.equals(name)) {
                        String value = httpServletRequest.getParameter(name);
                        OMElement omEle =
                            soapFactory.createOMElement(
                                Constants.REST_WITH_GET.GET_PARAMETER_OPERATION,
                                omNs);
                        omEle.setText(value);
                        opElement.addChild(omEle);
                    }
                }

                envelope.getBody().addChild(opElement);
                XMLStreamWriter wrtier =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
                envelope.serialize(wrtier);
                System.out.flush();
                Object sessionContext =
                    httpServletRequest.getSession().getAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY);
                if (sessionContext == null) {
                    sessionContext = new SessionContext(null);
                    httpServletRequest.getSession().setAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY,
                        sessionContext);
                }
                MessageContext msgContext =
                    new MessageContext(
                        engineContext,
                        (SessionContext) sessionContext,
                        engineContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_HTTP)),
                        engineContext.getAxisConfiguration().getTransportOut(
                            new QName(Constants.TRANSPORT_HTTP)));
                msgContext.setProperty(Constants.Configuration.DO_REST,Constants.VALUE_TRUE);

                msgContext.setEnvelope(envelope);
                processSOAPMessage(msgContext, httpServletRequest, httpServletResponse);

            } else {
                ListingAgent lister = new ListingAgent(engineContext);
                lister.handle(httpServletRequest, httpServletResponse);
            }
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    /*
    * (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */

    /**
     * Method doPost
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        try {
            Object sessionContext =
                req.getSession().getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if (sessionContext == null) {
                sessionContext = new SessionContext(null);
                req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY, sessionContext);
            }
            MessageContext msgContext =
                new MessageContext(
                    engineContext,
                    (SessionContext) sessionContext,
                    engineContext.getAxisConfiguration().getTransportIn(
                        new QName(Constants.TRANSPORT_HTTP)),
                    engineContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP)));

            XMLStreamReader reader =
                XMLInputFactory.newInstance().createXMLStreamReader(
                    new BufferedReader(new InputStreamReader(req.getInputStream())));

            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the server.xml/client.xml file
            Object doREST = msgContext.getProperty(Constants.Configuration.DO_REST);
            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;
            if (doREST != null && "true".equals(doREST)) {
                SOAPFactory soapFactory = new SOAP11Factory();
                builder = new StAXOMBuilder(reader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                builder = new StAXSOAPModelBuilder(reader);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }

            msgContext.setEnvelope(envelope);
            processSOAPMessage(msgContext, req, res);
        } catch (AxisFault e) {
            throw new ServletException(e);
        } catch (XMLStreamException e) {
            throw new ServletException(e);
        } catch (FactoryConfigurationError e) {
            throw new ServletException(e);
        }
    }

    public void processSOAPMessage(
        MessageContext msgContext,
        HttpServletRequest req,
        HttpServletResponse res)
        throws AxisFault {
        try {
            res.setContentType("text/xml; charset=utf-8");
            AxisEngine engine = new AxisEngine(engineContext);
            msgContext.setServerSide(true);

            String filePart = req.getRequestURL().toString();
            msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO, filePart));
            String soapActionString = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
            if (soapActionString != null) {
                msgContext.setWSAAction(soapActionString);
            }
            Utils.configureMessageContextForHTTP(
                req.getContentType(),
                soapActionString,
                msgContext);
            msgContext.setProperty(
                MessageContext.TRANSPORT_OUT,res.getOutputStream());
            engine.receive(msgContext);

            Object contextWritten = msgContext.getProperty(Constants.RESPONSE_WRITTEN);
            if (contextWritten == null || !Constants.VALUE_TRUE.equals(contextWritten)) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

}