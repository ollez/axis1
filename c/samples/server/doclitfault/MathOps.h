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
 *
 *
 *   @author Damitha Kumarage (damitha@opensource.lk, damitha@jkcsworld.com)
 *
 */

/*
 * This is the Service Class genarated by the tool WSDL2Ws
 * MathOps.h: interface for the MathOpsclass.
 *
 */
#if !defined(__MATHOPS_SERVERSKELETON_H__INCLUDED_)
#define __MATHOPS_SERVERSKELETON_H__INCLUDED_

#include <axis/AxisUserAPI.hpp>

#include "DivByZeroStruct.h"
#include "AxisServiceException.h"

class MathOps 
{
	public:
		MathOps();
	public:
		virtual ~MathOps();
	public: 
		int div(int Value0,int Value1);
};

#endif /* !defined(__MATHOPS_SERVERSKELETON_H__INCLUDED_)*/