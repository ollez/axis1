/*
 *   Copyright 2003-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/*
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Roshan Weerasuriya (roshan@opensource.lk, roshanw@jkcsworld.com)
 */

#include <string>
using namespace std;

#include "../../gen_src/rpc/InteropTestPortType.h"
#include <axis/server/IHeaderBlock.h>
#include <axis/server/AxisException.h>

#define ARRAYSIZE 2

int
main(int argc, char *argv[])
{
    int x;
    char buffer1[100];
    char endpoint[256];
    const char *server = "localhost";
    const char *port = "80";
    if (argc == 3)
    {
	server = argv[1];
	port = argv[2];
    }
    printf("Usage :\n %s <server> <port>\n\n", argv[0]);
    //sprintf(endpoint, "http://%s:%s/axis/base", server, port);
    //endpoint for Axis CPP sample
    sprintf(endpoint, "http://%s:%s/axis/base", server, port);
    InteropTestPortType ws(endpoint, APTHTTP);

    /*
    <SOAP-ENV:Header>
    	<ns2:TestHeader xmlns:ns2="http://ws.apache.org/axisCppTest/" xmlns:namesp1="http://mynamespace.com">
	    <Credentials ns2:type="normal">
		<username ns2:usertype="normal_user">Test User</username>
		<password>Test Password</password>
	    </Credentials>
	</ns2:TestHeader>
    </SOAP-ENV:Header>
    */

    //set SOAP headers
    IHeaderBlock *phb = ws.createSOAPHeaderBlock("TestHeader", 
		"http://ws.apache.org/axisCppTest/");
    phb->createNamespaceDecl("namesp1", "http://mynamespace.com");

    //create parent node
    BasicNode *parentNode = phb->createChild(ELEMENT_NODE);
    parentNode->setLocalName("Credentials");
    parentNode->createAttribute("type", "", "http://ws.apache.org/axisCppTest/", "normal");
    parentNode->createAttribute("scale", "", "http://ws.apache.org/axisCppTest/", "normal_scale");
    parentNode->createAttribute("lang1", "", "http://ws.apache.org/axisCppTest/", "normal_lang1");
    parentNode->createAttribute("lang2", "", "http://ws.apache.org/axisCppTest/", "normal_lang2");
    parentNode->createAttribute("lang3", "", "http://ws.apache.org/axisCppTest/", "normal_lang3");

    //create child node     
    BasicNode *childNode = phb->createChild(ELEMENT_NODE);
    childNode->setLocalName("username");
    childNode->createAttribute("usertype", "", "http://ws.apache.org/axisCppTest/", "normal_user");
    //create char node for value
    BasicNode *valueNode = phb->createChild(CHARACTER_NODE);
    valueNode->setValue("Test User");
    //buld node tree
    childNode->addChild(valueNode);
    parentNode->addChild(childNode);

    //add another node set
    childNode = phb->createChild(ELEMENT_NODE);
    childNode->setLocalName("password");

    valueNode = phb->createChild(CHARACTER_NODE);
    valueNode->setValue("Test Password");

    childNode->addChild(valueNode);
    parentNode->addChild(childNode);

    phb->addChild(parentNode);

    /* start testing the getters for SOAP Header Attributes*/
    IAttribute* pIAttribute1 = parentNode->getFirstAttribute();
    if (pIAttribute1)
    	printf("pIAttribute1 details = %s %s \n", pIAttribute1->getLocalName(), pIAttribute1->getUri());

    IAttribute* pIAttribute2 = NULL;
    int iTmpX = 0;
    while ((pIAttribute2 = parentNode->getNextAttribute()) != NULL) {
	printf("pIAttribute2 details = %s %s \n", pIAttribute2->getLocalName(), pIAttribute2->getUri());
	
	iTmpX++;
   	if (iTmpX==2) break; /*Just for a simple logic*/
    }

    IAttribute* pIAttribute3 = parentNode->getCurrentAttribute();
    if (pIAttribute3)
        printf("pIAttribute3 details = %s %s \n", pIAttribute3->getLocalName(), pIAttribute3->getUri());

    IAttribute* pIAttribute4 = parentNode->getLastAttribute();
    if (pIAttribute4)
    	printf("pIAttribute4 details = %s %s \n", pIAttribute4->getLocalName(), pIAttribute4->getUri());
    /*----end testing the getters for SOAP Header Attributes----- */

    printf("Sending Requests to end point %s \n\n", endpoint);
    printf("invoking echoString...\n");
    //testing echoString 
    try 
    {
    if (0 == strcmp(ws.echoString("hello world"), "hello world"))
	printf("successful\n");
    else
	printf("failed\n");
    }
    catch (AxisException& e)
    {
        printf("%s\n", e.what());
    }
    catch(...)
    {
        printf("Unknown exception\n");
    }

    printf("Soap Header test end\n");
    return 0;
}
