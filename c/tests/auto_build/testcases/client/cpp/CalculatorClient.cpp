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

#include "Calculator.hpp"
#include <iostream>
#include <axis/AxisException.hpp>
#include <ctype.h>

void PrintUsage();
bool IsNumber(const char* p);

int main(int argc, char* argv[])
{
	char endpoint[256];
	const char* url="http://192.168.101.4:80/axis/Calculator";
	const char* op = 0;
	int i1=0, i2=0;
        int iResult;
        char* pcDetail;
        pcDetail = 0;

	url = argv[1];
		bool bSuccess = false;
		int	iRetryIterationCount = 3;

		do
		{

	try
        {
		printf("Sending Requests to Server %s .......\n\n", url);
		sprintf(endpoint, "%s", url);
		Calculator ws(endpoint);

		op = "add";
		i1 = 2;
		i2 = 3;

		if (strcmp(op, "add") == 0)
		{
        	        iResult = ws.add(i1, i2);
                	//ws.getFaultDetail(&pcDetail);
	                printf("%d\n", iResult);
        	        /*printf("pcDetail:%s\n", pcDetail);*/
					bSuccess = true;
		}
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
            printf("Unknown exception has occured\n" );
        }
	catch(...)
        {
            printf("Unknown exception has occured\n" );
        }
		iRetryIterationCount--;
		} while( iRetryIterationCount > 0 && !bSuccess);
		return 0;
}

void PrintUsage()
{
	printf("Usage :\n Calculator <server> <port> <operation> <parameter> <parameter>\n\n");
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
