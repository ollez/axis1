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
package org.apache.axis.wsdl.wsdl2ws.cpp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.WrapperUtils;
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;
/**
 * @author nithya
 *
 */
public class ExceptionHeaderWriter extends HeaderFileWriter{
	
	private WebServiceContext wscontext;
	private ArrayList methods;		

	String faultInfoName;
	String langName;
	String faultType;
	   
	public ExceptionHeaderWriter(WebServiceContext wscontext,String faultInfoName,String langName,String faultType)throws WrapperFault{
	    //super(WrapperUtils.getClassNameFromFullyQualifiedName(wscontext.getSerInfo().getQualifiedServiceName()));
	    super("Axis"+faultInfoName+"Exception");//damitha
            System.out.println("faultInfoName is:"+faultInfoName);
	    this.wscontext = wscontext;
	    this.methods = wscontext.getSerInfo().getMethods();
	    this.faultInfoName ="Axis"+faultInfoName+"Exception";
		this.langName =langName;
		this.faultType =faultType;
    }

     protected File getFilePath() throws WrapperFault {
      	String targetOutputLocation = this.wscontext.getWrapInfo().getTargetOutputLocation();
	    if(targetOutputLocation.endsWith("/"))
		       targetOutputLocation = targetOutputLocation.substring(0, targetOutputLocation.length() - 1);
	     new File(targetOutputLocation).mkdirs();
	    String fileName = targetOutputLocation + "/" + faultInfoName + ".h";
	    return new File(fileName);
    }

	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writePreprocssorStatements()
		 */
		protected void writePreprocssorStatements() throws WrapperFault {
			try{
				//writer.write("#include \""+faultInfoName+".h\"\n\n");//damitha
				//writer.write("#include <axis/server/AxisException.h>\n\n");//damitha
				writer.write("#include <string>\n");
				writer.write("#include <exception>\n");
				writer.write("#include <axis/server/AxisException.h>\n");
				writer.write("#include \""+langName+".h\"\n\n");
				writer.write("using namespace std;\n");				
			}catch(IOException e){
				throw new WrapperFault(e);
			}
		}

	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeClassComment()
		 */
		protected void writeClassComment() throws WrapperFault {
			try{
				writer.write("/*\n");
				writer.write(" * This is the Client Stub Class genarated by the tool WSDL2Ws\n");
				writer.write(" * "+faultInfoName+".h: interface for the "+faultInfoName+"class.\n");
				writer.write(" *\n");
				writer.write(" */\n");
			}catch(IOException e){
				throw new WrapperFault(e);
			}
		}
		
	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeConstructors()
		 */
		protected void writeConstructors() throws WrapperFault {
			try{
			writer.write("public:\n\t"+faultInfoName+"();\n");
			writer.write("\t"+faultInfoName+"("+faultType+" pFault);\n");
			writer.write("\t"+faultInfoName+"(int iExceptionCode);\n");
			writer.write("\t"+faultInfoName+"(exception* e);\n");
			writer.write("\t"+faultInfoName+"(exception* e, int iExceptionCode);\n");
			
			
			}catch(IOException e){
				throw new WrapperFault(e);
			}
		}
		
	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeDistructors()
		 */
		protected void writeDistructors() throws WrapperFault {
			try{
			writer.write("\tvirtual ~"+faultInfoName+"() throw();\n");
			}catch(IOException e){
				throw new WrapperFault(e);
			}
		}
	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeMethods()
		 */
		protected void writeMethods() throws WrapperFault {
			try{
				writer.write("\t const char* what() throw();\n");
				writer.write("\t const int getExceptionCode();\n");
				writer.write("\t const string getMessage(exception* e);\n");
				writer.write("\t const string getMessage(int iExceptionCode);\n");
				writer.write("private:\n\t void processException(exception* e);\n");
				writer.write("\t void processException("+faultType+" pFault);\n");
				writer.write("\t void processException(exception* e, int iExceptionCode);\n");
				writer.write("\t void processException(int iExceptionCode);\n");//damitha
				writer.write("\t string m_sMessage;\n");
				writer.write("\t int m_iExceptionCode;\n\n");								
			}catch (Exception e) {
			  e.printStackTrace();
			  throw new WrapperFault(e);
	    	}
		}
		
	protected String getFileType()
		{
			return "Exception";	// must change accordingly
		}
        protected String getExtendsPart(){return ": public AxisException";}//damitha

}//end of main
 
 
//class AxisDivByZeroException : public AxisException
//{

