/* -*- C++ -*- */

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * 4. The names "SOAP" and "Apache Software Foundation" must
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
 *
 */

// IHandlerSoapSerializer.h:
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_IHANDLERSOAPSERIALIZER_H__DE308D72_BC3E_407A_9252_649D0399C90F__INCLUDED_)
#define AFX_IHANDLERSOAPSERIALIZER_H__DE308D72_BC3E_407A_9252_649D0399C90F__INCLUDED_

#include "IWrapperSoapSerializer.h"
class IHeaderBlock;
/**
    @class IHandlerSoapSerializer
    @brief interface for the IHandlerSoapSerializer class.


    @author Roshan Weerasuriya (roshan@jkcs.slt.lk, roshan@opensource.lk)
*/
class IHandlerSoapSerializer : public IWrapperSoapSerializer

{
public:		
	virtual ~IHandlerSoapSerializer() {};
	virtual IHeaderBlock* createHeaderBlock()=0;
	virtual int AXISCALL AddHeaderBlock(IHeaderBlock* pBlk)=0;
	/**
	 * A handler may get the entire soap body and encrypt/compress it and encode
	 * to either base64Binary or hexBinary before sending to the trasport. So any
	 * handler in the response message path may use following functions to get the
	 * entire soap body / set encrypted and/or compressed and then encoded soap body
	 * back to the Serializer
	 */
	virtual int AXISCALL SetBodyAsHexBinary(xsd__hexBinary body)=0;
	virtual int AXISCALL SetBodyAsBase64Binary(xsd__base64Binary body)=0;
	virtual const AxisChar* AXISCALL GetBodyAsString()=0;
};

#endif // !defined(AFX_IHANDLERSOAPSERIALIZER_H__DE308D72_BC3E_407A_9252_649D0399C90F__INCLUDED_)
