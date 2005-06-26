package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.soap.impl.llom.SOAPBodyImpl;
import org.apache.axis.soap.impl.llom.SOAPFaultImpl;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFault;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMException;

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

public class SOAP11BodyImpl extends SOAPBodyImpl{
    /**
     * @param envelope
     */
    public SOAP11BodyImpl(SOAPEnvelope envelope) throws SOAPProcessingException {
        super(envelope);
    }

    /**
     * Constructor SOAPBodyImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAP11BodyImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        super(envelope, builder);
    }

    public SOAPFault addFault(Exception e) throws OMException  {
        SOAPFault soapFault =  new SOAP11Factory().createSOAPFault(this, e);
        return soapFault;
    }
}
