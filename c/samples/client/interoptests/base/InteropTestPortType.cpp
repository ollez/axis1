/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains Client Stub implementation for remote web service.
 */

#include "InteropTestPortType.hpp"

#include <axis/AxisWrapperAPI.hpp>

using namespace std;

 extern int Axis_DeSerialize_SOAPStruct(SOAPStruct* param, IWrapperSoapDeSerializer* pDZ);
extern void* Axis_Create_SOAPStruct(SOAPStruct *Obj, bool bArray = false, int nSize=0);
extern void Axis_Delete_SOAPStruct(SOAPStruct* param, bool bArray = false, int nSize=0);
extern int Axis_Serialize_SOAPStruct(SOAPStruct* param, IWrapperSoapSerializer* pSZ, bool bArray = false);
extern int Axis_GetSize_SOAPStruct();

InteropTestPortType::InteropTestPortType(const char* pchEndpointUri, AXIS_PROTOCOL_TYPE eProtocol)
:Stub(pchEndpointUri, eProtocol)
{
}

InteropTestPortType::InteropTestPortType()
:Stub(" ", APTHTTP1_1)
{
	m_pCall->setEndpointURI("http://localhost/axis/base");
}

InteropTestPortType::~InteropTestPortType()
{
}


/*Methods corresponding to the web service methods*/

/*
 * This method wrap the service method echoString
 */
xsd__string InteropTestPortType::echoString(xsd__string Value0)
{
	xsd__string Ret;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoString");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoString", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)Value0, "inputString", XSD_STRING);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoStringResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsString("_return", 0);
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
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoStringArray
 */
xsd__string_Array InteropTestPortType::echoStringArray(xsd__string_Array Value0)
{
	xsd__string_Array RetArray = {NULL, 0};
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return RetArray;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoStringArray");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoStringArray", "http://soapinterop.org/");
		applyUserPreferences();
	m_pCall->addBasicArrayParameter((Axis_Array*)(&Value0), XSD_STRING, "inputStringArray");
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoStringArrayResponse", "http://soapinterop.org/"))
			{
				RetArray = (xsd__string_Array&)m_pCall->getBasicArray(XSD_STRING, "_return", 0);
			}
		}
	m_pCall->unInitialize();
		return RetArray;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoInteger
 */
xsd__int InteropTestPortType::echoInteger(xsd__int Value0)
{
	xsd__int* Ret = NULL;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoInteger");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoInteger", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputInteger", XSD_INT);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoIntegerResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsInt("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoIntegerArray
 */
xsd__int_Array InteropTestPortType::echoIntegerArray(xsd__int_Array Value0)
{
	xsd__int_Array RetArray = {NULL, 0};
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return RetArray;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoIntegerArray");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoIntegerArray", "http://soapinterop.org/");
		applyUserPreferences();
	m_pCall->addBasicArrayParameter((Axis_Array*)(&Value0), XSD_INT, "inputIntegerArray");
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoIntegerArrayResponse", "http://soapinterop.org/"))
			{
				RetArray = (xsd__int_Array&)m_pCall->getBasicArray(XSD_INT, "_return", 0);
			}
		}
	m_pCall->unInitialize();
		return RetArray;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoFloat
 */
xsd__float InteropTestPortType::echoFloat(xsd__float Value0)
{
	xsd__float* Ret = NULL;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoFloat");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoFloat", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputFloat", XSD_FLOAT);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoFloatResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsFloat("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoFloatArray
 */
xsd__float_Array InteropTestPortType::echoFloatArray(xsd__float_Array Value0)
{
	xsd__float_Array RetArray = {NULL, 0};
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return RetArray;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoFloatArray");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoFloatArray", "http://soapinterop.org/");
		applyUserPreferences();
	m_pCall->addBasicArrayParameter((Axis_Array*)(&Value0), XSD_FLOAT, "inputFloatArray");
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoFloatArrayResponse", "http://soapinterop.org/"))
			{
				RetArray = (xsd__float_Array&)m_pCall->getBasicArray(XSD_FLOAT, "_return", 0);
			}
		}
	m_pCall->unInitialize();
		return RetArray;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoStruct
 */
