/////////////////////////////////////////////////////////////////////////////
// This is the Service Class genarated by the tool WSDL2Ws
//		InteropTestPortTypeB.h: interface for the InteropTestPortTypeBclass.
//
//////////////////////////////////////////////////////////////////////
#if !defined(__INTEROPTESTPORTTYPEB_SERVERSKELETON_H__OF_AXIS_INCLUDED_)
#define __INTEROPTESTPORTTYPEB_SERVERSKELETON_H__OF_AXIS_INCLUDED_

#include <axis/AxisUserAPI.hpp>

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
		void echoStructAsSimpleTypes(SOAPStruct* Value0, AXIS_OUT_PARAM AxisChar** outValue0, AXIS_OUT_PARAM int* outValue1, AXIS_OUT_PARAM float* outValue2);
		SOAPStruct* echoSimpleTypesAsStruct(float Value0,int Value1,AxisChar* Value2);
		ArrayOfString2D echo2DStringArray(ArrayOfString2D Value0);
		SOAPStructStruct* echoNestedStruct(SOAPStructStruct* Value0);
		SOAPArrayStruct* echoNestedArray(SOAPArrayStruct* Value0);
};

#endif // !defined(__INTEROPTESTPORTTYPEB_SERVERSKELETON_H__OF_AXIS_INCLUDED_)
