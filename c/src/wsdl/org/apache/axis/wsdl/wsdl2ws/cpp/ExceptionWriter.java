/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.axis.wsdl.wsdl2ws.cpp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//
//import javax.xml.namespace.QName;
//
//import org.apache.axis.wsdl.wsdl2ws.WrapperConstants;
import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.WrapperUtils;
//import org.apache.axis.wsdl.wsdl2ws.CUtils;
//import org.apache.axis.wsdl.wsdl2ws.info.MethodInfo;
//import org.apache.axis.wsdl.wsdl2ws.info.ParameterInfo;
//import org.apache.axis.wsdl.wsdl2ws.info.Type;
//import org.apache.axis.wsdl.wsdl2ws.info.FaultInfo; 
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;
/**
 * @author nithya
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExceptionWriter extends CPPExceptionClassWriter{
	   private WebServiceContext wscontext;
	   private ArrayList methods;		  
	   String faultInfoName;
	   String langName;
	   String faultType;
	   
		public ExceptionWriter(WebServiceContext wscontext,String faultInfoName)throws WrapperFault{
			super(WrapperUtils.getClassNameFromFullyQualifiedName(wscontext.getSerInfo().getQualifiedServiceName()));
			this.wscontext = wscontext;
			this.methods = wscontext.getSerInfo().getMethods();
			this.faultInfoName ="Axis"+faultInfoName+"Exception";
			//this.langName =langName;
			//this.faultType =faultType;			
		}
	
	    protected File getFilePath() throws WrapperFault {
			String targetOutputLocation = this.wscontext.getWrapInfo().getTargetOutputLocation();
			if(targetOutputLocation.endsWith("/"))
				targetOutputLocation = targetOutputLocation.substring(0, targetOutputLocation.length() - 1);
			new File(targetOutputLocation).mkdirs();
			String fileName = targetOutputLocation + "/" + faultInfoName + ".cpp";
		//	this.wscontext.addGeneratedFile(faultInfoName + ".cpp");
			return new File(fileName);
		}	

	protected File getFilePath(boolean useServiceName) throws WrapperFault {
		String targetOutputLocation = this.wscontext.getWrapInfo().getTargetOutputLocation();
		if(targetOutputLocation.endsWith("/"))
			targetOutputLocation = targetOutputLocation.substring(0, targetOutputLocation.length() - 1);
		new File(targetOutputLocation).mkdirs();

		String fileName = targetOutputLocation + "/" + faultInfoName + ".cpp";
		
		if( useServiceName)
		{
			fileName = targetOutputLocation + "/" + this.wscontext.getSerInfo().getServicename() + "_" + faultInfoName + ".cpp";
		}
		this.wscontext.addGeneratedFile( this.wscontext.getSerInfo().getServicename()+"_"+faultInfoName + ".cpp");
		return new File(fileName);
	}
		
	protected String getServiceName() throws WrapperFault {
		return wscontext.getSerInfo().getServicename();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writePreprocssorStatements()
	 */
	protected void writePreprocssorStatements() throws WrapperFault {
		try{
			if("AxisClientException".equals(faultInfoName))
			{
				writer.write("#include \""+getServiceName()+"_"+faultInfoName+".h\"\n\n");
			}
			else
			{
				writer.write("#include \""+faultInfoName+".h\"\n\n");
			}
			writer.write("#include <axis/server/AxisWrapperAPI.h>\n\n");
		}catch(IOException e){
			throw new WrapperFault(e);
		}
	}
		
	    protected void writeClassComment() throws WrapperFault {
				try{
					writer.write("/*\n");	
					writer.write(" * This file was auto-generated by the Axis C++ Web Service " +
						"Generator (WSDL2Ws)\n");
					writer.write(" * This file contains implementations of the "+getServiceName()+" Exception " +
						"class of the web service.\n");										
					writer.write(" */\n\n");

				}catch(IOException e){
					throw new WrapperFault(e);
				}
		}
		
	/* (non-Javadoc)
	 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeConstructors()
	 */
	protected void writeConstructors() throws WrapperFault {
		try{
			String faultName = faultInfoName;
		
			if("AxisClientException".equals(faultInfoName))
			{
				faultName = getServiceName()+"_"+faultInfoName;
			}

		writer.write(faultName+"::"+faultName+"()\n{\n");
		writer.write("/* This only serves the purpose of indicating that the \n");
		writer.write(" * service has thrown an excpetion \n");
		writer.write(" */ \n");
		writer.write("\tm_iExceptionCode = AXISC_SERVICE_THROWN_EXCEPTION; \n");
		writer.write("\tprocessException(m_iExceptionCode); \n");
		writer.write("}\n\n");
		
		writer.write(faultName+"::"+faultName+"(ISoapFault* pFault)\n");
		writer.write("{\n");
		writer.write("\tm_iExceptionCode = AXISC_SERVICE_THROWN_EXCEPTION;\n");
		writer.write("\tm_pISoapFault = pFault;\n"); // Fred Preston
		writer.write("\tprocessException(pFault);");
		writer.write("}\n\n");
		
		writer.write(faultName+"::"+faultName+"(int iExceptionCode)\n");
		writer.write("{\n\n");
		writer.write("\tm_iExceptionCode = iExceptionCode;\n");
		writer.write("\tprocessException (iExceptionCode);\n");
		writer.write("}\n\n");
		
		writer.write(faultName+"::"+faultName+"(exception* e)\n");
		writer.write("{\n");
		writer.write("\tprocessException (e);\n");
		writer.write("}\n\n");
		
		writer.write(faultName+"::"+faultName+"(exception* e,int iExceptionCode)\n");
		writer.write("{\n\n");
		writer.write("\tprocessException (e, iExceptionCode);\n");
		writer.write("}\n\n");	
	    }catch(IOException e){
			throw new WrapperFault(e);
		}	
	}
	
    	/* (non-Javadoc)
		 * @see org.apache.axis.wsdl.wsdl2ws.cpp.HeaderFileWriter#writeDistructors()
		 */
	  protected void writeDistructors() throws WrapperFault {
			try{
				String faultName = faultInfoName;
		
				if("AxisClientException".equals(faultInfoName))
				{
					faultName = getServiceName()+"_"+faultInfoName;
				}
			writer.write(faultName+"::~"+faultName+"() throw () \n{\n\tm_sMessage =\"\";\n}\n\n");
			}catch(IOException e){
				throw new WrapperFault(e);
			}
		}
		
	   protected void writeMethods() throws WrapperFault {	
	   try{	
		String faultName = faultInfoName;
		
		if("AxisClientException".equals(faultInfoName))
		{
			faultName = getServiceName()+"_"+faultInfoName;
		}
	       writer.write("void "+faultName+":: processException(exception* e, int iExceptionCode)\n");
	       writer.write("{\n");
	       writer.write("\tm_sMessage = getMessage (e) + getMessage (iExceptionCode);\n");
	       writer.write("}\n\n");

	       writer.write("void "+faultName+"::processException (ISoapFault* pFault)\n");
		   writer.write("{\n");
           writer.write("\t/*User can do something like deserializing the struct into a string*/\n");
		   writer.write("}\n\n");

		   writer.write("void "+faultName+"::processException(exception* e)\n");
		   writer.write("{\n");
		   writer.write("\tm_sMessage = getMessage (e);\n");
		   writer.write("}\n\n");

		    writer.write("void "+faultName+"::processException(int iExceptionCode)\n");
		    writer.write("{\n");
		    writer.write("\tm_sMessage = getMessage (iExceptionCode);\n");
		    writer.write("}\n\n");
		
	       writer.write("const string "+faultName+"::getMessage (exception* objException)\n");
	       writer.write("{\n");
	       writer.write("\tstring sMessage = objException->what();\n");
	       writer.write("\treturn sMessage;\n");
	       writer.write("}\n\n");

		   writer.write("const string "+faultName+"::getMessage (int iExceptionCode)\n");
		   writer.write("{\n");
		   writer.write("\tstring sMessage;\n");
		   writer.write("\tswitch(iExceptionCode)\n");
		   writer.write("\t{\n");
		   writer.write("\t\tcase AXISC_SERVICE_THROWN_EXCEPTION:\n");
		   writer.write("\t\tsMessage = \"The "+getServiceName()+" service has thrown an exception. see details\";\n");
		   writer.write("\t\tbreak;\n");
		   writer.write("\t\tdefault:\n");
		   writer.write("\t\tsMessage = \"Unknown Exception has occured in the "+getServiceName()+" service\";\n");
		   writer.write("\t}\n");
		   writer.write("return sMessage;\n");
	       writer.write("}\n\n");

		   writer.write("const char* "+faultName+"::what() throw ()\n");
	       writer.write("{\n");
		   writer.write("\treturn m_sMessage.c_str ();\n");
		   writer.write("}\n\n");
		   
      	   writer.write("const int "+faultName+"::getExceptionCode()");//damitha
	  	   writer.write("{\n");
		   writer.write("\treturn m_iExceptionCode;\n");
		   writer.write("}\n\n");

		writer.write("const ISoapFault* "+faultName+"::getFault()");//Fred Preston
		writer.write("{\n");
		writer.write("\treturn m_pISoapFault;\n");
		writer.write("}\n\n");
			  	     
	 }catch(IOException e){
					 throw new WrapperFault(e);
				 }
	   }	
}
