/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.ews.ws4j2ee.context.webservices.server.interfaces;

import org.apache.geronimo.ews.ws4j2ee.context.webservices.server.jaxb.XsdQNameType;



/**
 * This interface is published to abstract the SOAP header element in the webservices.xml which is a layer 4 elment.
 *
 */
public interface WSCFSOAPHeader {
	
	/**
	 * gets the local part of the soap header
	 * @return local part
	 */
	public String getLocalpart() ;
	
	/**
	 * Gets teh namespace of the SOAP header
	 * @return namespace
	 */
	public String getNamespaceURI() ;
	
	public XsdQNameType getJaxbSoapHeader();
}
