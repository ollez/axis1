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
package org.apache.geronimo.ews.ws4j2ee.context.webservices.server.jaxb;

import org.apache.geronimo.ews.ws4j2ee.context.webservices.server.AbstractWSCFWSDLPort;
import org.apache.geronimo.ews.ws4j2ee.context.webservices.server.interfaces.WSCFWSDLPort;

/**
 * This encapsulates the Qname wsdlport name and this is the concrete implementation of the WSCFWSDLPort
 */
public class WSCFWSDLPortImpl extends AbstractWSCFWSDLPort implements WSCFWSDLPort {
    public WSCFWSDLPortImpl(XsdQNameType jaxbWSDLPort) {
        if (null == jaxbWSDLPort) {
            return;
        }
        if (null != jaxbWSDLPort.getValue()) {
            this.localpart = jaxbWSDLPort.getValue().getLocalPart();
            this.namespaceURI = jaxbWSDLPort.getValue().getNamespaceURI();
        }
    }
}
