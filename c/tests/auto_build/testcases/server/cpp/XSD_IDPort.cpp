/*
 * Copyright 2003-2004 The Apache Software Foundation.

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains definitions of the web service
 */

#include "XSD_IDPort.hpp"


XSD_IDPort::XSD_IDPort()
{
}

XSD_IDPort::~XSD_IDPort()
{
}

/* This function is called by the AxisEngine when something went wrong
 with the current web service request processing. Appropriate actions should
 be taken here.*/
void XSD_IDPort::onFault()
{
}
/* This function is called by the AxisEngine when this web service
 library is first loaded. So here we can initialize any global/static
 data structures of this web service or open database connections */
void XSD_IDPort::init()
{
}
/* This function is called by the AxisEngine when this web service
 library is unloaded. So we can deallocate any global/static data structures
 and close database connections etc here. */
void XSD_IDPort::fini()
{
}
xsd__ID XSD_IDPort::asNonNillableElement(xsd__ID Value0)  
{
	return Value0;
}
xsd__ID XSD_IDPort::asNillableElement(xsd__ID Value0)  
{
	return Value0;
}
RequiredAttributeElement* XSD_IDPort::asRequiredAttribute(RequiredAttributeElement* Value0)  
{
	return Value0;
}
OptionalAttributeElement* XSD_IDPort::asOptionalAttribute(OptionalAttributeElement* Value0)  
{
	return Value0;
}
xsd__ID_Array XSD_IDPort::asArray(xsd__ID_Array Value0)  
{
	return Value0;
}
SimpleComplexType* XSD_IDPort::asComplexType(SimpleComplexType* Value0)  
{
	return Value0;
}
