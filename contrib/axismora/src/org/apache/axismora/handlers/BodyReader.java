/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 *    Apache Software Foundation (http://www.apache.org/)."
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

package org.apache.axismora.handlers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axismora.MessageContext;
import org.apache.axismora.util.AxisUtils;

import org.apache.axis.AxisFault;
import org.apache.axis.encoding.SerializationContextImpl;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.message.SOAPHeaderElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * This is a example of how to read the SOAP message body frpm a handler
 * @author Dimuthu Leelarathne. (muthulee@opensource.lk)
*/

public class BodyReader extends BasicHandler {
    XmlPullParser xpp;
    int state;
    MessageContext msData;

    public void invoke(MessageContext msgdata) {
        try {
            this.msData = msgdata;
            Writer w = new java.io.PrintWriter(System.out);
            new SerializationContextImpl(w).writeDOMElement(
                AxisUtils.make(msgdata.getBodyParser()));
            w.flush();
            ArrayList headers =
                this.msData.getSOAPHeaders(
                    "copyHeaderToBody",
                    new QName("opensource.lk", "acsessbody"));
            if (headers.size() == 0) {
                msgdata.setSoapFault(new SOAPFault(new AxisFault("headerNotFound")));
            }

            SOAPHeaderElement header = (SOAPHeaderElement) headers.get(0);
            msData.resetBody((String) header.getObjectValue());
        } catch (IOException e) {
            e.printStackTrace(); 
            msgdata.setSoapFault(
                new SOAPFault(
                    AxisUtils.getTheAixsFault(
                        org.apache.axis.Constants.FAULT_SOAP12_RECEIVER,
                        e.getMessage(),
                        null,
                        this.getName(),
                        e)));
        }
    }

    /* (non-Javadoc)
     * @see lk.opensource.service.Handler#getName()
     */
    public String getName() {
        return "copyHeaderToBody";
    }
}
