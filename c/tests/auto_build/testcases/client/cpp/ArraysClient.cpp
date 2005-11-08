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

#include "Arrays.hpp"
#include <axis/AxisException.hpp>
#include <ctype.h>
#include <iostream>
#include <signal.h>

void sig_handler(int);
void PrintUsage();
bool IsNumber(const char* p);

void testAxis_Array();
void testAxis_ArrayWithNillElements();
void testAxis_ArrayCopying();

int main(int argc, char* argv[])
{
    char endpoint[256];
    const char* url="http://localhost:80/axis/Arrays";

    signal(SIGILL, sig_handler);
    signal(SIGABRT, sig_handler);
    signal(SIGSEGV, sig_handler);
    //signal(SIGQUIT, sig_handler);
    //signal(SIGBUS, sig_handler);
    signal(SIGFPE, sig_handler);

    url = argv[1];

    testAxis_Array();
    testAxis_ArrayWithNillElements();
    testAxis_ArrayCopying();

    bool bSuccess = false;
    int iRetryIterationCount = 3;
    do
    {
        try
        {
            sprintf(endpoint, "%s", url);
            Arrays ws(endpoint);

            int arraySize = 3;
            xsd__int_Array inputArray;
            xsd__int** array = new xsd__int*[arraySize]();
            for (int count = 0 ; count < arraySize ; count++)
            {
                array[count] = new xsd__int(count);
            }
            inputArray.set(array, arraySize);
            
            xsd__int_Array outputArray = ws.SimpleArray(inputArray);
            int outputSize = 0;
            const xsd__int** output = outputArray.get(outputSize);
            cout << "Array size = " << outputSize << endl;
            if (output != NULL)
            {
                for (int count = 0 ; count < outputSize ; count++)
                {
                    if (output[count] != NULL)
                    {
                        cout << *output[count] << endl;
                    }
                    else
                    {
                        cout << "NULL" << endl;
                    }
                }
            }
            else
            {
                cout << "NULL array" << endl;
            }
            
            bSuccess = 1;
        }
        catch(AxisException& e)
        {
            bool bSilent = false;

           if( e.getExceptionCode() == CLIENT_TRANSPORT_OPEN_CONNECTION_FAILED)
          {
             if( iRetryIterationCount > 0)
             {
                 bSilent = true;
               }
         }
         else
          {
             iRetryIterationCount = 0;
         }

            if( !bSilent)
            {
             cout << "Exception : " << e.what() << endl;
           }
     }
     catch(exception& e)
       {
         cout << "Unexpected exception has occured: " << e.what() << endl;
     }
     catch(...)
        {
         cout << "Unknown exception has occured" << endl;
      }
     iRetryIterationCount--;
   } while( iRetryIterationCount > 0 && !bSuccess);
    cout<< "---------------------- TEST COMPLETE -----------------------------"<< endl;
 
  return 0;
}

void PrintUsage()
{
  printf("Usage :\n Calculator <url>\n\n");
 exit(1);
}

bool IsNumber(const char* p)
{
    for (int x=0; x < strlen(p); x++)
 {
     if (!isdigit(p[x])) return false;
 }
 return true;
}

void sig_handler(int sig) {
    signal(sig, sig_handler);
    cout << "SIGNAL RECEIVED " << sig << endl;
 exit(1);
}

