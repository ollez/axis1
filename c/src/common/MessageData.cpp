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
 *
 *
 * @author Susantha Kumara (skumara@virtusa.com)
 *
 */

// MessageData.cpp: implementation of the MessageData class.
//
//////////////////////////////////////////////////////////////////////

#include <axis/server/MessageData.h>
#include "IDeployerUtils.h"
#include "../wsdd/WSDDDeployment.h"
//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

const AxisChar* MessageData::m_pachBlankPropertyValue = "";
extern WSDDDeployment* g_pWSDDDeployment;
IMessageDataFunctions IMessageData::ms_VFtable;

MessageData::MessageData()
{
	m_pSZ = NULL;
	m_pDZ = NULL;
	m_bPastPivotState= false;
}

MessageData::~MessageData()
{
	map <AxisChar*, AxisChar*, ltstr>::iterator itCurrentItem= m_Properties.begin();

	while (itCurrentItem != m_Properties.end()) {
		free((*itCurrentItem).first);
		free((*itCurrentItem).second);

		itCurrentItem++;
	}

	m_Properties.clear();
}

void MessageData::SetSerializer(SoapSerializer *pSZ)
{
	m_pSZ = pSZ;
}

void MessageData::SetDeSerializer(SoapDeSerializer *pDZ)
{
	m_pDZ = pDZ;
}

void MessageData::SetUserName(string& sUserName)
{
  m_sUserName = sUserName;
}
string& MessageData::GetUserName()
{
  return m_sUserName;
 // return "damitha kumarage"; 
}

void MessageData::SetService(const WSDDService* argService)
{
  m_Service = argService;
}

const WSDDService* MessageData::GetService()
{
  return m_Service;

}

/*
comm on 25Jul2003 at 10.00am
ISoapSerializer* MessageData::getSoapSerializer()
{
	return m_pSZ;
}
*/

IWrapperSoapDeSerializer* MessageData::getSoapDeserializer()
{
	return m_pDZ;
}

bool MessageData::isPastPivot()
{
	return m_bPastPivotState;
}

int MessageData::setPastPivotState(bool bState)
{
	m_bPastPivotState = bState;

	return AXIS_SUCCESS;
}

void MessageData::getSoapSerializer(IHandlerSoapSerializer **pIHandlerSoapSerializer)
{
	*pIHandlerSoapSerializer = static_cast<IHandlerSoapSerializer*>(m_pSZ);
}

void MessageData::GetSoapSerializer(IWrapperSoapSerializer **pIWrapperSoapSerializer)
{
	*pIWrapperSoapSerializer = static_cast<IWrapperSoapSerializer*>(m_pSZ);
}

void MessageData::GetSoapDeSerializer(IWrapperSoapDeSerializer **pIWrapperSoapDeSerializer)
{
	*pIWrapperSoapDeSerializer= static_cast<IWrapperSoapDeSerializer*>(m_pDZ);
}

void MessageData::getSoapDeSerializer(IHandlerSoapDeSerializer **pIHandlerSoapDeSerializer)
{
	*pIHandlerSoapDeSerializer= static_cast<IHandlerSoapDeSerializer*>(m_pDZ);
}

int MessageData::setProperty(AxisChar* pachName, const AxisChar* pachValue)
{
	AxisChar* pachTmpName = (AxisChar*) malloc(strlen(pachName)+1);
	strcpy(pachTmpName, pachName);
	AxisChar* pachTmpValue = (AxisChar*) malloc(strlen(pachValue)+1);
	strcpy(pachTmpValue, pachValue);

	m_Properties[pachTmpName]= pachTmpValue;

	return AXIS_SUCCESS;
}

/*
 *The method caller has to check whether the returned string is empty or
 * not. If it is empty then the idea is that the property is not 
 * available.
 */
const AxisChar* MessageData::getProperty(AxisChar* pachName)
{	
	if (m_Properties.find(pachName) != m_Properties.end())
	{
		return m_Properties[pachName];
	}
	
	return m_pachBlankPropertyValue;
}

void MessageData::getWSDDDeployment(IDeployerUtils **pIDeployerUtils)
{
	*pIDeployerUtils= static_cast<IDeployerUtils*>(g_pWSDDDeployment);
}
