package org.apache.axis2.soap.impl.llom.soap11;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMSerializerUtil;
import org.apache.axis2.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.impl.llom.SOAPFaultCodeImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class SOAP11FaultCodeImpl extends SOAPFaultCodeImpl {
    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAP11FaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * @param parent
     * @param parent
     */
    public SOAP11FaultCodeImpl(SOAPFault parent) throws SOAPProcessingException {
        super(parent, false);
    }


    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!(subCode instanceof SOAP11FaultSubCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Sub Code. But received some other implementation");
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        if (!(value instanceof SOAP11FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Value. But received some other implementation");
        }
        super.setValue(value);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }

    protected void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput, boolean cache) throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(omOutput));
        }

        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (this.getNamespace() != null) {
            String prefix = this.getNamespace().getPrefix();
            String nameSpaceName = this.getNamespace().getName();
            writer.writeStartElement(prefix, SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME,
                    nameSpaceName);
        } else {
            writer.writeStartElement(
                    SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME);
        }

        OMSerializerUtil.serializeAttributes(this, omOutput);
        OMSerializerUtil.serializeNamespaces(this, omOutput);


        String text = this.getValue().getText();
        writer.writeCharacters(text);
        writer.writeEndElement();

//        //serilize siblings
//        if (this.nextSibling != null) {
//            nextSibling.serialize(omOutput);
//        } else if (this.parent != null) {
//            if (!this.parent.isComplete()) {
//                builder.setCache(cache);
//                builder.next();
//            }
//        }

    }


}