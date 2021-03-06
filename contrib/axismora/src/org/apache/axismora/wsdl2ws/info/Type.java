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
 * This calss represent the Custom Complex type in the service.
 * This Class is the  representation of the WSDL Schema type. The class name is given in the
 * context of the xml QName. It is up to the user to make sense of the QName* 
 * 
 * @author Srianth Perera (hemapani@opensource.lk)
 */
package org.apache.axismora.wsdl2ws.info;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axismora.wsdl2ws.WrapperConstants;
import org.apache.axismora.wsdl2ws.WrapperUtils;
import org.apache.axismora.wsdl2ws.c.CUtils;
import org.apache.axismora.wsdl2ws.cpp.CPPUtils;

public class Type {
    /* max no of attribs expected in a type */
    private static final int MAXIMUM_NO_ATTRIBS = 101;
    private QName name;

	/**
	  * If the specified node represents a supported JAX-RPC enumeration,
	  * a Vector is returned which contains the base type and the enumeration values.
	  * The first element in the vector is the base type (an TypeEntry).
	  * Subsequent elements are values (Strings).
	  * If this is not an enumeration, null is value.
	  */
    private Vector enumerationdata;

    /* This can be null */
    private String languageSpecificName;
	/* element names and the type of the elements (QName,ElementInfo)*/
    private Hashtable elements;
	/* attribute names and the type of the attributes (QName,QName)*/
    private Hashtable attributes;
    /* has the attributes are specified with order <sequence> in the schema */
    private boolean hasOrder;
    /*if order presents the order is set in the vector */
    private Vector attribOrder;
    /* weather the type is Array */
    private boolean isArray;
    
    private boolean canThisOccuredmoreThanOnceAllTheTime = false;
    //to handle <xsd:element name="three" type="typens:enum" maxOccurs="unbounded" />
    //types at the top level. But this is not allowed in the Schema spec. 
    
    
    private String language;

    public Type(QName name, String languageSpecificNamein, boolean hasOrder,String language) {
        this.languageSpecificName = languageSpecificNamein;
        this.hasOrder = hasOrder;
        this.name = name;
		elements = new Hashtable();
		attributes = new Hashtable();
        if(language == null)
			this.language = WrapperConstants.LANGUAGE_JAVA;
        else
        	this.language = language;
        	
        // if the language specific name does not specified try weather is it a simple type  	 
        if(languageSpecificNamein == null){
        	if(WrapperConstants.LANGUAGE_CPP.equalsIgnoreCase(this.language))
				this.languageSpecificName = CPPUtils.getclass4qname(name);
			else if(WrapperConstants.LANGUAGE_C.equalsIgnoreCase(this.language))
					this.languageSpecificName = CUtils.getclass4qname(name);
			else
				this.languageSpecificName = TypeMap.getBasicTypeClass4qname(name);
			 
			
        }
        
        //if it is not a simple type genarate the name using usual QName -> language specific name mapping
        if(this.languageSpecificName == null)
            	this.languageSpecificName = qname2LSN();
		this.languageSpecificName = this.languageSpecificName.replaceAll("/","_");  
		this.languageSpecificName = this.languageSpecificName.replaceAll(":","_");
		this.languageSpecificName = this.languageSpecificName.replaceAll(">","_");
		
       	this.attribOrder = new Vector();
    }

    /**
     * @return QName
     */
    public QName getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(QName name) {
        this.name = name;
    }

    public Enumeration getAttributeNames() {
       	return this.attributes.keys();
    }


/**
 * The Type take the attributes name to lowercase when add, If there is two names like "Name" and "name"
 * they will convert to "name"  Is that acceptable ....  
 */
    public void setTypeForAttributeName(String attribName, Type type) {
		attribName = TypeMap.resoleveWSDL2LanguageNameClashes(attribName,this.language);
        if (hasOrder)
            this.attribOrder.add(attribName);
        this.attributes.put(attribName, type);
    }

	public Type getTypForAttribName(String attribName) {
		return (Type) this.attributes.get(attribName);
	}

