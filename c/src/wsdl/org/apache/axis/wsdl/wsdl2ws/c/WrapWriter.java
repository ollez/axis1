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
 */

/**
 * @author Srinath Perera(hemapani@openource.lk)
 * @author Susantha Kumara(susantha@opensource.lk, skumara@virtusa.com)
 */

package org.apache.axis.wsdl.wsdl2ws.c;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.wsdl.wsdl2ws.CUtils;
import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.WrapperUtils;
import org.apache.axis.wsdl.wsdl2ws.info.MethodInfo;
import org.apache.axis.wsdl.wsdl2ws.info.ParameterInfo;
import org.apache.axis.wsdl.wsdl2ws.info.Type;
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;

public class WrapWriter extends CFileWriter
{
    private WebServiceContext wscontext;
    private ArrayList methods;
    public WrapWriter(WebServiceContext wscontext) throws WrapperFault
    {
        super(
            WrapperUtils.getClassNameFromFullyQualifiedName(
                wscontext.getSerInfo().getQualifiedServiceName()
                    + CUtils.WRAPPER_NAME_APPENDER));
        this.wscontext = wscontext;
        this.methods = wscontext.getSerInfo().getMethods();
    }

    protected File getFilePath() throws WrapperFault
    {
        return this.getFilePath(false);
    }

    protected File getFilePath(boolean useServiceName) throws WrapperFault
    {
        String targetOutputLocation =
            this.wscontext.getWrapInfo().getTargetOutputLocation();
        if (targetOutputLocation.endsWith("/"))
            targetOutputLocation =
                targetOutputLocation.substring(
                    0,
                    targetOutputLocation.length() - 1);
        new File(targetOutputLocation).mkdirs();

        String fileName = targetOutputLocation + "/" + classname + ".c";

        if (useServiceName)
        {
            String serviceName = this.wscontext.getSerInfo().getServicename();
            fileName =
                targetOutputLocation
                    + "/"
                    + serviceName
                    + "_"
                    + classname
                    + ".c";
            this.wscontext.addGeneratedFile(
                serviceName + "_" + classname + ".c");
        }
        else
        {
            this.wscontext.addGeneratedFile(classname + ".c");
        }

        return new File(fileName);
    }

