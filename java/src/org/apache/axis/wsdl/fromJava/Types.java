
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

package org.apache.axis.wsdl.fromJava;

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
import java.util.Vector;

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
    HashMap schemaUniqueElementNames = null; 
    BuilderBeanClassRep beanBuilder = null;

    /**
     * This class serailizes a <code>Class</code> to XML Schema. The constructor
     * provides the context for the streamed node within the WSDL document
     * @param def WSDL Definition Element to declare namespaces
     * @param doc Document element of the WSDL used to create child elements
     * @param reg TypeMappingRegistry to handle known types
     * @param namespaces user defined or autogenerated namespace and prefix maps
     * @param targetNamespace targetNamespace of the document
     * @param factory Java2WSDLFactory                          
     */
    public Types(Definition def, 
                 TypeMappingRegistry reg, 
                 Namespaces namespaces, 
                 String targetNamespace,
                 Java2WSDLFactory factory) {
        this.def = def;
        createDocumentFragment();
        this.reg = reg;
        this.namespaces = namespaces;
        this.targetNamespace = targetNamespace;
        schemaElementNames = new HashMap();
        schemaUniqueElementNames = new HashMap();
        schemaTypes = new HashMap();
        beanBuilder = factory.getBuilderBeanClassRep();
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
        if (isSimpleType(type)) {
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
        Element element = createRootElement(qName, elementType, isNullable(type));
        if (element != null)
            writeSchemaElement(qName,element);
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
        javax.xml.rpc.namespace.QName qName = getTypeQName(type);
        String pref = def.getPrefix(qName.getNamespaceURI());
        if (pref == null)
          def.addNamespace(namespaces.getCreatePrefix(qName.getNamespaceURI()), qName.getNamespaceURI());
        return getWsdlQName(qName);
    }

    /**
     * Return the QName of the type
     * @param type input class
     * @return QName
     */
    private javax.xml.rpc.namespace.QName getTypeQName(Class type) {
        javax.xml.rpc.namespace.QName qName = null;
        String typeName = null;
        if (type.isArray()) {
            Class componentType = type.getComponentType();

            // Check for Byte[], byte[] and Object[]
            if (componentType == java.lang.Byte.class) {
                qName = new javax.xml.rpc.namespace.QName(Constants.URI_SOAP_ENC, "base64");
            } else if (componentType == java.lang.Byte.TYPE) {
                qName = new javax.xml.rpc.namespace.QName(Constants.URI_CURRENT_SCHEMA_XSD, "base64Binary");
            } else if (componentType == java.lang.Object.class) {
                qName = new javax.xml.rpc.namespace.QName(Constants.URI_SOAP_ENC, "Array");
            } else {
                // Construct ArrayOf in targetNamespace
                javax.xml.rpc.namespace.QName cqName = getTypeQName(componentType);
                String pre = namespaces.getCreatePrefix(cqName.getNamespaceURI());
                String localPart = "ArrayOf_" + pre + "_" + cqName.getLocalPart();
                qName = new javax.xml.rpc.namespace.QName(targetNamespace, 
                                                          localPart);
            } 
        } else {
            // Get the QName from the registry, or create our own.
            qName = reg.getTypeQName(type);
            if (qName == null) {
                String pkg = getPackageNameFromFullName(type.getName());
                String lcl = getLocalNameFromFullName(type.getName());

                String ns = namespaces.getCreate(pkg);
                String pre = namespaces.getCreatePrefix(ns);
                String localPart = lcl.replace('$', '_');
                qName = new javax.xml.rpc.namespace.QName(ns, localPart);
            }
        }

        return qName;
    }

    /**
     * Utility method to get the package name from a fully qualified java class name
     * @param full input class name
     * @return package name
     */
    public static String getPackageNameFromFullName(String full) {
        if (full.lastIndexOf('.') < 0)
            return "";
        else 
            return full.substring(0, full.lastIndexOf('.')); 
    }

    /**
     * Utility method to get the local class name from a fully qualified java class name
     * @param full input class name
     * @return package name
     */
    public static String getLocalNameFromFullName(String full) {
        if (full.lastIndexOf('.') < 0)
            return full;
        else 
            return full.substring(full.lastIndexOf('.')+1); 
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

        // Quick return if schema type
        if (isSimpleSchemaType(type))
            return "xsd:" + reg.getTypeQName(type).getLocalPart();
        if (isSimpleSoapEncodingType(type))
            return "soapenc:" + reg.getTypeQName(type).getLocalPart();

        // Write the namespace
        QName qName = writeTypeNamespace(type);

        // If an array the component type should be processed first
        String componentTypeName = null;
        Class componentType = null;
        if (type.isArray()) {
            componentType = type.getComponentType();
            componentTypeName = writeType(componentType);
        }

        String soapTypeName = qName.getLocalPart();
        String prefix = namespaces.getCreatePrefix(qName.getNamespaceURI());
        String prefixedName = prefix+":"+soapTypeName;

        // If processed before, or this is a known namespace, return
        if (!addToTypesList(qName, soapTypeName))
          return prefixedName;

        if (type.isArray()) {
            // ComplexType representation of array
            Element complexType = docHolder.createElement("complexType");
            writeSchemaElement(qName, complexType);
            complexType.setAttribute("name", soapTypeName);

            Element complexContent = docHolder.createElement("complexContent");
            complexType.appendChild(complexContent);

            Element restriction = docHolder.createElement("restriction");
            complexContent.appendChild(restriction);
            restriction.setAttribute("base", "soap:Array");

            Element attribute = docHolder.createElement("attribute");
            restriction.appendChild(attribute);
            attribute.setAttribute("ref", "soapenc:arrayType");
            attribute.setAttribute("wsdl:arrayType", componentTypeName + "[]" );
        } else {
            if (isEnumClass(type)) {
                writeEnumType(qName, type);
            } else {
                writeBeanClassType(qName, type);
            }
        }
        return prefixedName;
    }
    
    /**
     * Returns true if indicated type matches the JAX-RPC enumeration class.
     * Note: supports JSR 101 version 0.6 Public Draft
     */
    public static boolean isEnumClass(Class cls) {
        try {
            java.lang.reflect.Method m  = cls.getMethod("getValue", null);
            java.lang.reflect.Method m2 = cls.getMethod("toString", null);
            java.lang.reflect.Method m3 = cls.getMethod("fromString",
                                                        new Class[] {java.lang.String.class});

            if (m != null && m2 != null && m3 != null &&
                cls.getMethod("fromValue", new Class[] {m.getReturnType()}) != null) {
                try {
                    if (cls.getMethod("setValue",  new Class[] {m.getReturnType()}) == null)
                        return true;
                    return false;
                } catch (java.lang.NoSuchMethodException e) {
                    return true;  // getValue & fromValue exist.  setValue does not exist.  Thus return true. 
                }
            }
        } catch (java.lang.NoSuchMethodException e) {}
        return false;
    }

    /**
     * Write Enumeration Complex Type
     * (Only supports enumeration classes of string types)
     * @param qname QName of type.
     * @param type class of type
     */
    private void writeEnumType(QName qName, Class cls)  throws Exception  {
        if (!isEnumClass(cls))
            return;
        // Get the base type of the enum class
        java.lang.reflect.Method m  = cls.getMethod("getValue", null);
        Class base = m.getReturnType();

        // Create simpleType, restriction elements
        Element simpleType = docHolder.createElement("simpleType");
        writeSchemaElement(qName, simpleType);
        simpleType.setAttribute("name", qName.getLocalPart());
        Element restriction = docHolder.createElement("restriction");
        simpleType.appendChild(restriction);
        String baseType = writeType(base);
        restriction.setAttribute("base", baseType);

        // Create an enumeration using the field values
        Field[] fields= cls.getDeclaredFields();
        for (int i=0; i < fields.length; i++) {
            Field field = fields[i];
            int mod = field.getModifiers();

            // Inspect each public static final field of the same type as the base
            if (Modifier.isPublic(mod) && 
                Modifier.isStatic(mod) &&
                Modifier.isFinal(mod) &&
                field.getType() == base) {
                // Create an enumeration using the value specified
                Element enumeration = docHolder.createElement("enumeration");
                enumeration.setAttribute("value", field.get(null).toString());
                restriction.appendChild(enumeration);
                
            }
        }

    }

    /**
     * Write Bean Class Complex Type
     * @param qname QName of type.
     * @param type class of type
     */
    private void writeBeanClassType(QName qName, Class cls)  throws Exception  {
        // ComplexType representation of bean class
        Element complexType = docHolder.createElement("complexType");
        writeSchemaElement(qName, complexType);
        complexType.setAttribute("name", qName.getLocalPart());

        // See if there is a super class
        Element e = null;
        Class superClass = cls.getSuperclass();
        if (superClass != null &&
            superClass != java.lang.Object.class) {
            // Write out the super class
            String base = writeType(superClass);
            Element complexContent = docHolder.createElement("complexContent");
            complexType.appendChild(complexContent);
            Element extension = docHolder.createElement("extension");
            complexContent.appendChild(extension);
            extension.setAttribute("base", base);
            e = extension;
        } else {
            e = complexType;
        }

        // Add fields under all element
        Element all = docHolder.createElement("all");
        e.appendChild(all);
        
        // Build a ClassRep that represents the bean class.  This
        // allows users to provide their own field mapping.
        ClassRep clsRep = beanBuilder.build(cls);
        
        // Write out fields
        Vector fields = clsRep.getFields();
        for (int i=0; i < fields.size(); i++) {
            FieldRep field = (FieldRep) fields.elementAt(i);

            writeField(field.getName(), field.getType(), field.getIndexed(), all);
        }
    }

    /**
     * write a schema representation of the given Class field and append it to the where Node     * recurse on complex types
     * @param fieldName name of the field
     * @param fieldType type of the field
     * @param isUnbounded causes maxOccurs="unbounded" if set
     * @param where location for the generated schema node
     * @throws Exception
     */
    private void writeField(String fieldName, Class fieldType, boolean isUnbounded, Element where) throws Exception {
        String elementType = writeType(fieldType);
        Element elem = createElement(fieldName, elementType, isNullable(fieldType));
        if (isUnbounded) {
            elem.setAttribute("maxOccurs", "unbounded");
        }
        where.appendChild(elem);
    }

    /**
     * Create Element
     * @param qName the namespace of the created element
     * @param elementType schema type representation of the element
     * @param nullable nillable attribute of the element
     * @return the created Element
     */
    private Element createRootElement(QName qName, String elementType, boolean nullable) {
        if (!addToElementsList(qName))
            return null;

        Element element = docHolder.createElement("element");

        //Generate an element name that matches the type.
        // Previously a unique element name was generated.
        //String name = generateUniqueElementName(qName);
        String name = elementType.substring(elementType.lastIndexOf(":")+1);

        element.setAttribute("name", name);
        if (nullable)
            element.setAttribute("nillable", "true");
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
            element.setAttribute("nillable", "true");
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
     * Is the given class one of the simple types defined by Schema
     * (per JSR 101 v.0.6)
     * @param type input Class
     * @return true if the type is a simple schema type
     */
    boolean isSimpleSchemaType(Class type) {
      return (type == java.lang.String.class ||
              type == java.lang.Boolean.TYPE  ||
              type == java.lang.Byte.TYPE ||
              type == java.lang.Double.TYPE ||
              type == java.lang.Float.TYPE ||
              type == java.lang.Integer.TYPE ||
              type == java.lang.Long.TYPE ||
              type == java.lang.Short.TYPE ||
              type == java.math.BigInteger.class ||
              type == java.math.BigDecimal.class ||
              type == javax.xml.rpc.namespace.QName.class ||
              type == java.util.Calendar.class ||
              type == byte[].class);
    }

    /**
     * Is the given class one of the simple types defined by SoapEncoding.
     * (per JSR 101 v.0.6)
     * @param type input Class
     * @return true if the type is a simple soap encoding type
     */
    boolean isSimpleSoapEncodingType(Class type) {
      return (type == java.lang.String.class ||
              type == java.lang.Boolean.class  ||
              type == java.lang.Byte.class ||
              type == java.lang.Double.class ||
              type == java.lang.Float.class ||
              type == java.lang.Integer.class ||
              type == java.lang.Long.class ||
              type == java.lang.Short.class ||
              type == java.math.BigDecimal.class ||
              type == java.lang.Byte[].class);
    }

    /**
     * Is the given class one of the simple types
     * @param type input Class
     * @return true if the type is a simple type
     */
    boolean isSimpleType(Class type) {
        return (isSimpleSchemaType(type) ||
                isSimpleSoapEncodingType(type));
    }

    /**
     * Generates a unique element name for a given namespace of the form
     * el0, el1 ....
     *
     * @param qName the namespace for the generated element
     * @return elementname
     */
    private String generateUniqueElementName(QName qName) {
      Integer count = (Integer)schemaUniqueElementNames.get(qName.getNamespaceURI());
      if (count == null)
        count = new Integer(0);
      else
        count = new Integer(count.intValue() + 1);
      schemaUniqueElementNames.put(qName.getNamespaceURI(), count);
      return "el" + count.intValue();
    }

    /**
     * Add the type to an ArrayList and return true if the Schema node
     * needs to be generated
     * If the type already exists, just return false to indicate that the type is already
     * generated in a previous iteration
     *
     * @param qName the name space of the type
     * @param typeName the name of the type
     * @return if the type is added returns true, else if the type is already present returns false
     */
    private boolean addToTypesList (QName qName, String typeName) {
        boolean added = false;
        ArrayList types = (ArrayList)schemaTypes.get(qName.getNamespaceURI());
        if (types == null) {
            types = new ArrayList();
            types.add(typeName);
            schemaTypes.put(qName.getNamespaceURI(), types);
            added = true;
        }
        else {
            if (!types.contains(typeName)) {
               types.add(typeName);
               added = true;
            }
        }

        // If addded, look at the namespace uri to see if the schema element should be 
        // generated.
        if (added) {
            String prefix = namespaces.getCreatePrefix(qName.getNamespaceURI());
            if (prefix.equals("soap") ||
                prefix.equals("soapenc") ||
                prefix.equals("wsdl") ||
                prefix.equals("xsd")) 
                return false;
            else
                return true;
        }
        return false;
    }

    /**
     * Add the element to an ArrayList and return true if the Schema element
     * needs to be generated
     * If the element already exists, just return false to indicate that the type is already
     * generated in a previous iteration
     *
     * @param qName the name space of the element
     * @param typeName the name of the type
     * @return if the type is added returns true, else if the type is already present returns false
     */
    private boolean addToElementsList (QName qName) {
        boolean added = false;
        ArrayList elements = (ArrayList)schemaElementNames.get(qName.getNamespaceURI());
        if (elements == null) {
            elements = new ArrayList();
            elements.add(qName.getLocalPart());
            schemaElementNames.put(qName.getNamespaceURI(), elements);
            added = true;
        }
        else {
            if (!elements.contains(qName.getLocalPart())) {
               elements.add(qName.getLocalPart());
               added = true;
            }
        }
        return added;
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
