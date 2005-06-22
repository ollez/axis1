package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMException;
import org.apache.axis.om.impl.llom.serialize.StreamingOMSerializer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

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
public class OMSerializerUtil {

    /**
     * Method serializeEndpart
     *
     * @param writer
     * @throws javax.xml.stream.XMLStreamException
     */
    static void serializeEndpart(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
    }

    /**
     * Method serializeAttribute
     *
     * @param attr
     * @param writer
     * @throws XMLStreamException
     */
    static void serializeAttribute(OMAttribute attr, XMLStreamWriter writer)
            throws XMLStreamException {

        // first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        if (ns != null) {

            // add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null) {
                writer.writeAttribute(prefix, namespaceName,
                        attr.getLocalName(), attr.getValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(),
                        attr.getValue());
            }
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }
    }

    /**
     * Method serializeNamespace
     *
     * @param namespace
     * @param writer
     * @throws XMLStreamException
     */
    static void serializeNamespace(OMNamespace namespace, XMLStreamWriter writer)
            throws XMLStreamException {
        if (namespace != null) {
            String uri = namespace.getName();
            String prefix = writer.getPrefix(uri);
            String ns_prefix = namespace.getPrefix();
            if (prefix == null) {
                writer.writeNamespace(ns_prefix, namespace.getName());
                writer.setPrefix(ns_prefix, uri);
            }
        }
    }


    /**
     * Method serializeStartpart
     *
     * @param writer
     * @throws XMLStreamException
     */
    static void serializeStartpart(OMElementImpl element,XMLStreamWriter writer)
            throws XMLStreamException {
        String nameSpaceName = null;
        String writer_prefix = null;
        String prefix = null;
        if (element.ns != null) {
            nameSpaceName = element.ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = element.ns.getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName,
                            element.getLocalName());
                } else {
                    if (prefix != null) {
                        writer.writeStartElement(prefix, element.getLocalName(),
                                nameSpaceName);
                        writer.writeNamespace(prefix, nameSpaceName);
                        writer.setPrefix(prefix, nameSpaceName);
                    } else {
                        writer.writeStartElement(nameSpaceName,
                                element.getLocalName());
                        writer.writeDefaultNamespace(nameSpaceName);
                        writer.setDefaultNamespace(nameSpaceName);
                    }
                }
            } else {
                writer.writeStartElement(element.getLocalName());
//                throw new OMException(
//                        "Non namespace qualified elements are not allowed");
            }
        } else {
            writer.writeStartElement(element.getLocalName());
//            throw new OMException(
//                    "Non namespace qualified elements are not allowed");
        }

        // add the elements attributes
        serializeAttributes(element, writer);

        // add the namespaces
        serializeNamespaces(element, writer);
    }

    public static void serializeNamespaces(OMElementImpl element, XMLStreamWriter writer) throws XMLStreamException {
        Iterator namespaces = element.getAllDeclaredNamespaces();
        if (namespaces != null) {
            while (namespaces.hasNext()) {
                serializeNamespace((OMNamespace) namespaces.next(), writer);
            }
        }
    }

    public static void serializeAttributes(OMElementImpl element, XMLStreamWriter writer) throws XMLStreamException {
        if (element.getAttributes() != null) {
            Iterator attributesList = element.getAttributes();
            while (attributesList.hasNext()) {
                serializeAttribute((OMAttribute) attributesList.next(), writer);
            }
        }
    }


    /**
     * Method serializeNormal
     *
     * @param writer
     * @param cache
     * @throws XMLStreamException
     */
    static void serializeNormal(OMElementImpl element,XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        
        if (cache){
            element.build();
        }

        serializeStartpart(element,writer);
        OMNode firstChild = element.firstChild;
        if (firstChild != null) {
            if (cache){
                firstChild.serializeWithCache(writer);
            }else{
                firstChild.serialize(writer);
            }
        }
        serializeEndpart(writer);
    }

    static void serializeByPullStream(OMElementImpl element,XMLStreamWriter writer) throws XMLStreamException {
        StreamingOMSerializer streamingOMSerializer =  new StreamingOMSerializer();
        streamingOMSerializer.serialize(element.getXMLStreamReaderWithoutCaching(),
                writer);
        return;
    }
}