    protected void writeClassComment() throws WrapperFault
    {
        try
        {
            writer.write("/*\n");
            writer.write(
                " * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)\n");
            writer.write(
                " * This file contains Web Service Wrapper implementations\n");
            writer.write(" */\n\n");
        }
        catch (IOException e)
        {
            throw new WrapperFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeMethods()
     */
    protected void writeMethods() throws WrapperFault
    {
        try
        {
            writer.write("/*implementation of BasicHandler interface*/\n");
            writer.write(
                "void AXISCALL "
                    + classname
                    + "_OnFault(void*p, void *pMsg){\n}\n\n");
            writer.write(
                "int AXISCALL "
                    + classname
                    + "_Init(void*p){\n\treturn AXIS_SUCCESS;\n}\n\n");
            writer.write(
                "int AXISCALL "
                    + classname
                    + "_Fini(void*p){\n\treturn AXIS_SUCCESS;\n}\n\n");
            writer.write(
                "int AXISCALL "
                    + classname
                    + "_GetType(void*p){\n\treturn WEBSERVICE_HANDLER;\n}\n\n");
            writer.write(
                "AXIS_BINDING_STYLE AXISCALL "
                    + classname
                    + "_GetBindingStyle(void*p){\n\treturn RPC_ENCODED;\n}\n\n");
            writeInvoke();
            writer.write(
                "\n/*Methods corresponding to the web service methods*/\n");
            MethodInfo minfo;
            for (int i = 0; i < methods.size(); i++)
            {
                minfo = (MethodInfo) methods.get(i);
                this.writeMethodInWrapper(minfo);
                writer.write("\n");
            }

        }
        catch (IOException e)
        {
            throw new WrapperFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writePreprocssorStatements()
     */
    protected void writePreprocessorStatements() throws WrapperFault
    {
        try
        {
            writer.write("#include <string.h>\n");
            writer.write("#include \"" + classname + ".h\"\n");
            //As there is no service header file for C the header files for types should be included here itself
            Type atype;
            Iterator types = this.wscontext.getTypemap().getTypes().iterator();
            String LangTypeName = null;
            while (types.hasNext())
            {
                atype = (Type) types.next();
                LangTypeName = WrapperUtils.getLanguageTypeName4Type(atype);
                if (null != LangTypeName)
                    writer.write("#include \"" + LangTypeName + ".h\"\n");
            }
            writer.write("\n");
        }
        catch (IOException e)
        {
            throw new WrapperFault(e);
        }
    }

    /**
     * write the invoke method
     * @throws IOException
     */
    private void writeInvoke() throws IOException
    {
        writer.write("\n/*\n");
        writer.write(" * This method invokes the right service method \n");
        writer.write(" */\n");
        writer.write(
            "int AXISCALL " + classname + "_Invoke(void*p, void *pMsg){\n");
        writer.write("\tIMessageData* mc = (IMessageData*)pMsg;\n");
        writer.write("\tconst AxisChar* method = 0;\n");
        writer.write("\tIWrapperSoapDeSerializer DZ = {0,0};\n");
        writer.write("\tIWrapperSoapSerializer SZ = {0,0};\n");
        writer.write(
            "\tmethod = mc->_functions->getOperationName(mc->_object);\n");
        writer.write(
            "\tmc->_functions->getSoapSerializer(mc->_object, &SZ);\n");
        writer.write(
            "\tmc->_functions->getSoapDeSerializer(mc->_object, &DZ);\n");
        //if no methods in the service simply return
        if (methods.size() == 0)
        {
            writer.write("}\n");
            return;
        }
        MethodInfo minfo = (MethodInfo) methods.get(0);
        //if conditions (if parts)		
        writer.write(
            "\tif (0 == strcmp(method, \"" + minfo.getMethodname() + "\"))\n");
        writer.write(
            "\t\treturn "
                + minfo.getMethodname()
                + CUtils.WRAPPER_METHOD_APPENDER
                + "(DZ, SZ);\n");
        //(else if parts)
        if (methods.size() > 1)
        {
            for (int i = 1; i < methods.size(); i++)
            {
                minfo = (MethodInfo) methods.get(i);
                writer.write(
                    "\telse if (0 == strcmp(method, \""
                        + minfo.getMethodname()
                        + "\"))\n");
                writer.write(
                    "\t\treturn "
                        + minfo.getMethodname()
                        + CUtils.WRAPPER_METHOD_APPENDER
                        + "(DZ, SZ);\n");
            }
        }
        //(else part)
        writer.write("\telse return AXIS_FAIL;\n");
        //end of method
        writer.write("}\n\n");
    }

    /**
     * This method genarate methods that wraps the each method of the service
     * @param methodName
     * @param params
     * @param outparam
     * @throws IOException
     */

    public void writeMethodInWrapper(MethodInfo minfo)
        throws WrapperFault, IOException
    {
        boolean isAllTreatedAsOutParams = false;
        ParameterInfo returntype = null;
        int noOfOutParams = minfo.getOutputParameterTypes().size();
        if (0 == noOfOutParams)
        {
            returntype = null;
        }
        else
            if (1 == noOfOutParams)
            {
                returntype =
                    (ParameterInfo) minfo
                        .getOutputParameterTypes()
                        .iterator()
                        .next();
            }
            else
            {
                isAllTreatedAsOutParams = true;
            }
        Collection params = minfo.getInputParameterTypes();
        String methodName = minfo.getMethodname();
        Type retType = null;
        String outparamType = null;
        String outparamTypeName = null;
        boolean returntypeissimple = false;
        boolean returntypeisarray = false;
        boolean aretherearrayparams = false;
        if (returntype != null)
        {
            outparamTypeName =
                WrapperUtils.getClassNameFromParamInfoConsideringArrays(
                    returntype,
                    wscontext);
            retType =
                wscontext.getTypemap().getType(returntype.getSchemaName());
            if (retType != null)
            {
                returntypeisarray = retType.isArray();
                if (CUtils.isSimpleType(retType.getLanguageSpecificName()))
                {
                    returntypeissimple = true;
                }
            }
        }
        String paramTypeName;
        String returnParamName;
        ArrayList paramsB = new ArrayList(params);
        Type type;

        writer.write(
            "\n/*forward declaration for the c method " + methodName + " */\n");
        if (returntype == null)
        {
            writer.write("extern void " + methodName + "(");
        }
        else
        {
            writer.write("extern " + outparamTypeName + " " + methodName + "(");
        }

        for (int i = 0; i < paramsB.size(); i++)
        {
            if (i > 0)
                writer.write(",");
            paramTypeName =
                WrapperUtils.getClassNameFromParamInfoConsideringArrays(
                    (ParameterInfo) paramsB.get(i),
                    wscontext);
            writer.write(paramTypeName);
            if ((type =
                wscontext.getTypemap().getType(
                    ((ParameterInfo) paramsB.get(i)).getSchemaName()))
                != null
                && type.isArray())
            {
                aretherearrayparams = true;
            }
        }
        if (isAllTreatedAsOutParams)
        {
            ArrayList paramsC = (ArrayList) minfo.getOutputParameterTypes();
            for (int i = 0; i < paramsC.size(); i++)
            {
                type =
                    wscontext.getTypemap().getType(
                        ((ParameterInfo) paramsC.get(i)).getSchemaName());
                writer.write(
                    ", AXIS_OUT_PARAM "
                        + WrapperUtils
                            .getClassNameFromParamInfoConsideringArrays(
                            (ParameterInfo) paramsC.get(i),
                            wscontext)
                        + "*");
            }
        }
        writer.write(");\n");
        writer.write("\n/*\n");
        writer.write(" * This method wrap the service method \n");
        writer.write(" */\n");
        //method signature
        writer.write(
            "int "
                + methodName
                + CUtils.WRAPPER_METHOD_APPENDER
                + "(IWrapperSoapDeSerializer DZ, IWrapperSoapSerializer SZ)\n{\n");
        writer.write("\tint nStatus;\n");
        for (int i = 0; i < paramsB.size(); i++)
        {
            paramTypeName =
                WrapperUtils.getClassNameFromParamInfoConsideringArrays(
                    (ParameterInfo) paramsB.get(i),
                    wscontext);
            writer.write("\t" + paramTypeName + " v" + i + ";\n");
        }
        if (returntype != null)
        {
            writer.write("\t" + outparamTypeName + " ret;\n");
        }
        if (aretherearrayparams)
        {
            writer.write("\tAxis_Array array;\n");
        }
        // Multiples parameters so fill the methods prototype
        if (isAllTreatedAsOutParams)
        {
            ArrayList paramsC = (ArrayList) minfo.getOutputParameterTypes();
            for (int i = 0; i < paramsC.size(); i++)
            {
                type =
                    wscontext.getTypemap().getType(
                        ((ParameterInfo) paramsC.get(i)).getSchemaName());
                writer.write(
                    "\t"
                        + WrapperUtils
                            .getClassNameFromParamInfoConsideringArrays(
                            (ParameterInfo) paramsC.get(i),
                            wscontext)
                        + " out"
                        + i
                        + ";\n");
            }
        }
        writer.write(
            "\tif (AXIS_SUCCESS != DZ._functions->checkMessageBody(DZ._object, \""
                + methodName
                + "\", \""
                + wscontext.getWrapInfo().getTargetNameSpaceOfWSDL()
                + "\")) return AXIS_FAIL;\n");
        writer.write(
            "\tSZ._functions->createSoapMethod(SZ._object, \""
                + methodName
                + "Response\", \""
                + wscontext.getWrapInfo().getTargetNameSpaceOfWSDL()
                + "\");\n");
        //create and populate variables for each parameter
        for (int i = 0; i < paramsB.size(); i++)
        {
            paramTypeName = ((ParameterInfo) paramsB.get(i)).getLangName();
            if ((CUtils
                .isSimpleType(((ParameterInfo) paramsB.get(i)).getLangName())))
            {
                //for simple types	
                writer.write(
                    "\tv"
                        + i
                        + " = DZ._functions->"
                        + CUtils.getParameterGetValueMethodName(
                            paramTypeName,
                            false)
                        + "(DZ._object, 0, 0);\n");
            }
            else
                if ((type =
                    this.wscontext.getTypemap().getType(
                        ((ParameterInfo) paramsB.get(i)).getSchemaName()))
                    != null
                    && type.isArray())
                {
                    QName qname = WrapperUtils.getArrayType(type).getName();
                    String containedType = null;
                    if (CUtils.isSimpleType(qname))
                    {
                        containedType = CUtils.getclass4qname(qname);
                        writer.write(
                            "\tarray = DZ._functions->getBasicArray(DZ._object, "
                                + CUtils.getXSDTypeForBasicType(containedType)
                                + ", 0, 0);\n");
                        writer.write(
                            "\tmemcpy(&v"
                                + i
                                + ", &array, sizeof(Axis_Array));\n");
                    }
                    else
                    {
                        containedType = qname.getLocalPart();
                        writer.write(
                            "\tarray = DZ._functions->getCmplxArray(DZ._object, (void*)Axis_DeSerialize_"
                                + containedType
                                + "\n\t\t, (void*)Axis_Create_"
                                + containedType
                                + ", (void*)Axis_Delete_"
                                + containedType
                                + "\n\t\t, (void*)Axis_GetSize_"
                                + containedType
                                + ", Axis_TypeName_"
                                + containedType
                                + ", Axis_URI_"
                                + containedType
                                + ");\n");
                        writer.write(
                            "\tmemcpy(&v"
                                + i
                                + ", &array, sizeof(Axis_Array));\n");
                    }
                }
                else
                {
                    //for complex types 
                    writer.write(
                        "\tv"
                            + i
                            + " = ("
                            + paramTypeName
                            + "*)DZ._functions->getCmplxObject(DZ._object, (void*)Axis_DeSerialize_"
                            + paramTypeName
                            + "\n\t\t, (void*)Axis_Create_"
                            + paramTypeName
                            + ", (void*)Axis_Delete_"
                            + paramTypeName
                            + "\n\t\t, Axis_TypeName_"
                            + paramTypeName
                            + ", Axis_URI_"
                            + paramTypeName
                            + ");\n");
                }
        }
        writer.write(
            "\tif (AXIS_SUCCESS != (nStatus = DZ._functions->getStatus(DZ._object))) return nStatus;\n");
        if (returntype != null)
        {
            /* Invoke the service when return type not void */
            writer.write("\tret = " + methodName + "(");
            if (0 < paramsB.size())
            {
                for (int i = 0; i < paramsB.size() - 1; i++)
                {
                    writer.write("v" + i + ",");
                }
                writer.write("v" + (paramsB.size() - 1));
            }
            writer.write(");\n");
            /* set the result */
            if (returntypeissimple)
            {
                writer.write(
                    "\treturn SZ._functions->addOutputParam(SZ._object, \""
                        + methodName
                        + "Return\", (void*)&ret, "
                        + CUtils.getXSDTypeForBasicType(outparamTypeName)
                        + ");\n");
            }
            else
                if (returntypeisarray)
                {
                    QName qname = WrapperUtils.getArrayType(retType).getName();
                    String containedType = null;
                    if (CUtils.isSimpleType(qname))
                    {
                        containedType = CUtils.getclass4qname(qname);
                        writer.write(
                            "\treturn SZ._functions->addOutputBasicArrayParam(SZ._object, (Axis_Array*)(&ret),"
                                + CUtils.getXSDTypeForBasicType(containedType)
                                + ", \""
                                + methodName
                                + "Return\");\n");
                    }
                    else
                    {
                        containedType = qname.getLocalPart();
                        writer.write(
                            "\treturn SZ._functions->addOutputCmplxArrayParam(SZ._object, (Axis_Array*)(&ret),"
                                + "(void*) Axis_Serialize_"
                                + containedType
                                + ", (void*) Axis_Delete_"
                                + containedType
                                + ", (void*) Axis_GetSize_"
                                + containedType
                                + ", \""
                                + methodName
                                + "Return\", Axis_URI_"
                                + containedType
                                + ");\n");
                    }
                }
                else
                {
                    //complex type
                    outparamTypeName = returntype.getLangName();
                    //need to have complex type name without *
                    writer.write(
                        "\treturn SZ._functions->addOutputCmplxParam(SZ._object, ret, (void*)Axis_Serialize_"
                            + outparamTypeName
                            + ", (void*)Axis_Delete_"
                            + outparamTypeName
                            + ", \""
                            + methodName
                            + "Return\", Axis_URI_"
                            + outparamTypeName
                            + ");\n");
                }
        }
        else
            if (isAllTreatedAsOutParams)
            {
                writer.write("\t" + methodName + "(");
                if (0 < paramsB.size())
                {
                    for (int i = 0; i < paramsB.size(); i++)
                    {
                        writer.write("v" + i + ",");
                    }
                }
                ArrayList paramsC = (ArrayList) minfo.getOutputParameterTypes();
                for (int i = 0; i < paramsC.size() - 1; i++)
                {
                    writer.write("&out" + i + ",");
                }
                writer.write("&out" + (paramsC.size() - 1));
                writer.write(");\n");
                paramsC = (ArrayList) minfo.getOutputParameterTypes();
                for (int i = 0; i < paramsC.size(); i++)
                {
                    retType =
                        wscontext.getTypemap().getType(
                            ((ParameterInfo) paramsC.get(i)).getSchemaName());
                    if (retType != null)
                    {
                        outparamType = retType.getLanguageSpecificName();
                        returntypeisarray = retType.isArray();
                    }
                    else
                    {
                        outparamType = returntype.getLangName();
                    }
                    returntypeissimple = CUtils.isSimpleType(outparamType);
                    returnParamName =
                        ((ParameterInfo) paramsC.get(i)).getParamName();
                    if (returntypeissimple)
                    {
                        writer.write(
                            "\tSZ._functions->addOutputParam(SZ._object, \""
                                + returnParamName
                                + "\", (void*)&out"
                                + i
                                + ", "
                                + CUtils.getXSDTypeForBasicType(outparamType)
                                + ");\n");
                    }
                    else
                        if (returntypeisarray)
                        {
                            QName qname =
                                WrapperUtils.getArrayType(retType).getName();
                            String containedType = null;
                            if (CUtils.isSimpleType(qname))
                            {
                                containedType = CUtils.getclass4qname(qname);
                                writer.write(
                                    "\tSZ._functions->addOutputBasicArrayParam(SZ._object, (Axis_Array*)(&out"
                                        + i
                                        + "), "
                                        + CUtils.getXSDTypeForBasicType(
                                            containedType)
                                        + ", \""
                                        + returnParamName
                                        + "\");\n");
                            }
                            else
                            {
                                containedType = qname.getLocalPart();
                                writer.write(
                                    "\tSZ._functions->addOutputCmplxArrayParam(SZ._object, (Axis_Array*)(&out"
                                        + i
                                        + "),"
                                        + "(void*) Axis_Serialize_"
                                        + containedType
                                        + ", (void*) Axis_Delete_"
                                        + containedType
                                        + ", (void*) Axis_GetSize_"
                                        + containedType
                                        + ", \""
                                        + returnParamName
                                        + "\", Axis_URI_"
                                        + containedType
                                        + ");\n");
                            }
                        }
                        else
                        {
                            //complex type
                            writer.write(
                                "\tSZ._functions->addOutputCmplxParam(SZ._object, out"
                                    + i
                                    + ", (void*)Axis_Serialize_"
                                    + outparamType
                                    + ", (void*)Axis_Delete_"
                                    + outparamType
                                    + ", \""
                                    + returnParamName
                                    + "\", Axis_URI_"
                                    + outparamType
                                    + ");\n");
                        }
                }
                writer.write("\treturn AXIS_SUCCESS;\n");
            }
            else
            { //method does not return anything
                /* Invoke the service when return type is void */
                writer.write("\t" + methodName + "(");
                if (0 < paramsB.size())
                {
                    for (int i = 0; i < paramsB.size() - 1; i++)
                    {
                        writer.write("v" + i + ",");
                    }
                    writer.write("v" + (paramsB.size() - 1));
                }
                writer.write(");\n");
                writer.write("\treturn AXIS_SUCCESS;\n");
            }
        //write end of method
        writer.write("}\n");
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.cpp.CPPClassWriter#writeGlobalCodes()
     */
    protected void writeGlobalCodes() throws WrapperFault
    {
        Iterator types = wscontext.getTypemap().getTypes().iterator();
        HashSet typeSet = new HashSet();
        String typeName;
        Type type;
        try
        {
            while (types.hasNext())
            {
                type = (Type) types.next();
                if (type.isArray())
                    continue;
                typeName = type.getLanguageSpecificName();
                typeSet.add(typeName);
            }
            Iterator itr = typeSet.iterator();
            while (itr.hasNext())
            {
                typeName = itr.next().toString();
                writer.write(
                    "extern int Axis_DeSerialize_"
                        + typeName
                        + "("
                        + typeName
                        + "* param, IWrapperSoapDeSerializer* pDZ);\n");
                writer.write(
                    "extern void* Axis_Create_"
                        + typeName
                        + "("
                        + typeName
                        + " * param, bool bArray, int nSize);\n");
                writer.write(
                    "extern void Axis_Delete_"
                        + typeName
                        + "("
                        + typeName
                        + "* param, bool bArray, int nSize);\n");
                writer.write(
                    "extern int Axis_Serialize_"
                        + typeName
                        + "("
                        + typeName
                        + "* param, IWrapperSoapSerializer* pSZ, bool bArray);\n");
                writer.write("extern int Axis_GetSize_" + typeName + "();\n\n");
            }
        }
        catch (IOException e)
        {
            throw new WrapperFault(e);
        }
    }
}
