/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains Client Stub implementation for remote web service.
 */

#include "MathOps.h"

#include <axis/AxisWrapperAPI.hpp>

extern int Axis_DeSerialize_DivByZeroStruct(DivByZeroStruct* param, IWrapperSoapDeSerializer* pDZ);
extern void* Axis_Create_DivByZeroStruct(DivByZeroStruct *Obj, bool bArray = false, int nSize=0);
extern void Axis_Delete_DivByZeroStruct(DivByZeroStruct* param, bool bArray = false, int nSize=0);
extern int Axis_Serialize_DivByZeroStruct(DivByZeroStruct* param, IWrapperSoapSerializer* pSZ, bool bArray = false);
extern int Axis_GetSize_DivByZeroStruct();


extern int Axis_DeSerialize_OutOfBoundStruct(OutOfBoundStruct* param, IWrapperSoapDeSerializer* pDZ);
extern void* Axis_Create_OutOfBoundStruct(OutOfBoundStruct *Obj, bool bArray = false, int nSize=0);
extern void Axis_Delete_OutOfBoundStruct(OutOfBoundStruct* param, bool bArray = false, int nSize=0);
extern int Axis_Serialize_OutOfBoundStruct(OutOfBoundStruct* param, IWrapperSoapSerializer* pSZ, bool bArray = false);
extern int Axis_GetSize_OutOfBoundStruct();


extern int Axis_DeSerialize_SpecialDetailStruct(SpecialDetailStruct* param, IWrapperSoapDeSerializer* pDZ);
extern void* Axis_Create_SpecialDetailStruct(SpecialDetailStruct *Obj, bool bArray = false, int nSize=0);
extern void Axis_Delete_SpecialDetailStruct(SpecialDetailStruct* param, bool bArray = false, int nSize=0);
extern int Axis_Serialize_SpecialDetailStruct(SpecialDetailStruct* param, IWrapperSoapSerializer* pSZ, bool bArray = false);
extern int Axis_GetSize_SpecialDetailStruct();


using namespace std;

MathOps::MathOps(const char* pchEndpointUri, AXIS_PROTOCOL_TYPE eProtocol)
:Stub(pchEndpointUri, eProtocol)
{
}

MathOps::MathOps()
:Stub(" ", APTHTTP1_1)
{
	m_pCall->setEndpointURI("http://localhost/axis/MathOps");
}

MathOps::~MathOps()
{
}


/*Methods corresponding to the web service methods*/

/*
 * This method wrap the service method div
 */
int MathOps::div(int Value0, int Value1)
{
	int Ret;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER, NORMAL_CHANNEL)) 
			return Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "MathOps#div");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("div", "http://soapinterop.org/wsdl");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "int0", XSD_INT);
		m_pCall->addParameter((void*)&Value1, "int1", XSD_INT);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("divResponse", "http://soapinterop.org/wsdl"))
			{
				Ret = m_pCall->getElementAsInt("addReturn", 0);
			}
		}
		m_pCall->unInitialize();
		return Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
                        m_pCall->unInitialize();
			throw;
		}
                
                ISoapFault* pSoapFault = (ISoapFault*) m_pCall->checkFault("Fault", "http://localhost/axis/MathOps");
                if(pSoapFault)
		{
		    pcCmplxFaultName = pSoapFault->getCmplxFaultObjectName().c_str();
		    //printf("pcCmplxFaultName:%s\n", pcCmplxFaultName);
                    if(0 == strcmp("DivByZeroStruct", pcCmplxFaultName))
                    {
                        DivByZeroStruct* pFaultDetail = NULL;
                        pFaultDetail = (DivByZeroStruct*)pSoapFault->
                            getCmplxFaultObject((void*) Axis_DeSerialize_DivByZeroStruct,
                            (void*) Axis_Create_DivByZeroStruct,
                            (void*) Axis_Delete_DivByZeroStruct,"DivByZeroStruct", 0);

			pSoapFault->setCmplxFaultObject(pFaultDetail);
			
		        m_pCall->unInitialize();
                        throw AxisClientException(pSoapFault);
		    }
                    if(0 == strcmp("OutOfBoundStruct", pcCmplxFaultName))
                    {
                        OutOfBoundStruct* pFaultDetail = NULL;
                        pFaultDetail = (OutOfBoundStruct*)pSoapFault->
                            getCmplxFaultObject((void*) Axis_DeSerialize_OutOfBoundStruct,
                            (void*) Axis_Create_OutOfBoundStruct,
                            (void*) Axis_Delete_OutOfBoundStruct,"OutOfBoundStruct", 0);

			pSoapFault->setCmplxFaultObject(pFaultDetail);
			
		        m_pCall->unInitialize();
                        throw AxisClientException(pSoapFault);
		    }
                    if(0 == strcmp("SpecialDetailStruct", pcCmplxFaultName))
                    {
                        SpecialDetailStruct* pFaultDetail = NULL;
                        pFaultDetail = (SpecialDetailStruct*)pSoapFault->
                            getCmplxFaultObject((void*) Axis_DeSerialize_SpecialDetailStruct,
                            (void*) Axis_Create_SpecialDetailStruct,
                            (void*) Axis_Delete_SpecialDetailStruct,"SpecialDetailStruct", 0);

			pSoapFault->setCmplxFaultObject(pFaultDetail);
			
		        m_pCall->unInitialize();
                        throw AxisClientException(pSoapFault);
		    } 
		    else
		    {
                        m_pCall->unInitialize();
                        throw AxisClientException(pSoapFault);
		    }
                }
		else throw;
	}
}

int MathOps::getFaultDetail(char** ppcDetail)
{
	return m_pCall->getFaultDetail(ppcDetail);
}
