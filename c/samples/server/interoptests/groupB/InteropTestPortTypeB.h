/////////////////////////////////////////////////////////////////////////////
// This is the Service Class genarated by the tool WSDL2Ws
//		InteropTestPortTypeB.h: interface for the InteropTestPortTypeBclass.
//
//////////////////////////////////////////////////////////////////////
#if !defined(__INTEROPTESTPORTTYPEB_SERVERSKELETON_H__INCLUDED_)
#define __INTEROPTESTPORTTYPEB_SERVERSKELETON_H__INCLUDED_

#include <axis/common/AxisUserAPI.h>

#include "SOAPArrayStruct.h"
#include "SOAPStruct.h"
#include "ArrayOffloat.h"
#include "ArrayOfSOAPStruct.h"
#include "ArrayOfint.h"
#include "ArrayOfString2D.h"
#include "SOAPStructStruct.h"
#include "ArrayOfstring.h"

class InteropTestPortTypeB 
{
	public:
		InteropTestPortTypeB();
	public:
		virtual ~InteropTestPortTypeB();
	public: 
		void echoStructAsSimpleTypes(SOAPStruct* Value0, AXIS_OUT_PARAM string* outValue0, AXIS_OUT_PARAM int* outValue1, AXIS_OUT_PARAM float* outValue2);
		SOAPStruct* echoSimpleTypesAsStruct(float Value0,int Value1,string Value2);
		ArrayOfString2D echo2DStringArray(ArrayOfString2D Value0);
		SOAPStructStruct* echoNestedStruct(SOAPStructStruct* Value0);
		SOAPArrayStruct* echoNestedArray(SOAPArrayStruct* Value0);
};

#endif // !defined(__INTEROPTESTPORTTYPEB_SERVERSKELETON_H__INCLUDED_)
