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

// Param.cpp: implementation of the Param class.
//
//////////////////////////////////////////////////////////////////////

#include <axis/engine/AxisEngine.h>
#include <axis/common/Param.h>
#include <axis/common/ArrayBean.h>
#include <axis/common/BasicTypeSerializer.h>
#include <stdlib.h>
#include <stdio.h>
#include <axis/common/AxisUtils.h>

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

int AxisEngine::m_bServer;

Param::Param(const Param& param)
{
	m_sName = param.m_sName.c_str();
	m_sValue = param.m_sValue.c_str();
	m_Type = param.m_Type;
	switch(m_Type)
	{
	case USER_TYPE: 
		{
			m_Value.pCplxObj = new ComplexObjectHandler();
			m_Value.pCplxObj->m_TypeName = param.m_Value.pCplxObj->m_TypeName.c_str();
			m_Value.pCplxObj->m_URI = param.m_Value.pCplxObj->m_URI.c_str();
		}
		break;
	case XSD_ARRAY:
		{
			m_Value.pArray = new ArrayBean();
			m_Value.pArray->m_TypeName = param.m_Value.pArray->m_TypeName.c_str();
			m_Value.pArray->m_URI = param.m_Value.pArray->m_URI.c_str();
			m_Value.pArray->m_type = param.m_Value.pArray->m_type;
			m_Value.pArray->m_size = param.m_Value.pArray->m_size;
			m_Value.pArray->m_ItemName = param.m_Value.pArray->m_ItemName.c_str();
			//copy constructor is not intended to use to copy the array in
			//union v
		}
		break;
	default: 
			m_Value = param.m_Value;
	}
}

Param::Param(const AxisChar* str, XSDTYPE type)
{
	m_sValue = str;
	m_Type = type;
	switch (type)
	{
    case XSD_DURATION: m_sName = "Duration"; break;
    case XSD_DATETIME: m_sName = "DateTime"; break;
    case XSD_TIME: m_sName = "Time"; break;
    case XSD_DATE: m_sName = "Date"; break;
    case XSD_YEARMONTH: m_sName = "YearMonth"; break;
    case XSD_YEAR: m_sName = "Year"; break;
    case XSD_MONTHDAY: m_sName = "MonthDay"; break;
    case XSD_DAY: m_sName = "Day"; break;
    case XSD_MONTH: m_sName = "Month"; break;
    case XSD_ANYURI: m_sName = "AnyURIString"; break;
    case XSD_QNAME: m_sName = "QNameString"; break;
	case XSD_STRING: m_sName = "String"; break;
	case XSD_BASE64BINARY: m_sName = "Base64BinaryString"; break;
	case XSD_HEXBINARY: m_sName = "HexBinaryString"; break;
    default:;
	}
}

Param::Param(time_t time)
{
    //m_uAxisTime = AxisTime(time);
}

Param::Param(struct tm tValue)
{
	m_sName = "DateTime";
	m_Value.tValue = tValue;
	m_Type = XSD_DATETIME;
    //m_uAxisTime = AxisTime(timeStruct);
}

Param::Param(int nValue)
{
	m_sName = "Int";
	m_Value.nValue = nValue;
	m_Type = XSD_INT;
}

Param::Param(unsigned int unValue)
{
	m_sName = "Unsigned Int";
	m_Value.unValue = unValue;
	m_Type = XSD_UNSIGNEDINT;
}

Param::Param(short sValue)
{
    m_sName = "Short";
	m_Value.sValue = sValue;
	m_Type = XSD_SHORT;	
}

Param::Param(unsigned short usValue)
{
    m_sName = "Unsigned Short";
	m_Value.usValue = usValue;
	m_Type = XSD_UNSIGNEDSHORT;
}

Param::Param(char cValue)
{
    m_sName = "Byte";
	m_Value.cValue = cValue;
	m_Type = XSD_BYTE;
}

