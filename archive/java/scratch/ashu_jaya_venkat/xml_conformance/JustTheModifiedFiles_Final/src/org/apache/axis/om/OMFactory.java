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
package org.apache.axis.om;

import javax.xml.namespace.QName;
import org.apache.axis.om.impl.llom.OMDocument;
//Actually importing an implemenation class in an api abstract class definition is not good
//But as of now OMDocument has no corresponding API interface defined for it, till then
//we would have this line here.


/**
 * Class OMFactory
 */
public interface OMFactory {
    /**
     * @param localName
     * @param ns
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns);

    /**
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                              OMElement parent,
                                              OMXMLParserWrapper builder);

    /**
     * This is almost the same as as createOMElement(localName,OMNamespace) method above.
     * But some people may, for some reason, need to use the conventional method of putting a namespace.
     * Or in other words people might not want to use the new OMNamespace.
     * Well, this is for those people.
     *
     * @param localName
     * @param namespaceURI
     * @param namespacePrefix
     * @return
     */
    public OMElement createOMElement(String localName,
                                              String namespaceURI,
                                              String namespacePrefix);

    /**
     * QName(localPart),
     * QName(namespaceURI, localPart) - a prefix will be assigned to this
     * QName(namespaceURI, localPart, prefix)
     *
     * @param qname
     * @param parent
     * @return
     * @throws OMException
     */
    public OMElement createOMElement(QName qname, OMElement parent)
            throws OMException;

    /**
     * @param uri
     * @param prefix
     * @return
     */
    public OMNamespace createOMNamespace(String uri, String prefix);

    /**
     * @param parent
     * @param text
     * @return
     */
    public OMText createText(OMElement parent, String text);

    /**
     * @param s
     * @return
     */
    public OMText createText(String s);

    public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value);

    /**
     * Method createOMDTD
     * Creates a DTD node setting its parent as <code>parent</code> and value
     * as <code>content</code>
     *
     * @param parent
     * @param content
     * @return
     */
    public abstract OMDTD createOMDTD(OMDocument parent, String content);

    /**
     * Method createOMPI
     * Creates a DTD node setting its parent as <code>parent</code> and piTarget
     * and piData values intialized to the provided input params
     * @param parent
     * @param piTarget
     * @param piData
     * @return
     */
    public abstract OMPI createOMPI(OMElement parent, String piTarget, String piData);

    /**
     * Method createOMComment
     * creates a OMComment node setting its parent as <code>parent</code> and value
     * to <code>content</code>
     * @param parent
     * @param content
     * @return
     */
    public abstract OMComment createOMComment(OMElement parent, String content);



    // make the constructor protected

}