
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.axis.wsdlgen;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SOAPTypeMappingRegistry;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.utils.XMLUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.QName;

import com.ibm.wsdl.util.xml.DOMUtils;

/**
 *
 * <p>Description: </p> This class is used to recursively serializes a Java Class into
 * an XML Schema representation.
 *
 * It has utility methods to create a schema node, assosiate namespaces to the various types
 *
 *
 * @author unascribed
 */
public class Types {

    Definition def;
    Namespaces namespaces = null;
    TypeMappingRegistry reg;
    String targetNamespace;
    Element wsdlTypesElem = null;
    HashMap schemaTypes = null;
    HashMap schemaElementNames = null;

    /**
     * This class serailizes a <code>Class</code> to XML Schema. The constructor
     * provides the context for the streamed node within the WSDL document
     * @param def WSDL Definition Element to declare namespaces
     * @param doc Document element of the WSDL used to create child elements
     * @param reg TypeMappingRegistry to handle known types
     * @param namespaces user defined or autogenerated namespace and prefix maps
     * @param targetNamespace targetNamespace of the document
     */
    public Types(Definition def, TypeMappingRegistry reg, Namespaces namespaces, String targetNamespace) {
        this.def = def;
        createDocumentFragment();
        this.reg = reg;
        this.namespaces = namespaces;
        this.targetNamespace = targetNamespace;
        schemaElementNames = new HashMap();
        schemaTypes = new HashMap();
    }

    /**
     * Serialize the Class as XML schema to the document.
     * Create a types node for the WSDL if one doesn't exist
     * Create a schema node for the Class namespace, if one doesn't exist
     *
     * In case of a primitive type, no need to stream out anything, just return
     * the QName of the primitive type
     *
     * @param param <code>Class</code> to generate the XML Schema info for
     * @return the QName of the generated Schema type, null if void
     */
    public QName writePartType(Class type) throws Exception {
        if (type.getName().equals("void"))
          return null;
        if (isPrimitiveWsdlType(type)) {
            javax.xml.rpc.namespace.QName qName = reg.getTypeQName(type);
            return getWsdlQName(qName);
        }else {
            if (wsdlTypesElem == null)
                writeWsdlTypesElement();
            return writeTypeAsElement(type);
        }
    }

    /**
     * Create a schema element for the given type
     * @param type the class type
     * @return the QName of the generated Element
     */
    private QName writeTypeAsElement(Class type) throws Exception {
        QName qName = writeTypeNamespace(type);
        String elementType = writeType(type);
        writeSchemaElement(qName, createElement(qName, elementType, isNullable(type)));
        return qName;
    }

    /**
     * write out the namespace declaration and return the type QName for the
     * given <code>Class</code>
     *
     * @param type input Class
     * @return QName for the schema type representing the class
     */
    private QName writeTypeNamespace(Class type) {
        javax.xml.rpc.namespace.QName qName = null;
        String typeName = null;
        if (type.isArray()) {
            Class componentType = type.getComponentType();
            typeName = "ArrayOf" + componentType.getName().substring(componentType.getName().lastIndexOf('.') + 1);
            if (isPrimitiveWsdlType(componentType))
              qName = new javax.xml.rpc.namespace.QName(targetNamespace, typeName);
            else
              qName = new javax.xml.rpc.namespace.QName(namespaces.getCreate(componentType.getName()), typeName);
        }else {
            typeName = type.getName().substring(type.getName().lastIndexOf('.') + 1);
            qName = new javax.xml.rpc.namespace.QName(namespaces.getCreate(type.getName()), typeName);
        }
        QName typeQName = getWsdlQName(qName);
        String pref = def.getPrefix(qName.getNamespaceURI());
        if (pref == null)
          def.addNamespace(namespaces.getCreatePrefix(qName.getNamespaceURI()), qName.getNamespaceURI());
        return typeQName;
    }

