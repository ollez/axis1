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

///////////////////////////////////////////////////////////////////////////////////
//This file is automatically generated by the Axis C++ Wrapper Class Generator
//Web service wrapper class's implementation generated by Axis WCG
//Parameters and wrapper methos to manipulate SOAPStruct
////////////////////////////////////////////////////////////////////////////////////

#include "SOAPStruct.h"
#include <axis/AxisWrapperAPI.hpp>

/////////////////////////////////////////////////////////////////////////////
// This static method serialize a SOAPStruct type of object
//////////////////////////////////////////////////////////////////////
int Axis_Serialize_SOAPStruct(SOAPStruct* param, IWrapperSoapSerializer* pSZ, bool bArray = false)
{
	/* first serialize attributes if any*/
	pSZ->serialize(">", 0);
	/* then serialize elements if any*/
	pSZ->serializeAsElement("varString", (void*)&(param->varString), XSD_STRING);
	pSZ->serializeAsElement("varInt", (void*)&(param->varInt), XSD_INT);
	pSZ->serializeAsElement("varFloat", (void*)&(param->varFloat), XSD_FLOAT);
	return AXIS_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////
// This static method deserialize a SOAPStruct type of object
//////////////////////////////////////////////////////////////////////
int Axis_DeSerialize_SOAPStruct(SOAPStruct* param, IWrapperSoapDeSerializer *pIWSDZ)
{
	param->varString = pIWSDZ->getElementAsString("varString",0);
	param->varInt = pIWSDZ->getElementAsInt("varInt",0);
	param->varFloat = pIWSDZ->getElementAsFloat("varFloat",0);
	return pIWSDZ->getStatus();
}
void* Axis_Create_SOAPStruct(void* pObj, bool bArray = false, int nSize=0)
{
	if (bArray && (nSize > 0))
	{
		if (pObj)
		{
			SOAPStruct* pNew = new SOAPStruct[nSize];
			memcpy(pNew, pObj, sizeof(SOAPStruct)*nSize/2);
			memset(pObj, 0, sizeof(SOAPStruct)*nSize/2);
			delete [] ((SOAPStruct*)pObj);
			return pNew;
		}
		else
		{
			return new SOAPStruct[nSize];
		}
	}
	else
		return new SOAPStruct;
}

SOAPStruct::~SOAPStruct()
{
	free(varString);
}

SOAPStruct::SOAPStruct()
{
	varString = 0;
}

/////////////////////////////////////////////////////////////////////////////
// This static method delete a SOAPStruct type of object
//////////////////////////////////////////////////////////////////////
void Axis_Delete_SOAPStruct(SOAPStruct* param, bool bArray = false, int nSize=0)
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
/////////////////////////////////////////////////////////////////////////////
// This static method gives the size of SOAPStruct type of object
//////////////////////////////////////////////////////////////////////
int Axis_GetSize_SOAPStruct()
{
	return sizeof(SOAPStruct);
}
