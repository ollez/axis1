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
package org.apache.wsdl.impl;

import org.apache.wsdl.ExtensionElement;
import org.apache.wsdl.WSDLTypes;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLTypesImpl extends ComponentImpl implements WSDLTypes {
    
    private HashMap extensionElements = new HashMap();

    /**
     * Sets the <code>ExtensionElement</code>s as a <code>HashMap</code>
     * @return
     */
    public HashMap getTypes() {
        return extensionElements;
    }
    
    /**
     * Returns all the <code>ExtensionElement</code>s as a <code>HashMap</code>
     * @param types
     */
    public void setTypes(HashMap types) {
        this.extensionElements = types;
    }
    
    /**
     * Adds the <code>ExtensionElement</code> to the map keyed with the <code>QName</code>     * 
     * @param qName
     * @param element
     */
    public void addElement(QName qName, ExtensionElement element){
        
        this.extensionElements.put(qName, element);
    }
    
    /**
     * Will return the Element with the given <code>QName</code>
     * Returns null if not found.
     * @param qName
     * @return
     */
    public ExtensionElement getElement(QName qName){
        return (ExtensionElement)this.extensionElements.get(qName);
    }
    
}