void testAxis_Array()
{
    // Unit test the Axis_Array object for simple types (in this case xsd__int)
    // Initialize Array
    int unitTestInputSize = 3;
    xsd__int_Array unitTest_Axis_Array;
    xsd__int** unitTestActualArray = new xsd__int*[unitTestInputSize]();
    for (int count = 0 ; count < unitTestInputSize ; count++ )
    {
        unitTestActualArray[count] = new xsd__int(count);
    }
    unitTest_Axis_Array.set(unitTestActualArray, unitTestInputSize);

    // Verify the correct data is available
    int size;
    const xsd__int** unitTestOutputArray = unitTest_Axis_Array.get(size);
    cout << "Array size = " << size << endl;
    if (unitTestOutputArray != NULL)
    {
        for (int count = 0 ; count < size ; count++)
        {
            if (unitTestOutputArray[count] != NULL)
            {
                cout << *unitTestOutputArray[count] << endl;
            }
            else
            {
                cout << "NULL" << endl;
            }
        }
    }
    else
    {
        cout << "NULL array" << endl;
    }

    // Alter the content of the original array, this should no longer affect the Axis_Array object
    for (count = 0 ; count < unitTestInputSize ; count++ )
    {
        unitTestActualArray[count] = new xsd__int(count * 1000);
    }

    // Verify the correct data is available, and not the altered content
    unitTestOutputArray = unitTest_Axis_Array.get(size);
    cout << "Array size = " << size << endl;
    if (unitTestOutputArray != NULL)
    {
        for (int count = 0 ; count < size ; count++)
        {
            if (unitTestOutputArray[count] != NULL)
            {
                cout << *unitTestOutputArray[count] << endl;
            }
            else
            {
                cout << "NULL" << endl;
            }
        }
    }
    else
    {
        cout << "NULL array" << endl;
    }

    // Delete the original array, this should not affect the Axis_Array
    for (count = 0 ; count < unitTestInputSize ; count++)
    {
        delete unitTestActualArray[count];
    }
    delete [] unitTestActualArray;

    // Verify the correct data is still available
    unitTestOutputArray = unitTest_Axis_Array.get(size);
    cout << "Array size = " << size << endl;
    if (unitTestOutputArray != NULL)
    {
        for (int count = 0 ; count < size ; count++)
        {
            if (unitTestOutputArray[count] != NULL)
            {
                cout << *unitTestOutputArray[count] << endl;
            }
            else
            {
                cout << "NULL" << endl;
            }
        }
    }
    else
    {
        cout << "NULL array" << endl;
    }
}

void testAxis_ArrayWithNillElements()
{
    // Unit test the Axis_Array object for simple types (in this case xsd__int) with a nil (NULL) element
    // Initialize Array
    int unitTestInputSize = 3;
    xsd__int_Array unitTest_Axis_Array;
    xsd__int** unitTestActualArray = new xsd__int*[unitTestInputSize]();
    unitTestActualArray[0] = new xsd__int(12345);
    unitTestActualArray[1] = NULL;
    unitTestActualArray[2] = new xsd__int(54321);

    unitTest_Axis_Array.set(unitTestActualArray, unitTestInputSize);

    // Verify the correct data is available
    int size;
    const xsd__int** unitTestOutputArray = unitTest_Axis_Array.get(size);
    cout << "Array size = " << size << endl;
    if (unitTestOutputArray != NULL)
    {
        for (int count = 0 ; count < size ; count++)
        {
            if (unitTestOutputArray[count] != NULL)
            {
                cout << *unitTestOutputArray[count] << endl;
            }
            else
            {
                cout << "NULL" << endl;
            }
        }
    }
    else
    {
        cout << "NULL array" << endl;
    }
}

void testAxis_ArrayCopying()
{
    int unitTestInputSize = 3;
    xsd__int_Array unitTest_Axis_Array;
    xsd__int** unitTestActualArray = new xsd__int*[unitTestInputSize]();
    for (int count = 0 ; count < unitTestInputSize ; count++ )
    {
        unitTestActualArray[count] = new xsd__int(count);
    }
    unitTest_Axis_Array.set(unitTestActualArray, unitTestInputSize);

    for (count = 0 ; count < unitTestInputSize ; count++ )
    {
        *unitTestActualArray[count] = count * 10;
    }
    unitTest_Axis_Array.set(unitTestActualArray, unitTestInputSize);

    for (count = 0 ; count < unitTestInputSize ; count++);
    {
        delete unitTestActualArray[count];
    }
    delete [] unitTestActualArray;

    int outputSize = 0;
    const xsd__int** outputArray = unitTest_Axis_Array.get(outputSize);
    cout << "Size is " << outputSize << endl;
    if (outputArray != NULL)
    {
        for (int count = 0 ; count < outputSize ; count++)
        {
            if (outputArray[count] != NULL)
            {
                cout << *outputArray[count] << endl;
            }
            else
            {
                cout << "NULL" << endl;
            }
        }
    }
    else
    {
        cout << "NULL" << endl;
    }
}