/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains Web Service Wrapper implementations
 */

#include "ArrayTestPortTypeWrapper.h"

extern int Axis_DeSerialize_intArrayType(intArrayType* param, IWrapperSoapDeSerializer* pDZ);
extern void* Axis_Create_intArrayType(intArrayType *Obj, bool bArray = false, int nSize=0);
extern void Axis_Delete_intArrayType(intArrayType* param, bool bArray = false, int nSize=0);
extern int Axis_Serialize_intArrayType(intArrayType* param, IWrapperSoapSerializer* pSZ, bool bArray = false);
extern int Axis_GetSize_intArrayType();

ArrayTestPortTypeWrapper::ArrayTestPortTypeWrapper()
{
	pWs = new ArrayTestPortType();
}

ArrayTestPortTypeWrapper::~ArrayTestPortTypeWrapper()
{
	delete pWs;
}

/*implementation of WrapperClassHandler interface*/
void ArrayTestPortTypeWrapper::onFault(void *pMsg)
{
	pWs->onFault();
}

int ArrayTestPortTypeWrapper::init()
{
	pWs->init();
	return AXIS_SUCCESS;
}

int ArrayTestPortTypeWrapper::fini()
{
	pWs->fini();
	return AXIS_SUCCESS;
}


/*
 * This method invokes the right service method 
 */
int ArrayTestPortTypeWrapper::invoke(void *pMsg)
{
	IMessageData* mc = (IMessageData*)pMsg;
	const AxisChar *method = mc->getOperationName();
	if (0 == strcmp(method, "echoIntArray"))
		return echoIntArray(mc);
	else return AXIS_FAIL;
}


/*Methods corresponding to the web service methods*/

/*
 * This method wrap the service method 
 */
int ArrayTestPortTypeWrapper::echoIntArray(void* pMsg)
{
	IMessageData* mc = (IMessageData*)pMsg;
	int nStatus;
	IWrapperSoapSerializer* pIWSSZ = NULL;
	mc->getSoapSerializer(&pIWSSZ);
	if (!pIWSSZ) return AXIS_FAIL;
	IWrapperSoapDeSerializer* pIWSDZ = NULL;
	mc->getSoapDeSerializer(&pIWSDZ);
	if (!pIWSDZ) return AXIS_FAIL;
	/* check whether we have got correct message */
	if (AXIS_SUCCESS != pIWSDZ->checkMessageBody("echoIntArray", "http://soapinterop.org/")) return AXIS_FAIL;
	pIWSSZ->createSoapMethod("echoIntArrayResponse", "http://soapinterop.org/");
	intArrayType *v0 = (intArrayType*)pIWSDZ->getCmplxObject((void*)Axis_DeSerialize_intArrayType
		, (void*)Axis_Create_intArrayType, (void*)Axis_Delete_intArrayType
		, "inputIntArrayType", Axis_URI_intArrayType);
	if (AXIS_SUCCESS != (nStatus = pIWSDZ->getStatus())) return nStatus;
	try
	{
		intArrayType* ret = pWs->echoIntArray(v0);
		return pIWSSZ->addOutputCmplxParam(ret, (void*)Axis_Serialize_intArrayType, (void*)Axis_Delete_intArrayType, "echoIntArrayReturn", Axis_URI_intArrayType);
	}
	catch(...){
	}
}