    /**
     * Write out the given Element into the appropriate schema node.
     * If need be create the schema node as well
     *
     * @param qName qName to get the namespace of the schema node
     * @param element the Element to append to the Schema node
     */
    private void writeSchemaElement(QName qName, Element element) {
        Element schemaElem = null;
        NodeList nl = wsdlTypesElem.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++ ) {
            NamedNodeMap attrs = nl.item(i).getAttributes();
            for (int n = 0; n < attrs.getLength(); n++) {
                Attr a = (Attr)attrs.item(n);
                if (a.getName().equals("targetNamespace") &&
                    a.getValue().equals(qName.getNamespaceURI()))
                    schemaElem = (Element)nl.item(i);
            }
        }
        if (schemaElem == null) {
          schemaElem = docHolder.createElement("schema");
          wsdlTypesElem.appendChild(schemaElem);
          schemaElem.setAttribute("xmlns", Constants.URI_CURRENT_SCHEMA_XSD);
          schemaElem.setAttribute("targetNamespace", qName.getNamespaceURI());
        }
        schemaElem.appendChild(element);
    }

    /**
     * Get the Types element for the WSDL document. If not present, create one
     * @throws Exception
     */
    private void writeWsdlTypesElement() throws Exception {
        NodeList nl = docFragment.getChildNodes();
        for (int n = 0; n < nl.getLength(); n++) {
            if (nl.item(n).getLocalName().equals("types")) {
                wsdlTypesElem = (Element)nl.item(n);
                return;
            }
        }
        wsdlTypesElem = docHolder.createElement("types");
        docFragment.appendChild(wsdlTypesElem);
    }

    /**
     * Write a schema representation for the given <code>Class</code>. Recurse through all the
     * Public fields as well as fields represented by java bean compliant accesor
     * methods.
     * Then return the qualified string representation of the generated type
     *
     * @param type Class for which to generate schema
     * @return a prefixed string for the schema type
     * @throws Exception
     */
    private String writeType(Class type) throws Exception {
        if (isPrimitiveWsdlType(type))
            return "xsd:" + reg.getTypeQName(type).getLocalPart();
        QName qName = writeTypeNamespace(type);
        String typeName = null;
        String componentTypeName = null;
        Class componentType = null;
        if (type.isArray()) {
            componentType = type.getComponentType();
            componentTypeName = writeType(componentType);
            typeName = "ArrayOf" + componentTypeName.substring(componentTypeName.indexOf(":")+1);
        }
        else {
            typeName = type.getName().replace('$', '_');
        }
        String soapTypeName;
        if (typeName.indexOf('.') >= 0)
            soapTypeName = typeName.substring(typeName.lastIndexOf('.')+1);
        else
            soapTypeName = typeName;

        String prefixedName = namespaces.getCreatePrefix(qName.getNamespaceURI())+":"+soapTypeName;

        if (!addToTypesList(qName, soapTypeName))
          return prefixedName;

        Element complexType = docHolder.createElement("complexType");
        writeSchemaElement(qName, complexType);
        complexType.setAttribute("name", soapTypeName);
        Element sequence = docHolder.createElement("sequence");
        complexType.appendChild(sequence);

        if (type.isArray()) {
            Element elem = docHolder.createElement("element");
            elem.setAttribute("name", "item");
            String elType = componentTypeName;
            if (isNullable(componentType)) {
                elem.setAttribute("nullable", "true");
            }
            elem.setAttribute("type", elType);
            complexType.setAttribute("base", "soap:Array");
            sequence.appendChild(elem);
        } else {
            Field[] fld= type.getDeclaredFields();
            for (int i=0;i<fld.length;i++) {
              if ((fld[i].getModifiers() == Modifier.PUBLIC) ||
                (isJavaBean(type, fld[i]))) {
                writeField (fld[i].getName(),fld[i].getType(),sequence);
              }
            }
        }
        return prefixedName;
    }

    /**
     * write a schema representation of the given Class field and append it to the where Node
     * recurse on complex types
     * @param fieldName name of the field
     * @param fieldType type of the field
     * @param where location for the generated schema node
     * @throws Exception
     */
    private void writeField(String fieldName, Class fieldType, Element where) throws Exception {
        String elementType = writeType(fieldType);
        Element elem = createElement(fieldName, elementType, isNullable(fieldType));
        where.appendChild(elem);
    }

    /**
     * Create Element with a unique name generated from the namespace information
     * @param qName the namespace of the created element
     * @param elementType schema type representation of the element
     * @param nullable nullable attribute of the element
     * @return the created Element
     */
    private Element createElement(QName qName, String elementType, boolean nullable) {
        Element element = docHolder.createElement("element");
        String name = generateUniqueElementName(qName);
        element.setAttribute("name", name);
        if (nullable)
            element.setAttribute("nullable", "true");
        element.setAttribute("type", elementType);
        return element;
    }

    /**
     * Create Element with a given name and type
     * @param elementName the name of the created element
     * @param elementType schema type representation of the element
     * @param nullable nullable attribute of the element
     * @return the created Element
     */
    private Element createElement(String elementName, String elementType, boolean nullable) {
        Element element = docHolder.createElement("element");
        element.setAttribute("name", elementName);
        if (nullable)
            element.setAttribute("nullable", "true");
        element.setAttribute("type", elementType);
        return element;
    }

    /**
     * convert from JAX-RPC QName to WSDL QName
     * @param qName JAX-RPC QName
     * @return WSDL QName
     */
    private QName getWsdlQName (javax.xml.rpc.namespace.QName qName) {
        return new QName(qName.getNamespaceURI(), qName.getLocalPart());
    }

    /**
     * Is the given class one of the WSDL primitive types
     * @param type input Class
     * @return true if the type is primitive
     */
    boolean isPrimitiveWsdlType(Class type) {
      return (type == java.lang.Boolean.class ||
              type == java.lang.Boolean.TYPE  ||
              type == java.lang.Byte.class ||
              type == java.lang.Byte.TYPE ||
              type == java.lang.Character.class ||
              type == java.lang.Character.TYPE ||
              type == java.lang.Double.class ||
              type == java.lang.Double.TYPE ||
              type == java.lang.Float.class ||
              type == java.lang.Float.TYPE ||
              type == java.lang.Integer.class ||
              type == java.lang.Integer.TYPE ||
              type == java.lang.Long.class ||
              type == java.lang.Long.TYPE ||
              type == java.lang.Short.class ||
              type == java.lang.Short.TYPE ||
              type == java.lang.String.class);
    }

    /**
     * Generates a unique element name for a given namespace of the form
     * el0, el1 ....
     *
     * @param qName the namespace for the generated element
     * @return elementname
     */
    private String generateUniqueElementName(QName qName) {
      Integer count = (Integer)schemaElementNames.get(qName);
      if (count == null)
        count = new Integer(0);
      else
        count = new Integer(count.intValue() + 1);
      schemaElementNames.put(qName, count);
      return "el" + count.intValue();
    }

    /**
     * Add the type to an ArrayList and return true to indicate that the Schema node
     * needs to be generated
     * If the type already exists, just return false to indicate that the type is already
     * generated in a previous iterration
     *
     * @param qName the name space of the type
     * @param typeName the name of the type
     * @return if the type is added returns true, else if the type is already present returns false
     */
    private boolean addToTypesList (QName qName, String typeName) {
        ArrayList types = (ArrayList)schemaTypes.get(qName.getNamespaceURI());
        if (types == null) {
            types = new ArrayList();
            types.add(typeName);
            schemaTypes.put(qName.getNamespaceURI(), types);
            return true;
        }
        else {
            if (!types.contains(typeName)) {
               types.add(typeName);
               return true;
            }
        }
        return false;
    }

    /**
     * Determines if the Field in the Class has bean compliant accessors. If so returns true,
     * else returns false
     * @param type the Class
     * @param field the Field
     * @return true if the Field has JavaBean style accessor
     */
    private boolean isJavaBean(Class type, Field field) {
        try {
            String setter = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            String getter = null;
            if (field.getType().getName() == "boolean")
              getter = "is" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            else
              getter = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            type.getMethod(setter, new Class[] {field.getType()});
            type.getMethod(getter, null);
        }
        catch (NoSuchMethodException ex) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the field is nullable. All non-primitives are Nullable
     *
     * @param type input Class
     * @return true if nullable
     */
    private boolean isNullable(Class type) {
        return !type.isPrimitive();
    }


    /** @todo ravi: Get rid of Doccument fragment and import node stuuf,
     *  once I have a handle on the wsdl4j mechanism to get at types.
     *
     *  Switch over notes: remove docHolder, docFragment in favor of wsdl4j Types
     */
    DocumentFragment docFragment;
    Document docHolder;

    private void createDocumentFragment () {
        this.docHolder = XMLUtils.newDocument();
        docFragment = docHolder.createDocumentFragment();
    }
    /**
     * Inserts the type fragment into the given wsdl document
     * @param doc
     */
    public void insertTypesFragment(Document doc) {
        doc.getDocumentElement().insertBefore(
                          doc.importNode(docFragment, true),
                          doc.getDocumentElement().getFirstChild());
    }
}
