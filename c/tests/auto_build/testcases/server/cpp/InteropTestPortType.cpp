/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains definitions of the web service
 */

#include "InteropTestPortType.h"


InteropTestPortType::InteropTestPortType()
{
}

InteropTestPortType::~InteropTestPortType()
{
}

/* This function is called by the AxisEngine when something went wrong
 with the current web service request processing. Appropriate actions should
 be taken here.*/
void InteropTestPortType::onFault()
{
}
/* This function is called by the AxisEngine when this web service
 library is first loaded. So here we can initialize any global/static
 data structures of this web service or open database connections */
void InteropTestPortType::init()
{
}
/* This function is called by the AxisEngine when this web service
 library is unloaded. So we can deallocate any global/static data structures
 and close database connections etc here. */
void InteropTestPortType::fini()
{
}
xsd__string InteropTestPortType::echoString(xsd__string Value0)  
{	return Value0;
}
xsd__string_Array InteropTestPortType::echoStringArray(xsd__string_Array Value0)  
{	return Value0;
}
int InteropTestPortType::echoInteger(int Value0)  
{	return Value0;
}
xsd__int_Array InteropTestPortType::echoIntegerArray(xsd__int_Array Value0)  
{	return Value0;
}
float InteropTestPortType::echoFloat(float Value0)  
{	return Value0;
}
xsd__float_Array InteropTestPortType::echoFloatArray(xsd__float_Array Value0)  
{	return Value0;
}
SOAPStruct* InteropTestPortType::echoStruct(SOAPStruct* Value0)  
{	return Value0;
}
SOAPStruct_Array InteropTestPortType::echoStructArray(SOAPStruct_Array Value0)  
{	return Value0;
}
void InteropTestPortType::echoVoid()  
{
}
xsd__base64Binary InteropTestPortType::echoBase64(xsd__base64Binary Value0)  
{	return Value0;
}
xsd__dateTime InteropTestPortType::echoDate(xsd__dateTime Value0)  
{	return Value0;
}
xsd__hexBinary InteropTestPortType::echoHexBinary(xsd__hexBinary Value0)  
{	return Value0;
}
xsd__decimal InteropTestPortType::echoDecimal(xsd__decimal Value0)  
{	return Value0;
}
xsd__boolean InteropTestPortType::echoBoolean(xsd__boolean Value0)  
{	return Value0;
}