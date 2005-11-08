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


#include "XSD_IDPort.hpp"
#include <axis/AxisException.hpp>
#include <ctype.h>
#include <iostream>


int main(int argc, char* argv[])
{
    char endpoint[256];
    const char* url="http://localhost:80/axis/XSD_IDPort";

    if(argc>1)
        url = argv[1];

    try
    {
        sprintf(endpoint, "%s", url);
        XSD_IDPort* ws = new XSD_IDPort(endpoint);
      
        char emptyID[1] = "";
        xsd__ID emptyInput = new char[1];
        strcpy (emptyInput, emptyID);
        char simpleID[25] = "A simple test message!";
        xsd__ID input = new char[25];
        strcpy (input, simpleID);

        // Test non-nillable element
        xsd__ID result = ws->asNonNillableElement(input);
        if (result)
        {
            if (*result)
            {
                cout << "non-nillable element=" << result << endl;
            }
            else
            {
                cout << "non-nillable element=<empty>" << endl;
            }
        }
        else
        {
            cout << "non-nillable element=<nil>" << endl;
        }
        delete [] input;

        // Test empty non-nillable element
        result = ws->asNonNillableElement(emptyInput);
        if (result)
        {
            if (*result)
            {
                cout << "empty non-nillable element=" << result << endl;
            }
            else
            {
                cout << "empty non-nillable element=<empty>" << endl;
            }
        }
        else
        {
            cout << "empty non-nillable element=<nil>" << endl;
        }
        delete [] emptyInput;

        // Test non-nillable element with XML reserved characters
        char reservedCharactersID[] = "<>&\"\'";
        xsd__ID reservedCharactersInput = reservedCharactersID;
        result = ws->asNonNillableElement(reservedCharactersInput);
        if (result)
        {
            if (*result)
            {
                cout << "non-nillable element with XML reserved characters=" << result << endl;
            }
            else
            {
                cout << "non-nillable element with XML reserved characters=<empty>" << endl;
            }
        }
        else
        {
            cout << "non-nillable element with XML reserved characters=<nil>" << endl;
        }

        // Test non-nillable element with XML reserved characters
        char whitespaceID[] = "  \t\r\nsome text \t\r\nmore text \t\r\n";
        xsd__ID whitespaceInput = whitespaceID;
        result = ws->asNonNillableElement(whitespaceInput);
        if (result)
        {
            if (*result)
            {
                cout << "non-nillable element with whitespace characters=\"" << result << "\"" << endl;
            }
            else
            {
                cout << "non-nillable element with whitespace characters=<empty>" << endl;
            }
        }
        else
        {
            cout << "non-nillable element with whitespace characters=<nil>" << endl;
        }

        // Test nillable element, with a value
        input = new char[25];
        strcpy (input, simpleID);
        xsd__ID nillableResult = ws->asNillableElement(input);
        if (nillableResult)
        {
            if (*nillableResult)
            {
                cout << "nillable element=" << nillableResult << endl;
            }
            else
            {
                cout << "nillable element=<empty>" << endl;
            }
            delete nillableResult;
        }
        else
        {
            cout << "nillable element=<nil>" << endl;
        }
        delete [] input;

        // Test empty nillable element
        emptyInput = new char[1];
        strcpy (emptyInput, emptyID);
        nillableResult = ws->asNillableElement(emptyInput);
        if (nillableResult)
        {
            if (*nillableResult)
            {
                cout << "empty nillable element=" << nillableResult << endl;
            }
            else
            {
                cout << "empty nillable element=<empty>" << endl;
            }
            delete nillableResult;
        }
        else
        {
            cout << "empty nillable element=<nil>" << endl;
        }
        delete [] emptyInput;

        // Test nillable element, with nil
        nillableResult = ws->asNillableElement(NULL);
        if (nillableResult)
        {
            if (*nillableResult)
            {
                cout << "nil element=" << nillableResult << endl;
            }
            else
            {
                cout << "nil element=<empty>" << endl;
            }
            delete nillableResult;
        }
        else
        {
            cout << "nil element=<nil>" << endl;
        }

        // Test required attribute
        input = new char[25];
        strcpy (input, simpleID);
        RequiredAttributeElement requiredAttributeInput;
        requiredAttributeInput.setrequiredAttribute(input);
        RequiredAttributeElement* requiredAttributeResult = ws->asRequiredAttribute(&requiredAttributeInput);
        if (requiredAttributeResult->getrequiredAttribute())
        {
            if (*(requiredAttributeResult->getrequiredAttribute()))
            {
                cout << "required attribute=" << requiredAttributeResult->getrequiredAttribute() << endl;
            }
            else
            {
                cout << "required attribute=<empty>" << endl;
            }
        }
        else
        {
            cout << "required attribute=<nil>" << endl;
        }
        delete requiredAttributeResult;

        // Test empty required attribute
        emptyInput = new char[1];
        strcpy (emptyInput, emptyID);
        requiredAttributeInput;
        requiredAttributeInput.setrequiredAttribute(emptyInput);
        requiredAttributeResult = ws->asRequiredAttribute(&requiredAttributeInput);
        if (requiredAttributeResult->getrequiredAttribute())
        {
            if (*(requiredAttributeResult->getrequiredAttribute()))
            {
                cout << "empty required attribute=" << requiredAttributeResult->getrequiredAttribute() << endl;
            }
            else
            {
                cout << "empty required attribute=<empty>" << endl;
            }
        }
        else
        {
            cout << "empty required attribute=<nil>" << endl;
        }
        delete requiredAttributeResult;

/* Optional Attributes currently unsupported by WSDL2Ws
 * Exact coding of this section may change depending on chosen implementation
        // Test optional attribute, with a value
        input = new char[25];
        strcpy (input, simpleID);
        OptionalAttributeElement optionalAttributeInput;
        optionalAttributeInput.setoptionalAttribute(input);
        OptionalAttributeElement* optionalAttributeResult = ws->asOptionalAttribute(&optionalAttributeInput);
        if (optionalAttributeResult->getoptionalAttribute())
        {
            if (*(optionalAttributeResult->getoptionalAttribute()))
            {
                cout << "optional attribute, with data=" << optionalAttributeResult->getoptionalAttribute() << endl;
            }
            else
            {
                cout << "optional attribute, with data=<empty>" << endl;
            }
        }
        else
        {
            cout << "optional attribute, with data=<not present>" << endl;
        }
        delete [] input;
        delete optionalAttributeResult;

        // Test empty optional attribute
        emptyInput = new char[1];
        strcpy (emptyInput, emptyID);
        optionalAttributeInput.setoptionalAttribute(emptyInput);
        optionalAttributeResult = ws->asOptionalAttribute(&optionalAttributeInput);
        if (optionalAttributeResult->getoptionalAttribute())
        {
            if (*(optionalAttributeResult->getoptionalAttribute()))
            {
                cout << "empty optional attribute=" << optionalAttributeResult->getoptionalAttribute() << endl;
            }
            else
            {
                cout << "empty optional attribute=<empty>" << endl;
            }
        }
        else
        {
            cout << "empty optional attribute=<not present>" << endl;
        }
        delete [] emptyInput;
        delete optionalAttributeResult;

        // Test optional attribute, not present
        // optionalAttributeInput.setattribute();
        optionalAttributeResult = ws->asOptionalAttribute(&optionalAttributeInput);
        if (optionalAttributeResult->getoptionalAttribute())
        {
            if (*(optionalAttributeResult->getoptionalAttribute()))
            {
                cout << "optional attribute, not present=" << optionalAttributeResult->getoptionalAttribute() << endl;
            }
            else
            {
                cout << "optional attribute, not present=<empty>" << endl;
            }
        }
        else
        {
            cout << "optional attribute, not present=<not present>" << endl;
        }
        delete optionalAttributeResult;
*/

        // Test array
            
		int arraySize = 2;
        xsd__ID_Array arrayInput;
		xsd__ID * array=new xsd__ID[arraySize]();        
        for (int inputIndex=0 ; inputIndex < 2 ; inputIndex++)
        {
            input = new char[25];
            strcpy (input, simpleID);
            array[inputIndex] = input;
        }
        arrayInput.set(array,arraySize);
        xsd__ID_Array arrayResult = ws->asArray(arrayInput);
		int outputSize=0;
		const xsd__ID * output=arrayResult.get(outputSize);
        cout << "array of " << outputSize << " elements" << endl;
        for (int index = 0; index < outputSize ; index++)
        {
            if (output!=NULL)
            {
                if (output[index]!=NULL)
                {
                    cout << "  element[" << index << "]=" << output[index] << endl;
                }
                else
                {
                    cout << "  element[" << index << "]=<empty>" << endl;
                }               
            }
            else
            {
                cout << "  element[" << index << "]=<nil>" << endl;
            }
        }
        // Clear up input array        
        for (int deleteIndex = 0 ; deleteIndex < arraySize ; deleteIndex++ )
        {
            delete array[deleteIndex];
        }
        delete [] array;


        // Test complex type
        input = new char[25];
        strcpy (input, simpleID);
        SimpleComplexType complexTypeInput;
        complexTypeInput.setcomplexTypeElement(input);
        SimpleComplexType* complexTypeResult = ws->asComplexType(&complexTypeInput);
        if (complexTypeResult->getcomplexTypeElement())
        {
            if (*(complexTypeResult->getcomplexTypeElement()))
            {
                cout << "within complex type=" << complexTypeResult->getcomplexTypeElement() << endl;
            }
            else
            {
                cout << "within complex type=<empty>" << endl;
            }
        }
        else
        {
            cout << "within complex type=<nil>" << endl;
        }
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