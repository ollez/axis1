/* -*- C++ -*- */
/*
 *   Copyright 2003-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *
 * @author Adrian Dick (adrian.dick@uk.ibm.com)
 *
 */

#if !defined(_BASE64BINARY_HPP____OF_AXIS_INCLUDED_)
#define _BASE64BINARY_HPP____OF_AXIS_INCLUDED_

#include "IAnySimpleType.hpp"
#include "../apr_base64.h"
#include <axis/AxisUserAPI.hpp>

AXIS_CPP_NAMESPACE_START

using namespace std;

class Base64Binary : public IAnySimpleType {
public:

	/**
	 * Serialize value to it's on-the-wire string form.
	 * @param value The value to be serialized.
	 * @return Serialized form of value.
	 */
    AxisChar* serialize(const void* value) throw (AxisSoapException);
	
	/**
	 * Deserialize value from it's on-the-wire string form.
	 * @param valueAsChar Serialized form of value.
	 * @return Deserialized value.
	 */
    void* deserialize(const AxisChar* valueAsChar) throw (AxisSoapException);
	
	/**
	 * Serialize Base64Binary value to it's on-the-wire string form.
	 * @param value The Base64Binary value to be serialized.
	 * @return Serialized form of Base64Binary value.
	 */
    AxisChar* serialize(const xsd__base64Binary* value) throw (AxisSoapException);
	
	/**
	 * Deserialized Base64Binary value from it's on-the-wire string form.
	 * @param valueAsChar Serialized form of Base64Binary value.
	 * @return Deserialized Base64Binary value.
	 */
    xsd__base64Binary* deserializeBase64Binary(const AxisChar* valueAsChar) throw (AxisSoapException);

private:
	AxisChar* m_Buf;
	xsd__base64Binary* m_base64Binary;
};

AXIS_CPP_NAMESPACE_END

#endif