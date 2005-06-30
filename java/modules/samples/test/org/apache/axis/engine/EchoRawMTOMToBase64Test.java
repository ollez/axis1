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

package org.apache.axis.engine;
/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.attachments.ByteArrayDataSource;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMOutput;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EchoRawMTOMToBase64Test extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference(AddressingConstants.WSA_TO,
                    "http://127.0.0.1:"
            + (UtilServer.TESTING_PORT+1)
            + "/axis/services/EchoXMLService/echoMTOMtoBase64");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoMTOMtoBase64");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    //private Thread thisThread;
   // private SimpleHTTPServer sas;
    private ServiceContext serviceContext;
    private ServiceDescription service;

    private boolean finish = false;

    public EchoRawMTOMToBase64Test() {
        super(EchoRawMTOMToBase64Test.class.getName());
    }

    public EchoRawMTOMToBase64Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                UtilServer.getConfigurationContext().createServiceContext(service.getName());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    private OMElement createEnvelope() {
    	
    	 OMFactory fac = OMAbstractFactory.getOMFactory();
    	 OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
    	 OMElement rpcWrapEle = fac.createOMElement("methodeWrap",omNs);
		 OMElement data = fac.createOMElement("data", omNs);
		 byte[] byteArray = new byte[] { 13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
				98 };
		 DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
		 OMTextImpl textData = new OMTextImpl(dataHandler, true);
		 data.addChild(textData); 
         rpcWrapEle.addChild(data);
		 return rpcWrapEle;
    }


    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();

        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();

        call.setTo(targetEPR);
        call.set(Constants.Configuration.ENABLE_MTOM,Constants.VALUE_TRUE);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

        OMElement result =
                (OMElement) call.invokeBlocking(operationName.getLocalPart(), payload);
        result.serializeWithCache(new OMOutput(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out)));
        call.close();
    }

   /* public void testCorrectSOAPEnvelope() throws Exception {

        OMElement payload = createEnvelope();

        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

        OMElement result =
                (OMElement) call.invokeBlocking(operationName.getLocalPart(), payload);
        result.serializeWithCache(new OMOutput(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out)));
        call.close();
    }*/


}