Param::Param(unsigned char ucValue)
{
    m_sName = "Unsigned Byte";
	m_Value.ucValue = ucValue;
	m_Type = XSD_UNSIGNEDBYTE;
}

Param::Param(long lValue, XSDTYPE type)
{
    m_Type = type;
	switch (type)
	{
        case XSD_LONG:m_sName = "Long";
        case XSD_INTEGER: m_sName = "Integer";
            m_Value.lValue = lValue;
            break;
        default:;
	}
}

Param::Param(unsigned long ulValue)
{
	m_sName = "Unsigned Long";
	m_Value.ulValue = ulValue;
	m_Type = XSD_UNSIGNEDLONG;
}

Param::Param(float fValue)
{
	m_sName = "Float";
	m_Value.fValue = fValue;
	m_Type = XSD_FLOAT;
}

Param::Param(double dValue, XSDTYPE type)
{
	m_Type = type;
	switch (type)
	{
        case XSD_DOUBLE: m_sName = "Double";
        case XSD_DECIMAL: m_sName = "Decimal";
            m_Value.dValue = dValue;
            break;
        default:;
	}
	/*m_sName = "Double";
	m_Value.dValue = dValue;
	m_Type = XSD_DOUBLE;*/

}



Param::~Param()
{
	switch (m_Type){
	case XSD_ARRAY:
		if (m_Value.pArray) delete m_Value.pArray;
		break;
	case USER_TYPE:
		delete m_Value.pCplxObj;
		break;
	default:;
	}
}

int Param::GetString(const AxisChar** pValue)
{
	if (m_Type == XSD_STRING)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_STRING;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = NULL;
		return AXIS_FAIL;
	} 
}

int Param::GetString(string* pValue)
{
	if (m_Type == XSD_STRING)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_STRING;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = "";
		return AXIS_FAIL;
	} 
}

int Param::GetAnyURI(const AxisChar** pValue)
{
	if (m_Type == XSD_ANYURI)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_ANYURI;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else
	{
		*pValue = NULL;
		return AXIS_FAIL;
	}
}

int Param::GetAnyURI(string* pValue)
{
	if (m_Type == XSD_ANYURI)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_ANYURI;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else
	{
		*pValue = "";
		return AXIS_FAIL;
	}
}

int Param::GetQName(const AxisChar** pValue)
{
	if (m_Type == XSD_QNAME)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_QNAME;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else
	{
		*pValue = NULL;
		return AXIS_FAIL;
	}
}

int Param::GetQName(string* pValue)
{
	if (m_Type == XSD_QNAME)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_QNAME;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else
	{
		*pValue = "";
		return AXIS_FAIL;
	}
}

int Param::GetHexString(const AxisChar** pValue)
{
	if (m_Type == XSD_HEXBINARY)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_HEXBINARY;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = NULL;
		return AXIS_FAIL;
	}
}

int Param::GetHexString(string* pValue)
{
	if (m_Type == XSD_HEXBINARY)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_HEXBINARY;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = "";
		return AXIS_FAIL;
	}
}

int Param::GetBase64String(const AxisChar** pValue)
{
	if (m_Type == XSD_BASE64BINARY)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_BASE64BINARY;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = NULL;
		return AXIS_FAIL;
	}
}

