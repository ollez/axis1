/*
 *   Copyright 2003-2004 The Apache Software Foundation.
// (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.axis.wsdl.wsdl2ws.CUtils;
import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.WrapperUtils;
import org.apache.axis.wsdl.wsdl2ws.info.MethodInfo;
import org.apache.axis.wsdl.wsdl2ws.info.ParameterInfo;
import org.apache.axis.wsdl.wsdl2ws.info.Type;
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;

public class ServiceWriter extends CFileWriter
{
    protected ArrayList methods;

    /**
     * @param wscontext
     * @throws WrapperFault
     */
    public ServiceWriter(WebServiceContext wscontext) throws WrapperFault
    {
        super(
            WrapperUtils.getClassNameFromFullyQualifiedName(
                wscontext.getSerInfo().getQualifiedServiceName()));
        this.wscontext = wscontext;
        this.methods = wscontext.getSerInfo().getMethods();
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.BasicFileWriter#writeClassComment()
     */
    protected void writeClassComment() throws WrapperFault
    {
        try
        {
            writer.write("/*\n");
			writer.write(" * Copyright 2003-2006 The Apache Software Foundation.\n\n");
			writer.write(" *\n");
			writer.write(" * Licensed under the Apache License, Version 2.0 (the \"License\");\n");
			writer.write(" * you may not use this file except in compliance with the License.\n");
			writer.write(" * You may obtain a copy of the License at\n");
			writer.write(" *\n");
			writer.write(" *\t\thttp://www.apache.org/licenses/LICENSE-2.0\n");
			writer.write(" *\n");
			writer.write(" * Unless required by applicable law or agreed to in writing, software\n");
			writer.write(" * distributed under the License is distributed on an \"AS IS\" BASIS,\n");
			writer.write(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
			writer.write(" * See the License for the specific language governing permissions and\n");
			writer.write(" * limitations under the License.\n");
			writer.write(" *\n");
            writer.write(" * This file was auto-generated by the Axis C++ Web Service Generator (WSDL2Ws)\n");
            writer.write(" * This file contains definitions of the web service\n");
            writer.write(" */\n\n");
        }
        catch (IOException e)
        {
            throw new WrapperFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.BasicFileWriter#writeMethods()
     */
    protected void writeMethods() throws WrapperFault
    {
        MethodInfo minfo;
        try
        {
            writer.write("\n");
            for (int i = 0; i < methods.size(); i++)
            {
                minfo = (MethodInfo) this.methods.get(i);
                boolean isAllTreatedAsOutParams = false;
                ParameterInfo returntype = null;
                int noOfOutParams = minfo.getOutputParameterTypes().size();
                if (0 == noOfOutParams)
                {
                    returntype = null;
                    writer.write("void ");
                }
                else
                    if (1 == noOfOutParams)
                    {
                        returntype =
                            (ParameterInfo) minfo
                                .getOutputParameterTypes()
                                .iterator()
                                .next();
                        writer.write(
                            WrapperUtils
                                .getClassNameFromParamInfoConsideringArrays(
                                returntype,
                                wscontext)
                                + " ");
                    }
                    else
                    {
                        isAllTreatedAsOutParams = true;
                        writer.write("void ");
                    }
                writer.write(minfo.getMethodname() + "(");
                //write parameter names 
                Iterator params = minfo.getInputParameterTypes().iterator();
                if (params.hasNext())
                {
                    ParameterInfo fparam = (ParameterInfo) params.next();
                    writer.write(
                        WrapperUtils
                            .getClassNameFromParamInfoConsideringArrays(
                            fparam,
                            wscontext)
                            + " Value"
                            + 0);
                }
                for (int j = 1; params.hasNext(); j++)
                {
                    ParameterInfo nparam = (ParameterInfo) params.next();
                    writer.write(
                        ","
                            + WrapperUtils
                                .getClassNameFromParamInfoConsideringArrays(
                                nparam,
                                wscontext)
                            + " Value"
                            + j);
                }
                if (isAllTreatedAsOutParams)
                {
                    params = minfo.getOutputParameterTypes().iterator();
                    for (int j = 0; params.hasNext(); j++)
                    {
                        ParameterInfo nparam = (ParameterInfo) params.next();
                        writer.write(
                            ", AXISC_OUT_PARAM "
                                + WrapperUtils
                                    .getClassNameFromParamInfoConsideringArrays(
                                    nparam,
                                    wscontext)
                                + " *OutValue"
                                + j);
                    }
                }
                writer.write(")\n{\n}\n");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new WrapperFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.wsdl.wsdl2ws.BasicFileWriter#writePreprocessorStatements()
     */
    protected void writePreprocessorStatements() throws WrapperFault
    {
        try
        {
            Type atype;
            Iterator types = this.wscontext.getTypemap().getTypes().iterator();
            HashSet typeSet = new HashSet();

            String typeName = null;
            while (types.hasNext())
            {
                atype = (Type) types.next();
                typeName = WrapperUtils.getLanguageTypeName4Type(atype);
                if (null != typeName)
                    typeSet.add(typeName);
            }
            Iterator itr = typeSet.iterator();
            while (itr.hasNext())
            {
                writer.write(
                    "#include \""
                        + itr.next().toString()
                        + CUtils.C_HEADER_SUFFIX
                        + "\"\n");
            }
            writer.write("\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new WrapperFault(e);
        }
    }

}
