#pragma warning (disable : 4786)

#include <iostream>
#include <string>
#include <map>

using namespace std;
#include "actions.h"
#include "cppyacc.hpp"
#include "Variable.h"
#include "Method.h"

//global variables used by the parser
map<string,int> lexer_keys; //keyword map in the lexical analyzer.

TranslationUnit* g_pTranslationUnit; //ultimate container of parsed information
string g_classname; //this holds the class name while the body of a class is being parsed.
list<string> g_classesfound;
int g_currentclasstype;
WSClass* g_pCurrentWSClass;
BeanClass* g_pCurrentBeanClass;
bool g_bdestructor_seen;
int g_currentaccessspecifier;

//forward declarations of function in this file
int map_var_type(int parsertype);
int get_var_type(const char* vartypename);

bool is_defined_class(const char* identifier)
{
	for(list<string>::iterator it = g_classesfound.begin(); it != g_classesfound.end(); it++)
	{
		if (*it == identifier)
		{
			return true;
		}
	}
	return false;
}

void translation_unit_start()
{
	g_pTranslationUnit = new TranslationUnit();
}

bool is_bean_class(base_specifier_list* baselist)
{
	if (!baselist) return false;
	for (base_specifier_list::iterator it = baselist->begin(); it != baselist->end(); it++)
	{
		if ((*(*it)->class_name) == "AccessBean")
		{
			return true;
		}
	}
	return false;	
}

void add_member_declaration(string_list* decl_specs, member_declarator_list* member_decl_list)
{
  int nVarType;
  int nConvVarType;
  unsigned char nQualifier = 0;
  Variable Var;
  bool bIsCtor = false; //
  //find current access qualifier
  switch (g_currentaccessspecifier)
  {
    case KW_private:
       nQualifier |= Q_PRIVATE;
    break;
    case KW_protected:
       nQualifier |= Q_PROTECTED;
    break;
    case KW_public:
       //nothing to be done
    break;
    default:;
  }
  Var.SetQualification(nQualifier);
  //find variable type and qualifiers
  if (decl_specs)
  {
	for (string_list::iterator tlit = decl_specs->begin(); tlit != decl_specs->end(); tlit++)
    {
    	string declstr = (*tlit)->c_str();
    	if (lexer_keys.find(declstr) != lexer_keys.end())
     	{
     		cout << "Variable Type : " << declstr.c_str() << endl;
      		nVarType = lexer_keys[declstr];
			if ((nConvVarType = map_var_type(nVarType)) != 0)
			{
        		if (nConvVarType == VAR_USER)
         		{
					Var.SetType(nConvVarType, declstr);
				}
				else
				{
					Var.SetType(nConvVarType);
				}
			}
			else //may be a variable qualifier like "const" or "static"
			{

			}
		}
	}
  }
  else //constructor or destructor
  {
		bIsCtor = true;
  }
  //
  for (member_declarator_list::iterator it = member_decl_list->begin(); it != member_decl_list->end(); it++)
  {
		if ((*it)->method) //a method declaration
		{
			if (g_currentclasstype == BEANCLASS)
			{
				//just ignore this method because we donot wrap methods in a bean class
				cout << "There are methods in a bean class which is not expected" << endl;
			}
			else
			{
				Method* pMethod = new Method();
				if (bIsCtor) //constructor or destructor - no return type
				{
					cout << "Ignoring constructor or destructor" << endl;
					//we dont wrap constructors or destructors.
				}
				else //other methods
				{
					pMethod->AddReturnType(new Variable(Var));
					pMethod->SetQualification(nQualifier);
					pMethod->SetMethodName(*((*it)->declarator_id));
					//set parameters
					if ((*it)->params)
					{
						for (param_decl_list::iterator pit = (*it)->params->begin(); pit != (*it)->params->end(); pit++)
						{
							Var.Reset();
							if ((*pit)->decl_specs)
							{
								for (string_list::iterator slit = (*pit)->decl_specs->begin(); slit != (*pit)->decl_specs->end(); slit++)
								{
									if (0 != get_var_type((*slit)->c_str())) //this is variable type not a qualifier
									{
										Var.SetType(get_var_type((*slit)->c_str()));
									}
									else
									{
										//handle other variable qualifiers here 
									}
								}
							}
							if ((*pit)->param)
							{
								Var.SetVarName((*pit)->param->decl_id);
							}
							//Add as a parameter to the method
							pMethod->AddParameter(new Variable(Var));
 						}
					}
					if (g_pCurrentWSClass)
					{
						g_pCurrentWSClass->AddMethod(pMethod);
					}
				}
			}
		}
		else //variable declaration
		{
			Variable* pVar = new Variable(Var);
			pVar->SetVarName(*((*it)->declarator_id));
			//check for arrays
			//TODO
			if (g_currentclasstype == BEANCLASS)
			{
				if (g_pCurrentBeanClass)
				{
					g_pCurrentBeanClass->AddVariable(pVar);
				}
				else
				{
					cout << "Ignoring variables in the other classes than bean classes" << endl;
				}
			}
		}
  }
}

int map_var_type(int parsertype)
{
	switch (parsertype)
	{
  		//basic types
		case KW_int: return VAR_INT;
		case KW_float: return VAR_FLOAT;
		case KW_char: return VAR_CHAR;
		case KW_double: return VAR_DOUBLE;
		default:;
	}
	return 0;
}

int get_var_type(const char* vartypename)
{
	//this should be in lexer_keys table
	if (lexer_keys.find(vartypename) == lexer_keys.end())
		return false;
	return map_var_type(lexer_keys[vartypename]);
}