/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
package org.apache.axis.soap;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;

/**
 * SOAP 1.1 constants
 *
 * @author Glen Daniels (gdaniels@apache.org)
 * @author Andras Avar (andras.avar@nokia.com)
 */
public class SOAP11Constants implements SOAPConstants {
    private static QName headerQName = new QName(Constants.URI_SOAP11_ENV,
                                                 Constants.ELEM_HEADER);
    private static QName bodyQName = new QName(Constants.URI_SOAP11_ENV,
                                               Constants.ELEM_BODY);
    private static QName faultQName = new QName(Constants.URI_SOAP11_ENV,
                                                Constants.ELEM_FAULT);
    private static QName roleQName = new QName(Constants.URI_SOAP11_ENV,
                                                Constants.ATTR_ACTOR);

    public String getEnvelopeURI() {
        return Constants.URI_SOAP11_ENV;
    }

    public String getEncodingURI() {
        return Constants.URI_SOAP11_ENC;
    }

    public QName getHeaderQName() {
        return headerQName;
    }

    public QName getBodyQName() {
        return bodyQName;
    }

    public QName getFaultQName() {
        return faultQName;
    }

    /**
     * Obtain the QName for the role attribute (actor/role)
     */
    public QName getRoleAttributeQName() {
        return roleQName;
    }

    /**
     * Obtain the MIME content type
     */
    public String getContentType() {
        return "text/xml";
    }

    /**
     * Obtain the "next" role/actor URI
     */
    public String getNextRoleURI() {
        return Constants.URI_SOAP11_NEXT_ACTOR;
    }

    /**
     * Obtain the ref attribute name
     */
    public String getAttrHref() {
        return Constants.ATTR_HREF;
    }

    /**
     * Obtain the item type name of an array
     */
    public String getAttrItemType() {
        return Constants.ATTR_ARRAY_TYPE;
    }

    /**
     * Obtain the Qname of VersionMismatch fault code
     */
    public QName getVerMismatchFaultCodeQName() {
        return Constants.FAULT_VERSIONMISMATCH;
    }

    /**
     * Obtain the Qname of Mustunderstand fault code
     */
    public QName getMustunderstandFaultQName() {
        return Constants.FAULT_MUSTUNDERSTAND;
    }

    /**
     * Obtain the QName of the SOAP array type
     */
    public QName getArrayType() {
        return Constants.SOAP_ARRAY;
    }
}
