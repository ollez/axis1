/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains implementations of the array Exception class of the web service.
 */

#include "array_AxisClientException.h"

#include <axis/AxisWrapperAPI.hpp>

array_AxisClientException::array_AxisClientException()
{
/* This only serves the purpose of indicating that the 
 * service has thrown an excpetion 
 */ 
	m_iExceptionCode = AXISC_SERVICE_THROWN_EXCEPTION; 
	processException(m_iExceptionCode); 
}

array_AxisClientException::array_AxisClientException(ISoapFault* pFault)
{
	m_iExceptionCode = AXISC_SERVICE_THROWN_EXCEPTION;
	processException(pFault);}

array_AxisClientException::array_AxisClientException(int iExceptionCode)
{

	m_iExceptionCode = iExceptionCode;
	processException (iExceptionCode);
}

array_AxisClientException::array_AxisClientException(exception* e)
{
	processException (e);
}

array_AxisClientException::array_AxisClientException(exception* e,int iExceptionCode)
{

	processException (e, iExceptionCode);
}

array_AxisClientException::~array_AxisClientException() throw () 
{
	m_sMessage ="";
}

void array_AxisClientException:: processException(exception* e, int iExceptionCode)
{
	m_sMessage = getMessage (e) + getMessage (iExceptionCode);
}

void array_AxisClientException::processException (ISoapFault* pFault)
{
	/*User can do something like deserializing the struct into a string*/
}

void array_AxisClientException::processException(exception* e)
{
	m_sMessage = getMessage (e);
}

void array_AxisClientException::processException(int iExceptionCode)
{
	m_sMessage = getMessage (iExceptionCode);
}

const string array_AxisClientException::getMessage (exception* objException)
{
	string sMessage = objException->what();
	return sMessage;
}

const string array_AxisClientException::getMessage (int iExceptionCode)
{
	string sMessage;
	switch(iExceptionCode)
	{
		case AXISC_SERVICE_THROWN_EXCEPTION:
		sMessage = "The array service has thrown an exception. see details";
		break;
		default:
		sMessage = "Unknown Exception has occured in the array service";
	}
return sMessage;
}

const char* array_AxisClientException::what() throw ()
{
	return m_sMessage.c_str ();
}

const int array_AxisClientException::getExceptionCode(){
	return m_iExceptionCode;
}
