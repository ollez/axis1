package sample.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 14, 2005
 * Time: 3:13:03 PM
 */
public class GroovyReceiver
    extends AbstractInOutSyncMessageReceiver
    implements MessageReceiver {

    public void invokeBusinessLogic(
        MessageContext inMessage,
        MessageContext outMessage)
        throws AxisFault {
        try {
            ServiceDescription service =
                inMessage
                    .getOperationContext()
                    .getServiceContext()
                    .getServiceConfig();
            Parameter implInfoParam = service.getParameter("ServiceClass");
            if (implInfoParam == null) {
                throw new AxisFault(
                    Messages.getMessage("paramIsNotSpecified", "ServiceClass"));
            }
            InputStream groovyFileStream =
                this.getClass().getResourceAsStream(
                    implInfoParam.getValue().toString());

            if (groovyFileStream == null) {
                throw new AxisFault(
                    Messages.getMessage("groovyUnableToLoad", implInfoParam.getValue().toString()));
            }

            //look at the method name. if available this should be a groovy method
            OperationDescription op =
                inMessage.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault(
                    Messages.getMessage("notFound", "Operation"));
            }
            String methodName = op.getName().getLocalPart();
            OMElement firstChild =
                (OMElement) inMessage.getEnvelope().getBody().getFirstChild();
            inMessage.getEnvelope().build();
            StringWriter writer = new StringWriter();
            firstChild.build();
            firstChild.serializeWithCache(
                new OMOutputImpl(
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                        writer)));
            writer.flush();
            String value = writer.toString();
            if (value != null) {
                InputStream in = new ByteArrayInputStream(value.getBytes());
                GroovyClassLoader loader = new GroovyClassLoader();
                Class groovyClass = loader.parseClass(groovyFileStream);
                GroovyObject groovyObject =
                    (GroovyObject) groovyClass.newInstance();
                Object[] arg = { in };
                Object obj = groovyObject.invokeMethod(methodName, arg);
                if (obj == null) {
                    throw new AxisFault(Messages.getMessage("groovyNoanswer"));
                }
                
                SOAPFactory fac = null;
                if(inMessage.isSOAP11()){
                    fac = OMAbstractFactory.getSOAP11Factory();
                }else{
                    fac = OMAbstractFactory.getSOAP12Factory();
                }
                SOAPEnvelope envelope = fac.getDefaultEnvelope();
                OMNamespace ns =
                    fac.createOMNamespace("http://soapenc/", "res");
                OMElement responseElement =
                    fac.createOMElement(methodName + "Response", ns);
                String outMessageString = obj.toString();
                // System.out.println("outMessageString = " + outMessageString);
                // responseElement.setText(outMessageString);
                responseElement.addChild(getpayLoad(outMessageString));
                envelope.getBody().addChild(responseElement);
                outMessage.setEnvelope(envelope);
            }
        } catch (Exception e) {
            throw new AxisFault(e);
        } 
    }

    private OMElement getpayLoad(String str) throws XMLStreamException {
        XMLStreamReader xmlReader =
            XMLInputFactory.newInstance().createXMLStreamReader(
                new ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        StAXOMBuilder staxOMBuilder =
            new StAXOMBuilder(fac, xmlReader);
        return staxOMBuilder.getDocumentElement();
    }

}