int Param::GetBase64String(string* pValue)
{
	if (m_Type == XSD_BASE64BINARY)
	{
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else if (m_Type == XSD_UNKNOWN) //see GetInt() to see why we do this
	{
		m_Type = XSD_BASE64BINARY;
		*pValue = m_sValue.c_str();
		return AXIS_SUCCESS;
	}
	else 
	{
		*pValue = "";
		return AXIS_FAIL;
	}
}

int Param::GetInt(int* pValue)
{
	if (m_Type == XSD_INT)
	{
		*pValue = m_Value.nValue;
	}
	else if (m_Type == XSD_UNKNOWN) 
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_INT;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.nValue;
	}
	else 
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetDateTime(struct tm* pValue)
{
	if (m_Type == XSD_DATETIME)
	{
		*pValue = m_Value.tValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_DATETIME;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.tValue;
	}
	else 
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetDate(struct tm* pValue)
{
	if (m_Type == XSD_DATE)
	{
		*pValue = m_Value.tValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_DATE;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.tValue;
	}
	else 
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetTime(struct tm* pValue)
{
	if (m_Type == XSD_TIME)
	{
		*pValue = m_Value.tValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_TIME;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.tValue;
	}
	else 
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetDuration(long* pValue)
{
	if (m_Type == XSD_DURATION)
	{
		*pValue = m_Value.lDuration;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_DATETIME;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.lDuration;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetUnsignedInt(unsigned int* pValue)
{
	if (m_Type == XSD_UNSIGNEDINT)
	{
		*pValue = m_Value.unValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_UNSIGNEDINT;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.unValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetShort(short* pValue)
{
	if (m_Type == XSD_SHORT)
	{
		*pValue = m_Value.sValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_SHORT;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.sValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetUnsignedShort(unsigned short* pValue)
{
	if (m_Type == XSD_UNSIGNEDSHORT)
	{
		*pValue = m_Value.usValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_UNSIGNEDSHORT;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.usValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetByte(char* pValue)
{
	if (m_Type == XSD_BYTE)
	{
		*pValue = m_Value.cValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_BYTE;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.cValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetUnsignedByte(unsigned char* pValue)
{
	if (m_Type == XSD_UNSIGNEDBYTE)
	{
		*pValue = m_Value.ucValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_UNSIGNEDBYTE;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.ucValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetLong(long* pValue)
{
	if (m_Type == XSD_LONG)
	{
		*pValue = m_Value.lValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_LONG;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.lValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetInteger(long* pValue)
{
	if (m_Type == XSD_INTEGER)
	{
		*pValue = m_Value.lValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_INTEGER;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.lValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetUnsignedLong(unsigned long* pValue)
{
	if (m_Type == XSD_UNSIGNEDLONG)
	{
		*pValue = m_Value.ulValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		//this situation comes when the soap does not contain the type of a parameter
		//but the wrapper class method knows exactly what the type of this parameter is.
		//then the deserializer must have put the value as a string and type as XSD_UNKNOWN.
		//so convert the m_sValue in to an int and change the types etc
		m_Type = XSD_UNSIGNEDLONG;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.ulValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetFloat(float* pValue)
{
	if (m_Type == XSD_FLOAT)
	{
		*pValue = m_Value.fValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		m_Type = XSD_FLOAT;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.fValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetDouble(double* pValue)
{
	if (m_Type == XSD_DOUBLE)
	{
		*pValue = m_Value.dValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		m_Type = XSD_DOUBLE;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.dValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

int Param::GetDecimal(double* pValue)
{
	if (m_Type == XSD_DECIMAL)
	{
		*pValue = m_Value.dValue;
	}
	else if (m_Type == XSD_UNKNOWN)
	{
		m_Type = XSD_DECIMAL;
		if (AXIS_SUCCESS != SetValue(m_sValue.c_str())) return AXIS_FAIL;
		*pValue = m_Value.dValue;
	}
	else
	{
		return AXIS_FAIL;
	}
	return AXIS_SUCCESS;
}

XSDTYPE Param::GetType() const
{
	return m_Type;
}

int Param::serialize(IWrapperSoapSerializer& pSZ)
{
	AxisString ATprefix;
	switch (m_Type){
	case XSD_INT:
	case XSD_BOOLEAN:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.nValue, m_Type);
		break; 
    case XSD_UNSIGNEDINT:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.unValue, m_Type);
		break;           
    case XSD_SHORT:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.sValue, m_Type);
		break; 
    case XSD_UNSIGNEDSHORT:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.usValue, m_Type);
		break;         
    case XSD_BYTE:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.cValue, m_Type);
		break; 
    case XSD_UNSIGNEDBYTE:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.ucValue, m_Type);
		break;
    case XSD_LONG:
    case XSD_INTEGER:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.lValue, m_Type);
		break;        
    case XSD_UNSIGNEDLONG:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.ulValue, m_Type);
		break;
	case XSD_FLOAT:
		pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.fValue, m_Type);
		break;
    case XSD_DOUBLE:
    case XSD_DECIMAL:
		pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.dValue, m_Type);
		break;              
	case XSD_STRING:
		pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_sValue.c_str(), m_Type);
		break;
	case XSD_HEXBINARY:
		pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_sValue.c_str(), m_Type);
		break;
	case XSD_BASE64BINARY:
		pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_sValue.c_str(), m_Type);
		break;
	case XSD_DURATION:
        pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.lDuration, m_Type);
        break;
    case XSD_DATETIME:
    case XSD_DATE:
    case XSD_TIME:
            pSZ << pSZ.SerializeBasicType(m_sName.c_str(), m_Value.tValue, m_Type);
        break;        
	case XSD_ARRAY:
		//pSZ << "<abc:ArrayOfPhoneNumbers xmlns:abc="http://example.org/2001/06/numbers"
		//				xmlns:enc="http://www.w3.org/2001/06/soap-encoding" 
        //              enc:arrayType="abc:phoneNumberType[2]" >";
		if (!m_Value.pArray) return AXIS_FAIL; //error condition
		pSZ << "<";
		if (!m_strPrefix.empty())
		{
			pSZ << m_strPrefix.c_str() << ":" << m_sName.c_str() << " xmlns:" << m_strPrefix.c_str() << "=\"" << m_strUri.c_str() << "\"";
		}
		else
		{
			pSZ << m_sName.c_str();
		}
		//get a prefix from Serializer
		ATprefix = pSZ.getNewNamespacePrefix();

		pSZ << " xmlns:enc"; 
		pSZ << "=\"http://www.w3.org/2001/06/soap-encoding\" ";
		if (m_Value.pArray->m_type == USER_TYPE)
		{
			pSZ << "xmlns:" << ATprefix.c_str() << "=\"" << m_Value.pArray->m_URI.c_str() << "\" "; 
		}
		pSZ << "enc:arrayType=\"";
		if (m_Value.pArray->m_type == USER_TYPE)
		{
			pSZ << ATprefix.c_str() << ":" << m_Value.pArray->m_TypeName.c_str(); 
		}
		else //basic type array
		{
			pSZ << "xsd:";
			pSZ << BasicTypeSerializer::BasicTypeStr(m_Value.pArray->m_type);
		}
		{
			char Buf[10]; //maximum array dimension is 99999999
			for (list<int>::iterator it=m_Value.pArray->m_size.begin(); it!=m_Value.pArray->m_size.end(); it++)
			{
				sprintf(Buf,"[%d]", *it);
				pSZ << Buf;
			}
		}
		pSZ << "\">";
		m_Value.pArray->Serialize(pSZ); //Only serializes the inner items
		pSZ << "</";
		if (!m_strPrefix.empty())
		{
			pSZ << m_strPrefix.c_str() << ":" << m_sName.c_str(); 
		}
		else
		{
			pSZ << m_sName.c_str();
		}
		pSZ << ">";
		break;
	case USER_TYPE:
		m_Value.pCplxObj->pSZFunct(m_Value.pCplxObj->pObject, pSZ);
		break;
	default:;
	}
	return AXIS_SUCCESS;
}

////////////////////////////////////////////////////////////////////////
//This method is used by the deserializer to set the value after setting the type.
//Also this method assumes that the type is already set and it is a basic type.
int Param::SetValue(const AxisChar* sValue)
{
	AxisChar* endptr = NULL;
	if (strlen(sValue) == 0) return AXIS_FAIL;
	m_sValue = sValue; //Whatever the type we put the string representation of the value
	switch (m_Type)
	{
	case XSD_INT:
        m_Value.nValue = strtol(sValue, &endptr, 10);
		break;
    case XSD_UNSIGNEDINT:
        m_Value.nValue = strtoul(sValue, &endptr, 10);
		break;
    case XSD_SHORT:
        m_Value.sValue = strtol(sValue, &endptr, 10);
		break;
    case XSD_UNSIGNEDSHORT:
        m_Value.usValue = strtoul(sValue, &endptr, 10);
		break;
    case XSD_BYTE:
        m_Value.cValue = strtol(sValue, &endptr, 10);
		break;
    case XSD_UNSIGNEDBYTE:
		m_Value.ucValue = strtoul(sValue, &endptr, 10);
		break;              
    case XSD_LONG:
    case XSD_INTEGER:
        m_Value.lValue = strtol(sValue, &endptr, 10);
		break;                
    case XSD_UNSIGNEDLONG:
		m_Value.ulValue = strtoul(sValue, &endptr, 10);
		break;        
	case XSD_FLOAT:
		m_Value.fValue = strtod(sValue, &endptr);
		break;
    case XSD_DOUBLE:
    case XSD_DECIMAL:
		m_Value.dValue = strtod(sValue, &endptr);
		break;        
	case XSD_STRING:
	case XSD_HEXBINARY:
	case XSD_BASE64BINARY:
	case XSD_ANYURI:
	case XSD_QNAME:
	case XSD_NOTATION:			
		m_sValue = sValue;
		break;
    case XSD_DATETIME:
    case XSD_DATE:
    case XSD_TIME:
		m_Value.tValue = AxisTime::Deserialize(sValue, m_Type);
		break;        
    case XSD_DURATION:
		m_Value.lDuration = AxisTime::DeserializeDuration(sValue, m_Type);
		break;
		//Continue this for all basic types
	case XSD_ARRAY:
	case USER_TYPE:
		return AXIS_FAIL;
	default:
		return AXIS_FAIL; //this is an unexpected situation
	}
	if (endptr && (strlen(endptr) > 0)) return AXIS_FAIL; //a string to number conversion is attempted and failed
	return AXIS_SUCCESS;
}

int Param::SetValue(XSDTYPE nType, uParamValue Value)
{
	m_Type = nType;
	switch (m_Type)
	{
	case XSD_INT:
	case XSD_BOOLEAN:
        m_Value.nValue = Value.nValue;
		break;
	case XSD_UNSIGNEDINT:
        m_Value.unValue = Value.unValue;
		break;
    case XSD_SHORT:
        m_Value.sValue = Value.sValue;
		break;
	case XSD_UNSIGNEDSHORT:
        m_Value.usValue = Value.usValue;
		break;
    case XSD_BYTE:
        m_Value.cValue = Value.cValue;
		break;
	case XSD_UNSIGNEDBYTE:
        m_Value.ucValue = Value.ucValue;
		break;
    case XSD_LONG:
    case XSD_INTEGER:
        m_Value.lValue = Value.lValue;
		break;
	case XSD_UNSIGNEDLONG:
        m_Value.ulValue = Value.ulValue;
		break;
	case XSD_FLOAT:
        m_Value.fValue = Value.fValue;
		break;
    case XSD_DOUBLE:
    case XSD_DECIMAL:
		m_Value.dValue = Value.dValue;
		break;        
	case XSD_STRING:
	case XSD_HEXBINARY:
	case XSD_BASE64BINARY:
		m_sValue = Value.pStrValue;
		break;
  case XSD_DURATION:
        m_Value.lDuration = Value.lDuration;
    case XSD_DATETIME:
    case XSD_DATE:
    case XSD_TIME:
        m_Value.tValue = Value.tValue;
    //    m_uAxisTime.setValue(nType, m_Value);
	//Continue this for all basic types
	case XSD_ARRAY:
		m_Value.pArray = Value.pArray;
		break;
	case USER_TYPE:
		m_Value.pCplxObj = Value.pCplxObj;
		break;
	default:
		return AXIS_FAIL; //this is an unexpected situation
	}
	return AXIS_SUCCESS;
}

void Param::setPrefix(const AxisChar* prefix)
{
	m_strPrefix = prefix;
}

void Param::setUri(const AxisChar* uri)
{
	m_strUri = uri;
}

void Param::operator=(const Param &param)
{
	m_sName = param.m_sName;
	m_sValue = param.m_sValue;
	m_Type = param.m_Type;	
	if (m_Type == USER_TYPE) 
	{
		m_Value.pCplxObj = param.m_Value.pCplxObj;
	}
	else if(m_Type == XSD_ARRAY)
	{
		m_Value.pArray = param.m_Value.pArray;
	}	
	else 
	{
		m_Value = param.m_Value;
	}
}

int Param::GetArraySize(int* pValue)
{
	if (m_Type != XSD_ARRAY) return AXIS_FAIL;
	*pValue = m_Value.pArray->GetArraySize();
	return AXIS_SUCCESS;
}

/*
int Param::SetUserType(IAccessBean* pObject)
{
	if (m_Type != USER_TYPE) return AXIS_FAIL;
	m_Value.pIBean = pObject;
	return AXIS_SUCCESS;
}
*/

int Param::SetUserType(void* pObject, AXIS_DESERIALIZE_FUNCT pDZFunct, AXIS_OBJECT_DELETE_FUNCT pDelFunct)
{
	if (m_Type != USER_TYPE) return AXIS_FAIL;
	m_Value.pCplxObj = new ComplexObjectHandler;
	m_Value.pCplxObj->pObject = pObject;
	m_Value.pCplxObj->pDZFunct = pDZFunct;
	m_Value.pCplxObj->pDelFunct = pDelFunct;
	return AXIS_SUCCESS;
}

int Param::SetArrayElements(void* pElements)
{
	if (m_Type != XSD_ARRAY) return AXIS_FAIL;
	if (m_Value.pArray)
	{
		if (m_Value.pArray->m_type != USER_TYPE)
		{
			m_Value.pArray->m_value.sta = pElements;
			return AXIS_SUCCESS;
		}
		else //unexpected situation
		{
			return AXIS_FAIL;
		}
	}
	return AXIS_FAIL;
}

//following function is called to set array of user types.
int Param::SetArrayElements(void* pObject, AXIS_DESERIALIZE_FUNCT pDZFunct, AXIS_OBJECT_DELETE_FUNCT pDelFunct, AXIS_OBJECT_SIZE_FUNCT pSizeFunct)
{
	if (m_Type != XSD_ARRAY) return AXIS_FAIL;
	if (m_Value.pArray)
	{
		if (m_Value.pArray->m_type == USER_TYPE)
		{
			m_Value.pArray->m_value.cta = new ComplexObjectHandler;
			m_Value.pArray->m_value.cta->pDZFunct = pDZFunct;
			m_Value.pArray->m_value.cta->pDelFunct = pDelFunct;
			m_Value.pArray->m_value.cta->pSizeFunct = pSizeFunct;
			m_Value.pArray->m_value.cta->pObject = pObject;
			return AXIS_SUCCESS;
		}
		else //unexpected situation
		{
			return AXIS_FAIL;
		}
	}
	return AXIS_FAIL;	
}

void Param::SetName(const AxisChar* sName)
{
	m_sName = sName;
}

// ComplexObjectHandler functions
ComplexObjectHandler::ComplexObjectHandler()
{
	Init();
}

ComplexObjectHandler::~ComplexObjectHandler()
{
	/* At client side we do not delete either output or return objects */
	if (AxisEngine::m_bServer) 
	{
		if (pObject && pDelFunct) pDelFunct(pObject);
	}
}

void ComplexObjectHandler::Init()
{
	pObject = NULL;
	pSZFunct = NULL;
	pDelFunct = NULL; 
	pDZFunct = NULL;
	pSizeFunct = NULL;
	m_TypeName = "";
	m_URI = "";	
}

const AxisString& Param::GetTypeName()
{
	return m_Value.pCplxObj->m_TypeName;		
}
