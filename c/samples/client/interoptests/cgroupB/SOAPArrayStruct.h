/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains functions to manipulate complex type SOAPArrayStruct
 */

#if !defined(__SOAPARRAYSTRUCT_H__INCLUDED_)
#define __SOAPARRAYSTRUCT_H__INCLUDED_

#include <axis/server/AxisUserAPI.h>

/*Local name and the URI for the type*/
static const char* Axis_URI_SOAPArrayStruct = "http://soapinterop.org/xsd";
static const char* Axis_TypeName_SOAPArrayStruct = "SOAPArrayStruct";

typedef struct SOAPArrayStructTag {
	xsd__string varString;
	int varInt;
	float varFloat;
	xsd__string_Array varArray;
} SOAPArrayStruct;

#endif /* !defined(__SOAPARRAYSTRUCT_H__INCLUDED_)*/
