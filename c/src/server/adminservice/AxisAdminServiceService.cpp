/*This file is automatically generated by the Axis C++ Wrapper Class Generator
 *Service file containing the two export functions of the Web service Library*/

#include "AxisAdminServiceWrapper.h" 
extern "C" {
STORAGE_CLASS_INFO
int GetClassInstance(BasicHandler **inst)
{
	*inst = new BasicHandler();
	WrapperClassHandler* pWCH = new AxisAdminServiceWrapper();
	(*inst)->_functions = 0;
	if (pWCH)
	{
		(*inst)->_object = pWCH;
		return pWCH->init();
	}
	return AXIS_FAIL;
}
STORAGE_CLASS_INFO 
int DestroyInstance(BasicHandler *inst)
{
	if (inst)
	{
		WrapperClassHandler* pWCH = reinterpret_cast<WrapperClassHandler*>(inst);
		pWCH->fini();
		delete pWCH;
		delete inst;
		return AXIS_SUCCESS;
	}
	return AXIS_FAIL;
}
}