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

import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.WrapperUtils;
import org.apache.axis.wsdl.wsdl2ws.CUtils;
import org.apache.axis.wsdl.wsdl2ws.info.MethodInfo;
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;

public class WrapHeaderWriter extends HeaderFileWriter{
	private WebServiceContext wscontext;
	private ArrayList methods;	
	public WrapHeaderWriter(WebServiceContext wscontext)throws WrapperFault{
		super(WrapperUtils.getClassNameFromFullyQualifiedName(wscontext.getSerInfo().getQualifiedServiceName()+CUtils.WRAPPER_NAME_APPENDER));
		this.wscontext = wscontext;
		this.methods = wscontext.getSerInfo().getMethods();
	}
	protected File getFilePath() throws WrapperFault {
		String targetOutputLocation = this.wscontext.getWrapInfo().getTargetOutputLocation();
		if(targetOutputLocation.endsWith("/"))
			targetOutputLocation = targetOutputLocation.substring(0, targetOutputLocation.length() - 1);
		new File(targetOutputLocation).mkdirs();
		String fileName = targetOutputLocation + "/" + classname + ".h";
		return new File(fileName);
	}

	protected void writeClassComment() throws WrapperFault {
			try{
				writer.write("/*\n");
				writer.write(" * This is the C wrapper header file genarated by the WSDL2Ws tool\n");
				writer.write(" *\n");
				writer.write(" */\n");
			}catch(IOException e){
				throw new WrapperFault(e);
			}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeMethods()
	 */
	protected void writeMethods() throws WrapperFault {
		try{
			writer.write("/*implementation of BasicHandler interface*/\n");
			writer.write("int AXISCALL "+classname+"_Invoke(void*p, void* pMsg);\n");
			writer.write("void AXISCALL "+classname+"_OnFault(void*p, void* pMsg);\n");
			writer.write("int AXISCALL "+classname+"_Init(void*p);\n");
			writer.write("int AXISCALL "+classname+"_Fini(void*p);\n");
			writer.write("int AXISCALL "+classname+"_GetType(void*p);\n");
			writer.write("AXIS_BINDING_STYLE AXISCALL "+classname+"_GetBindingStyle(void*p);\n");
			writer.write("/*Methods corresponding to the web service methods*/\n");
			MethodInfo minfo;
			for (int i = 0; i < methods.size(); i++) {
					 minfo = (MethodInfo)methods.get(i);
					 writer.write("int "+minfo.getMethodname()+CUtils.WRAPPER_METHOD_APPENDER+"(IWrapperSoapDeSerializer DZ, IWrapperSoapSerializer SZ);");
					 writer.write("\n");
			}
		}catch(IOException e){
			throw new WrapperFault(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writePreprocssorStatements()
	 */
	protected void writePreprocssorStatements() throws WrapperFault {
		try{
			writer.write("#include <axis/server/IMessageData.h>\n");
			writer.write("#include <axis/server/AxisWrapperAPI.h>\n\n");
		}catch(IOException e){
			throw new WrapperFault(e);
		}
	}
	protected void writeAttributes()throws WrapperFault{}
}
