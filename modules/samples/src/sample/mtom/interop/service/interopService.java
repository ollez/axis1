/*
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package sample.mtom.interop.service;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;

import java.util.Iterator;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public class interopService {
    public OMElement mtomSample(OMElement element) throws Exception {
        if (element.getLocalName().equalsIgnoreCase("Data")
                && element.getNamespace().getName().equalsIgnoreCase(
                        "http://example.org/mtom/data")) {
                OMText binaryNode = (OMText)element.getFirstChild();
                binaryNode.setOptimize(!binaryNode.isOptimized());
            }
         else if (element.getLocalName().equalsIgnoreCase("EchoTest") && element.getNamespace().getName().equalsIgnoreCase("http://example.org/mtom/data")) {
            Iterator childrenIterator = element.getChildren();
            while (childrenIterator.hasNext()) {
                OMElement dataElement = (OMElement) childrenIterator.next();
                OMText binaryNode = (OMText)dataElement.getFirstChild();
                binaryNode.setOptimize(!binaryNode.isOptimized());
            }
        }
        return element;
    }
}