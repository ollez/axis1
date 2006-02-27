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

#include <stdio.h>
#include "LargeReturningString.hpp"

LargeReturningString::LargeReturningString()
{
//	printf( "LargeReturningString::LargeReturningString\n");
	myString = NULL;
}

LargeReturningString::~LargeReturningString()
{
//	printf( "LargeReturningString::~LargeReturningString\n");
	delete myString;
}

/* This function is called by the AxisEngine when something went wrong
 with the current web service request processing. Appropriate actions should
 be taken here.*/
void LargeReturningString::onFault()
{
}

xsd__string LargeReturningString::getLargeString(xsd__int Value0)  
{
//	printf( ">LargeReturningString::getLargeString(%d)\n", Value0);

	if( myString != NULL)
	{
		delete myString;
	}

	myString = new char[Value0 + 1];

	for( int iCount = 0; iCount < Value0; iCount++)
	{
		myString[iCount] = iCount % 26 + 'a';
	}

	myString[Value0] = '\0';

	return myString;
}

xsd__int LargeReturningString::setLargeString(xsd__string Value0)  
{
//	printf( "LargeReturningString::setLargeString\n");

	if( myString != NULL)
	{
		delete myString;
	}

	int	iSize = strlen( Value0) + 1;

	myString = new char[iSize];

	strcpy( myString, Value0);

	return iSize;
}

xsd__string LargeReturningString::setRoundTripLargeString( xsd__string Value0, xsd__int Value1)
{
//	printf( "LargeReturningString::setRoundTripLargeString\n");

	if( myString != NULL)
	{
		delete myString;
	}

	myString = new char[Value1 + 1];

	strcpy( myString, Value0);

	return myString;
}

