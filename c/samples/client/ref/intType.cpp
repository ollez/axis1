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
 * This file contains functions to manipulate complex type intType
 */

#include "intType.hpp"
#include <axis/AxisWrapperAPI.hpp>

/*
 * This static method serialize a intType type of object
 */
int Axis_Serialize_intType(intType* param, IWrapperSoapSerializer* pSZ, bool bArray = false)
{
	if (bArray)
	{
		pSZ->serialize("<", Axis_TypeName_intType, ">", NULL);
	}
	else
	{
		const AxisChar* sPrefix = pSZ->getNamespacePrefix(Axis_URI_intType);
		pSZ->serialize("<", Axis_TypeName_intType, " xsi:type=\"", sPrefix, ":",
			Axis_TypeName_intType, "\" xmlns:", sPrefix, "=\"",
			Axis_URI_intType, "\">", NULL);
	}

	pSZ->serializeAsElement("intItem", (void*)&(param->intItem), XSD_INT);

	pSZ->serialize("</", Axis_TypeName_intType, ">", NULL);
	return AXIS_SUCCESS;
}

/*
 * This static method deserialize a intType type of object
 */
int Axis_DeSerialize_intType(intType* param, IWrapperSoapDeSerializer* pIWSDZ)
{
	param->intItem = pIWSDZ->getElementAsInt("intItem",0);
	return pIWSDZ->getStatus();
}
void* Axis_Create_intType(intType* pObj, bool bArray = false, int nSize=0)
{
	if (bArray && (nSize > 0))
	{
		if (pObj)
		{
			intType* pNew = new intType[nSize];
			memcpy(pNew, pObj, sizeof(intType)*nSize/2);
			memset(pObj, 0, sizeof(intType)*nSize/2);
			delete [] pObj;
			return pNew;
		}
		else
		{
			return new intType[nSize];
		}
	}
	else
		return new intType;
}

/*
 * This static method delete a intType type of object
 */
void Axis_Delete_intType(intType* param, bool bArray = false, int nSize=0)
{
	if (bArray)
	{
		delete [] param;
	}
	else
	{
		delete param;
	}
}
/*
 * This static method gives the size of intType type of object
 */
int Axis_GetSize_intType()
{
	return sizeof(intType);
}

intType::intType()
{
	/*do not allocate memory to any pointer members here
	 because deserializer will allocate memory anyway. */
	memset( &intItem, 0, sizeof( xsd__int));
}

intType::~intType()
{
	/*delete any pointer and array members here*/
}
