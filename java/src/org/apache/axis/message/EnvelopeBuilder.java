/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.axis.message;

/**
 * 
 * @author Glen Daniels (gdaniels@allaire.com)
 */

import org.apache.axis.Constants;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.utils.JavaUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.rpc.namespace.QName;

public class EnvelopeBuilder extends SOAPHandler
{
    private MessageElement element;
    private SOAPEnvelope envelope;
    
    private boolean gotHeader = false;
    private boolean gotBody = false;
    
    private static final QName headerQName = new QName(Constants.URI_SOAP_ENV,
                                                       Constants.ELEM_HEADER);
    private static final QName bodyQName = new QName(Constants.URI_SOAP_ENV,
                                                       Constants.ELEM_BODY);
    
    public EnvelopeBuilder(String messageType)
    {
        envelope = new SOAPEnvelope(false);
        envelope.setMessageType(messageType);
        myElement = envelope;
    }
    
    public EnvelopeBuilder(SOAPEnvelope env, String messageType)
    {
        envelope = env ;
        envelope.setMessageType(messageType);
        myElement = envelope;
    }
    
    public SOAPEnvelope getEnvelope()
    {
        return envelope;
    }
    
    public void startElement(String namespace, String localName,
                             String qName, Attributes attributes,
                             DeserializationContext context)
        throws SAXException
    {
        if (!namespace.equals(Constants.URI_SOAP_ENV))
            throw new SAXException(
                    JavaUtils.getMessage("badNamespace00", namespace));
        
        if (!localName.equals(Constants.ELEM_ENVELOPE))
            throw new SAXException(
                    JavaUtils.getMessage("badTag00", localName));

        String prefix = "";
        int idx = qName.indexOf(":");
        if (idx > 0)
            prefix = qName.substring(0, idx);

        envelope.setPrefix(prefix);
        envelope.setNamespaceURI(namespace);
        envelope.setNSMappings(context.getCurrentNSMappings());
        context.pushNewElement(envelope);
    }
    
    public SOAPHandler onStartChild(String namespace,
                                    String localName,
                                    String prefix,
                                    Attributes attributes,
                                    DeserializationContext context)
        throws SAXException
    {
        QName thisQName = new QName(namespace, localName);
        if (thisQName.equals(headerQName)) {
            if (gotHeader)
                throw new SAXException(JavaUtils.getMessage("only1Header00"));
            
            gotHeader = true;
            return new HeaderBuilder(envelope);
        }
        
        if (thisQName.equals(bodyQName)) {
            if (gotBody)
                throw new SAXException(JavaUtils.getMessage("only1Body00"));
            
            gotBody = true;
            return new BodyBuilder(envelope);
        }
        
        if (!gotBody)
            throw new SAXException(JavaUtils.getMessage("noCustomElems00"));

        /*
        element = new MessageElement(namespace, localName, prefix,
                                     attributes, context);
        
        if (element.getFixupDeserializer() != null)
            return element.getFixupDeserializer();
        */
        
        return null;
    }
    
    public void onEndChild(String namespace, String localName,
                           DeserializationContext context)
    {
        if (element != null) {
            envelope.addTrailer(element);
        }
    }

    public void endElement(String namespace, String localName,
                           DeserializationContext context)
        throws SAXException
    {
        // Envelope isn't dirty yet by default...
        envelope.setDirty(false);
    }
}
