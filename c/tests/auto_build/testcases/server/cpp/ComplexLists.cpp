/*
 * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)
 * This file contains definitions of the web service
 */

#include "ComplexLists.hpp"


ComplexLists::ComplexLists()
{
}

ComplexLists::~ComplexLists()
{
}

/* This function is called by the AxisEngine when something went wrong
 with the current web service request processing. Appropriate actions should
 be taken here.*/
void ComplexLists::onFault()
{
}

attrlisterr* ComplexLists::multilist(m_list* Value0, attrlist* Value1)  
{
	attrlisterr* ret=new attrlisterr();
	ret->attrlist_Ref=Value1;
	Value1->item.m_Array[0]->m_list_Ref=Value0;
	return ret;
	
}
attrlisterr* ComplexLists::multilistnil(m_list* Value0, attrlist* Value1)  
{
	attrlisterr* ret=new attrlisterr();
	ret->attrlist_Ref=Value1;
	Value1->item.m_Array[0]->m_list_Ref=Value0;
	return ret;
}
attrlisterr* ComplexLists::complexlist(attrlist* Value0, xsd__string Value1, attrlist* Value2)  
{
	attrlisterr* ret=new attrlisterr();
	ret->attrlist_Ref=Value0;
	Value0->item.m_Size=1;
	Value0->item.m_Array=new namepair*[Value0->item.m_Size];
	Value0->item.m_Array[0]=new namepair();

	return ret;
}