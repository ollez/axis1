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

/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains Web Service Wrapper declarations
 */

#if !defined(__PT1WRAPPER_SERVERWRAPPER_H__INCLUDED_)
#define __PT1WRAPPER_SERVERWRAPPER_H__INCLUDED_

#include "pt1.hpp"
#include <axis/server/WrapperClassHandler.hpp>
#include <axis/IMessageData.hpp>
#include <axis/GDefine.hpp>
#include <axis/AxisWrapperAPI.hpp>
#include "AxisServiceException.hpp" 
AXIS_CPP_NAMESPACE_USE 

class pt1Wrapper : public WrapperClassHandler
{
private:/* Actual web service object*/
	pt1 *pWs;
public:
	pt1Wrapper();
public:
	virtual ~pt1Wrapper();
public:/*implementation of WrapperClassHandler interface*/
	int AXISCALL invoke(void* pMsg);
	void AXISCALL onFault(void* pMsg);
	int AXISCALL init();
	int AXISCALL fini();
	AXIS_BINDING_STYLE AXISCALL getBindingStyle(){return RPC_ENCODED;};
private:/*Methods corresponding to the web service methods*/
	int EchoAttachment(void* pMsg);
};

#endif /* !defined(__PT1WRAPPER_SERVERWRAPPER_H__INCLUDED_)*/
