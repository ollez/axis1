/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 
/**
 * @author Srinath Perera(hemapani@openource.lk)
 * @author Susantha Kumara(susantha@opensource.lk, skumara@virtusa.com)
 */

package org.apache.axis.wsdl.wsdl2ws.cpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.wsdl.wsdl2ws.WrapperFault;
import org.apache.axis.wsdl.wsdl2ws.info.Type;
import org.apache.axis.wsdl.wsdl2ws.info.WebServiceContext;

public class ParmHeaderFileWriter extends ParamWriter{
	public ParmHeaderFileWriter(WebServiceContext wscontext,Type type)throws WrapperFault{
		super(wscontext,type);
	}
	public void writeSource()throws WrapperFault{
	   try{
			this.writer = new BufferedWriter(new FileWriter(getFilePath(), false));
			writeClassComment();
			// if this headerfile not defined define it 
			this.writer.write("#if !defined(__"+classname.toUpperCase()+"_"+getFileType().toUpperCase()+"_H__INCLUDED_)\n");
			this.writer.write("#define __"+classname.toUpperCase()+"_"+getFileType().toUpperCase()+"_H__INCLUDED_\n\n");
			writePreprocssorStatements();
			this.writer.write("class "+classname+"\n{\n");
			writeAttributes();
			this.writer.write("};\n\n");
			this.writer.write("#endif // !defined(__"+classname.toUpperCase()+"_"+getFileType().toUpperCase()+"_H__INCLUDED_)\n");
			writer.flush();
			writer.close();
			System.out.println(getFilePath().getAbsolutePath() + " created.....");
		} catch (IOException e) {
			e.printStackTrace();
			throw new WrapperFault(e);
		}
	}
	
	protected void writeAttributes()throws WrapperFault{
		  if(type.isArray()) return;
		  try{
			writer.write("public:\n");
			  for(int i=0;i<attribs.length;i++){
				  //if((t = wscontext.getTypemap().getType(new QName(attribs[i][2],attribs[i][3])))!= null && t.isArray()) continue;
				  writer.write("\t"+getCorrectParmNameConsideringArraysAndComplexTypes(new QName(attribs[i][2],attribs[i][3]),attribs[i][1])+" "+attribs[i][0]+";\n");
			  }    
		  } catch (IOException e) {
			   throw new WrapperFault(e);
		  }
	  }
	  
	protected void writeConstructors()throws WrapperFault{}
	   
	protected void writeDistructors() throws WrapperFault {}
   
	protected void writeMethods()throws WrapperFault{}

	protected File getFilePath() throws WrapperFault {
		String targetOutputLocation = this.wscontext.getWrapInfo().getTargetOutputLocation();
		if(targetOutputLocation.endsWith("/"))
			targetOutputLocation = targetOutputLocation.substring(0, targetOutputLocation.length() - 1);
		new File(targetOutputLocation).mkdirs();
		String fileName = targetOutputLocation + "/" + this.classname + ".h";
		return new File(fileName);
	}
	/* (non-Javadoc)
	 * @see org.apache.axis.wsdl.wsdl2ws.cpp.BasicFileWriter#writePreprocssorStatements()
	 */
	protected void writePreprocssorStatements() throws WrapperFault {
	  try{
		Type atype;
		Iterator types = this.wscontext.getTypemap().getTypes().iterator();
		writer.write("#include <AxisUserAPI.h>\n\n");
		HashSet typeSet = new HashSet();
		while(types.hasNext()){
			atype = (Type)types.next();
			if(!(atype.equals(this.type))){
				if (this.type.isContainedType(atype)){ 
					typeSet.add(atype.getLanguageSpecificName());
				}
			}
		}		
		Iterator itr = typeSet.iterator();
		while(itr.hasNext())
		{
			writer.write("#include \""+itr.next().toString()+".h\"\n");
		}		
			
		//Local name and the URI for the type
		writer.write("//Local name and the URI for the type\n");
		writer.write("static const char* Axis_URI_"+classname+" = \""+type.getName().getNamespaceURI()+"\";\n");
		writer.write("static const char* Axis_TypeName_"+classname+" = \""+type.getName().getLocalPart()+"\";\n\n");
	  }catch(IOException e){
	  	throw new WrapperFault(e);
	  }
	}
	protected String getFileType()
	{
		return "Param";	
	}
}