	public Enumeration getElementnames() {
		return this.elements.keys();
	}


/**
 * The Type take the attributes name to lowercase when add, If there is two names like "Name" and "name"
 * they will convert to "name"  Is that acceptable ....  
 */
	public void setTypeNameForElementName(ElementInfo element) {
		String attribName = TypeMap.resoleveWSDL2LanguageNameClashes(element.getName().getLocalPart(),this.language);
		if (hasOrder)
			this.attribOrder.add(attribName);
		this.elements.put(attribName, element);
	}

	public ElementInfo getElementForElementName(String attribName) {
		return (ElementInfo) this.elements.get(attribName);
	}


    public void setAttribOrder(Vector order) {
        this.attribOrder = order;
    }





    public boolean hasOrder() {
        return this.hasOrder;
    }

    public String getLanguageSpecificName() {
        return languageSpecificName;
    }

    public void setLanguageSpecificName(String languageSpecificName) {
        this.languageSpecificName = languageSpecificName;
    }
	/**
	 *  This mrthod define the standread conversion from qname to language spesific name
	 *  @return language specific name 
	 */ 
    protected String qname2LSN(){
       String nsuri = this.name.getNamespaceURI();
       if(nsuri == null) return  this.name.getLocalPart();
 	
	   if(language.equalsIgnoreCase(WrapperConstants.LANGUAGE_CPP)){
	   	 /* if it is CPP the namespace is neglected fr time been */
		 return this.name.getLocalPart(); 
	   }else if (language.equalsIgnoreCase(WrapperConstants.LANGUAGE_C)){
		 return this.name.getLocalPart();
	   }else			   
         return WrapperUtils.firstCharacterToLowercase(WrapperUtils.nsURI2packageName(nsuri)) +"."+ WrapperUtils.capitalizeFirstCaractor(this.name.getLocalPart());
    }
	/**
	 * @return
	 */
	public boolean isArray() {
		return isArray;
	}

	/**
	 * @param b
	 */
	public void setArray(boolean b) {
		isArray = b;
	}

	public boolean isContainedType(Type containedType){
		Iterator ntype = this.attributes.values().iterator();
		QName typeName;
		while(ntype.hasNext()){
			typeName = (QName)ntype.next();
			if(typeName.equals(containedType.name)){
				return true;
			}
		}	
		Iterator nelements = this.elements.values().iterator();
		while(nelements.hasNext()){
			typeName = (QName)nelements.next();
			if(typeName.equals(containedType.name)){
				return true;
			}
		}		
		return false;
	}
    /**
     * @return
     */
    public boolean isCanThisOccuredmoreThanOnceAllTheTime() {
        return canThisOccuredmoreThanOnceAllTheTime;
    }

    /**
     * @param b
     */
    public void setCanThisOccuredmoreThanOnceAllTheTime(boolean b) {
        canThisOccuredmoreThanOnceAllTheTime = b;
    }
    

    /**
	  * If the specified node represents a supported JAX-RPC enumeration,
	  * a Vector is returned which contains the base type and the enumeration values.
	  * The first element in the vector is the base type (an TypeEntry).
	  * Subsequent elements are values (Strings).
	  * If this is not an enumeration, null is value.
      * @return
      */
    public Vector getEnumerationdata() {
        return enumerationdata;
    }

    /**
     * @param vector
     */
    public void setEnumerationdata(Vector vector) {
        enumerationdata = vector;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
       String str = "---------"+this.name+"------------\n" +
       	"languageSpecificName = " + this.languageSpecificName +"\n";
       	if(enumerationdata != null){
			str = str + "enumerationType = "+((TypeEntry)enumerationdata.get(0)).getQName()+"\n(";
			for(int i = 1;i<enumerationdata.size();i++)	
				str = str +","+ enumerationdata.get(i);
			str = str + ")\n";
       	}else{
	       	str =str +"isArray ="+isArray+"\n";
	       	str = str + "Elements[\n";
	       	Iterator c = elements.values().iterator();
			while(c.hasNext())	
					str = str +","+ c.next()+"\n";
	       	str = str + "]\n";
	       	
			c = attributes.keySet().iterator();
			str = str + "Attributes[\n";

			while(c.hasNext()){	
				String name = (String)c.next();
					str = str +",("+ name+","+attributes.get(name)+")";
			}		
			str = str + "]\n";
	       	
        }
		str = str + "------------------------------------------------------\n";
       
        return str;
    }

}
