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
 *
 */

// IHeaderBlock.h:
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_IHEADERBLOCK_H__1FFF90C8_3E12_4EFD_8D97_61E2E92A0BB7__INCLUDED_)
#define AFX_IHEADERBLOCK_H__1FFF90C8_3E12_4EFD_8D97_61E2E92A0BB7__INCLUDED_

#include "../soap/BasicNode.h"
#include "../soap/SoapEnvVersions.h"
#include "../soap/Attribute.h"

enum HEADER_BLOCK_STD_ATTR_TYPE { ROLE_NEXT=1, ROLE_NONE=2, 
	ROLE_ULTIMATE_RECEIVER=3, ACTOR=4, MUST_UNDERSTAND_TRUE= 5, 
	MUST_UNDERSTAND_FALSE=6};


/**
    @class IHeaderBlock
    @brief interface for the IHeaderBlock class.


    @author Roshan Weerasuriya (roshan@jkcs.slt.lk, roshan@opensource.lk)
*/
class IHeaderBlock
{
public:
	virtual BasicNode* getFirstChild() =0;
	/**
	 * Returns the number of child elements of this HeaderBlock.
	 * @return The number of child elements of this HeaderBlock.
	 */
	virtual int getNoOfChildren() =0;
	virtual BasicNode* createChild(NODE_TYPE eNODE_TYPE,  AxisChar *pachLocalName, AxisChar *pachPrefix, AxisChar *pachUri, AxisChar* pachValue) = 0;
	virtual BasicNode* createImmediateChild(NODE_TYPE eNODE_TYPE, AxisChar *pachLocalName, AxisChar *pachPrefix, AxisChar *pachUri, AxisChar* pachValue) = 0;
	virtual Attribute* createStdAttribute(HEADER_BLOCK_STD_ATTR_TYPE eStdAttrType, SOAP_VERSION eSOAP_VERSION) =0;
	virtual Attribute* createAttribute(const AxisChar* localname, const AxisChar* prefix, const AxisChar* uri, const AxisChar* value) = 0;
	virtual Attribute* createAttribute(const AxisChar *localname, const AxisChar *prefix, const AxisChar *value) = 0;

	virtual BasicNode* createImmediateChild(NODE_TYPE eNODE_TYPE) = 0;
	virtual BasicNode* createChild(NODE_TYPE eNODE_TYPE)=0;

	/**
	 * Returns the last child element. The user has to check whether the
	 *  method return NULL before proceding.
	 * @return The last child element is returned if it exists. If the child element 
	 *  doesn't exsist this method returns NULL.
	 */
	virtual BasicNode* getLastChild() = 0;

	/**
	 * Returns the child element at the given postion. The user has to check whether the
	 *  method return NULL before proceding.
	 * @param iChildPosition The positon of the required child element.
	 * @return The required child element is returned if it exists. If the child element 
	 *  doesn't exsist this method returns NULL.
	 */
	virtual BasicNode* getChild(int iChildPosition) = 0;

	virtual int addChild(BasicNode* pBasicNode)=0;
	virtual void setLocalName(const AxisChar* localname)=0;
	virtual void setUri(const AxisChar* uri)=0;
	virtual void setPrefix(const AxisChar* prefix)=0;
	virtual int initializeForTesting() = 0;
	virtual ~IHeaderBlock() {};
};

#endif // !defined(AFX_IHEADERBLOCK_H__1FFF90C8_3E12_4EFD_8D97_61E2E92A0BB7__INCLUDED_)
