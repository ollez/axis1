// Copyright 2003-2004 The Apache Software Foundation.
// (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved
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


#include "XSD_integer.hpp"
#include <axis/AxisException.hpp>
#include <ctype.h>
#include <iostream>

#ifdef WIN32
#ifdef Q168440_WORKAROUND
  // Bug in MS Visual C++ 6.0. Fixed in Visual C++ .Net version.
  // Cannot print an __int64 number with cout without this overloading
  std::ostream& operator<<(std::ostream& os, __int64 i )
  {
    char buf[40];
    sprintf(buf,"%I64d", i );
    os << buf;
    return os;
  }
#endif
#endif

int main(int argc, char* argv[])
{
    char endpoint[256];
    const char* url="http://localhost:80/axis/XSD_integer";

    if(argc>1)
        url = argv[1];

      // bool bSuccess = false;

    try
    {
        sprintf(endpoint, "%s", url);
        XSD_integer* ws = new XSD_integer(endpoint);

        xsd__integer result = ws->asNonNillableElement((xsd__integer)9223372036854775807);
        cout << "non-nillable element=" << result << endl;
        result = ws->asNonNillableElement((xsd__integer)1);
        cout << "non-nillable element=" << result << endl;
        result = ws->asNonNillableElement((xsd__integer)-9223372036854775808);
        cout << "non-nillable element=" << result << endl;
        result = ws->asNonNillableElement((xsd__integer)-1);
        cout << "non-nillable element=" << result << endl;
        result = ws->asNonNillableElement((xsd__integer)0);
        cout << "non-nillable element=" << result << endl;


        // Test nillable element, with a value
        xsd__integer* nillableInput = new xsd__integer();
        *(nillableInput) = (xsd__integer)123456789;
        xsd__integer* nillableResult = ws->asNillableElement(nillableInput);
        if (nillableResult)
        {
            cout << "nillable element=" << *(nillableResult) << endl;
            delete nillableResult;
        }
        else
        {
            cout << "nillable element=<nil>" << endl;
        }
        delete nillableInput;

        // Test nillable element, with nil
        nillableResult = ws->asNillableElement(NULL);
        if (nillableResult)
        {
            cout << "nil element=" << *(nillableResult) << endl;
            delete nillableResult;
        }
        else
        {
            cout << "nil element=<nil>" << endl;
        }

        // Test required attribute
        RequiredAttributeElement requiredAttributeInput;
        requiredAttributeInput.setrequiredAttribute(123456789);
        RequiredAttributeElement* requiredAttributeResult = ws->asRequiredAttribute(&requiredAttributeInput);
        cout << "required attribute=" << requiredAttributeResult->getrequiredAttribute() << endl;
        delete requiredAttributeResult;

/* Optional Attributes currently unsupported by WSDL2Ws
 * Exact coding of this section may change depending on chosen implementation
        // Test optional attribute, with a value
        OptionalAttributeElement optionalAttributeInput;
        optionalAttributeInput.setoptionalAttribute(123456789);
        OptionalAttributeElement* optionalAttributeResult = ws->asOptionalAttribute(&optionalAttributeInput);
        if (optionalAttributeResult->getoptionalAttribute())
        {
            cout << "optional attribute, with data=" << optionalAttributeResult->getoptionalAttribute() << endl;
        }
        else
        {
            cout << "optional attribute, with data=<not present>" << endl;
        }
        delete optionalAttributeResult;

        // Test optional attribute, not present
        optionalAttributeInput.setattribute();
        optionalAttributeResult = ws->asOptionalAttribute(&optionalAttributeInput);
        if (optionalAttributeResult->getoptionalAttribute())
        {
            cout << "optional attribute, not present=" << optionalAttributeResult->getoptionalAttribute() << endl;
        }
        else
        {
            cout << "optional attribute, not present=<not present>" << endl;
        }
        delete optionalAttributeResult;
*/

        // Test array
        xsd__integer_Array arrayInput;
        arrayInput.m_Array = new xsd__integer*[2];
        xsd__integer * array = new xsd__integer[2];
        arrayInput.m_Size = 2;
        for (int inputIndex=0 ; inputIndex < 2 ; inputIndex++)
        {
            array[inputIndex] = 123456789;
            arrayInput.m_Array[inputIndex] = &array[inputIndex];
        }
        xsd__integer_Array arrayResult = ws->asArray(arrayInput);
        cout << "array of " << arrayResult.m_Size << " elements" << endl;
        for (int index = 0; index < arrayResult.m_Size ; index++)
        {
            cout << "  element[" << index << "]=" << *((xsd__integer*)(arrayResult.m_Array[index])) << endl;
            delete arrayResult.m_Array[index];
        }
        delete [] array;
        delete [] arrayInput.m_Array;
        delete [] arrayResult.m_Array;

        // Test complex type
        SimpleComplexType complexTypeInput;
        complexTypeInput.setcomplexTypeElement(123456789);
        SimpleComplexType* complexTypeResult = ws->asComplexType(&complexTypeInput);
        cout << "within complex type=" << complexTypeResult->getcomplexTypeElement() << endl;
        delete complexTypeResult;

       // Tests now complete
       delete ws;
    }
    catch(AxisException& e)
    {
        cout << "Exception : " << e.what() << endl;
    }
    catch(exception& e)
    {
        cout << "Unknown exception has occured: " << e.what() << endl;
    }
    catch(...)
    {
        cout << "Unknown exception has occured" << endl;
    }

    cout<< "---------------------- TEST COMPLETE -----------------------------"<< endl;
   
    return 0;
}