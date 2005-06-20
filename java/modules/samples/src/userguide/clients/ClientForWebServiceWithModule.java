package userguide.clients;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

public class ClientForWebServiceWithModule {
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
            "http://127.0.0.1:8080/axis2/services/MyServiceWithModule/echo");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getEchoOMElement();
            Call call = new Call();
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

            //Blocking invocation
            OMElement result = (OMElement) call.invokeBlocking("echo", payload);

            StringWriter writer = new StringWriter();
            result.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
            writer.flush();

            System.out.println(writer.toString());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}