SOAPStruct* InteropTestPortType::echoStruct(SOAPStruct* Value0)
{
	SOAPStruct* pReturn = NULL;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return pReturn;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoStruct");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoStruct", "http://soapinterop.org/");
		applyUserPreferences();
	m_pCall->addCmplxParameter(Value0, (void*)Axis_Serialize_SOAPStruct, (void*)Axis_Delete_SOAPStruct, "inputStruct", Axis_URI_SOAPStruct);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoStructResponse", "http://soapinterop.org/"))
			{
				pReturn = (SOAPStruct*)m_pCall->getCmplxObject((void*) Axis_DeSerialize_SOAPStruct, (void*) Axis_Create_SOAPStruct, (void*) Axis_Delete_SOAPStruct,"_return", 0);
		}
		}
	m_pCall->unInitialize();
		return pReturn;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoStructArray
 */
SOAPStruct_Array InteropTestPortType::echoStructArray(SOAPStruct_Array Value0)
{
	SOAPStruct_Array RetArray = {NULL, 0};
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return RetArray;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoStructArray");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoStructArray", "http://soapinterop.org/");
		applyUserPreferences();
	m_pCall->addCmplxArrayParameter((Axis_Array*)(&Value0), (void*)Axis_Serialize_SOAPStruct, (void*)Axis_Delete_SOAPStruct, (void*) Axis_GetSize_SOAPStruct, "inputStructArray", Axis_URI_SOAPStruct);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoStructArrayResponse", "http://soapinterop.org/"))
			{
				RetArray = (SOAPStruct_Array&)m_pCall->getCmplxArray((void*) Axis_DeSerialize_SOAPStruct, (void*) Axis_Create_SOAPStruct, (void*) Axis_Delete_SOAPStruct, (void*) Axis_GetSize_SOAPStruct, "_return", Axis_URI_SOAPStruct);
			}
		}
	m_pCall->unInitialize();
		return RetArray;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoVoid
 */
void InteropTestPortType::echoVoid()
{
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return ;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoVoid");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoVoid", "http://soapinterop.org/");
		applyUserPreferences();
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoVoidResponse", "http://soapinterop.org/"))
			{
			/*not successful*/
		}
		}
	m_pCall->unInitialize();
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoBase64
 */
xsd__base64Binary InteropTestPortType::echoBase64(xsd__base64Binary Value0)
{
	xsd__base64Binary* Ret;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoBase64");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoBase64", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputBase64", XSD_BASE64BINARY);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoBase64Response", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsBase64Binary("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoDate
 */
xsd__dateTime InteropTestPortType::echoDate(xsd__dateTime Value0)
{
	xsd__dateTime* Ret;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoDate");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoDate", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputDate", XSD_DATETIME);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoDateResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsDateTime("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoHexBinary
 */
xsd__hexBinary InteropTestPortType::echoHexBinary(xsd__hexBinary Value0)
{
	xsd__hexBinary* Ret;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoHexBinary");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoHexBinary", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputHexBinary", XSD_HEXBINARY);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoHexBinaryResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsHexBinary("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoDecimal
 */
xsd__decimal InteropTestPortType::echoDecimal(xsd__decimal Value0)
{
	xsd__decimal* Ret = NULL;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoDecimal");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoDecimal", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputDecimal", XSD_DECIMAL);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoDecimalResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsDecimal("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method echoBoolean
 */
xsd__boolean InteropTestPortType::echoBoolean(xsd__boolean Value0)
{
	xsd__boolean* Ret = NULL;
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return *Ret;
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "base#echoBoolean");
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("echoBoolean", "http://soapinterop.org/");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "inputBoolean", XSD_BOOLEAN);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("echoBooleanResponse", "http://soapinterop.org/"))
			{
				Ret = m_pCall->getElementAsBoolean("_return", 0);
			}
		}
	m_pCall->unInitialize();
		return *Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost/axis/base" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}

