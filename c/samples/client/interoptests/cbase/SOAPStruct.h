// Copyright 2003-2004 The Apache Software Foundation.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//        http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains functions to manipulate complex type SOAPStruct
 */

#if !defined(__SOAPSTRUCT_H__INCLUDED_)
#define __SOAPSTRUCT_H__INCLUDED_

#include <axis/server/AxisUserAPI.h>

/*Local name and the URI for the type*/
static const char* Axis_URI_SOAPStruct = "http://soapinterop.org/xsd";
static const char* Axis_TypeName_SOAPStruct = "SOAPStruct";

typedef struct SOAPStructTag {
	xsd__string varString;
	int varInt;
	float varFloat;
} SOAPStruct;

#endif /* !defined(__SOAPSTRUCT_H__INCLUDED_)*/
