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
package org.apache.axis.wsdl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
/**
 * This factory creates Type objects for the types supported by the WSDL2Java emitter.
 * The factory creates the Types by analyzing the XML Document.  The Type encapsulates
 * the QName, Java Name and defining Node for each Type. 
 *
 * @author Rich Scheuerle  (scheu@us.ibm.com)
 */
public class TypeFactory {

    private HashMap types;                  // All Types        
    private HashMap mapNamespaceToPackage;  // Mapping from Namespace to Java Package 

    /**
     * Create an Emit Type Factory
     */
    public TypeFactory() { 
        types = new HashMap();
        mapNamespaceToPackage = new HashMap();
    }

    /**
     * Invoke this method to associate a namespace URI with a particular Java Package
     */
    public void map (String namespace, String pkg) {
        mapNamespaceToPackage.put(namespace, pkg);
    } 

    /**
     * Invoke this method from the Emitter prior to emitting any code.
     * All of the supported Types are identified and placed in the types HashMap.
     */
    public void buildTypes(Document doc) {
        addTypes(doc, false);
    }

    /**
     * Access all of the emit types.                                      
     */
    public HashMap getTypes() {
        return types;
    }

    /**
     * Get the Type for the given QName                               
     */
    public Type getType(QName qName) {
        return (Type) types.get(qName);
    }

    /**     
     * Dump Types for debugging                                       
     */
    public void dump() {
        Iterator i = types.values().iterator();
        while (i.hasNext()) {
            Type et = (Type) i.next();
            System.out.println("");
            System.out.println("QName: " + et.getQName());
            System.out.println("JName: " + et.getJavaName());
            if (et.getNode() != null) {
                System.out.println("Node: " + et.getNode());
            }
        }
    }

    /**     
     * If the specified node represents a supported JAX-RPC complexType,
     * a Vector is returned which contains the child element types and
     * child element names.  The even indices are the element types (Types) and
     * the odd indices are the corresponding names (Strings).
     * If the specified node is not a supported JAX-RPC complexType,
     * null is returned.
     */
    public Vector getComplexElementTypesAndNames(Node node) {
        if (node == null) {
            return null;
        }

        // Get the node kind, expecting a schema complexType
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Utils.isSchemaNS(nodeKind.getNamespaceURI())) {
      
            // Under the complexType there should be a sequence or all group node.
            // (There may be other #text nodes, which we will ignore).  
            NodeList children = node.getChildNodes();
            Node groupNode = null;
            for (int j = 0; j < children.getLength() && groupNode == null; j++) {
                QName groupKind = Utils.getNodeQName(children.item(j));
                if (groupKind != null &&
                    (groupKind.getLocalPart().equals("sequence") ||
                     groupKind.getLocalPart().equals("all")) &&
                    Utils.isSchemaNS(groupKind.getNamespaceURI())) 
                    groupNode = children.item(j);
            } 
      
            if (groupNode != null) {

                // Process each of the element nodes under the group node
                Vector v = new Vector();
                NodeList elements = children.item(1).getChildNodes();
                for (int i=0; i < elements.getLength(); i++) {
                    QName elementKind = Utils.getNodeQName(elements.item(i));
                    if (elementKind != null &&
                        elementKind.getLocalPart().equals("element") &&
                        Utils.isSchemaNS(elementKind.getNamespaceURI())) {
              
                        // Get the name and type qnames.
                        // The name of the element is the local part of the name's qname.
                        // The type qname is used to locate the Type, which is then
                        // used to retrieve the proper java name of the type.
                        Node elementNode = elements.item(i);
                        QName nodeName = Utils.getNodeNameQName(elementNode);
                        QName nodeType = Utils.getNodeTypeQName(elementNode);
                        if (nodeType == null) { // The element may use an anonymous type
                            nodeType = nodeName;
                        }
        
                        Type Type = (Type) types.get(nodeType);
                        if (Type != null) {
                            v.add(Type);
                            v.add(nodeName.getLocalPart());
                        }
                    }
                }
                return v;
            }
        }
        return null;
    } 

    /**     
     * If the specified node represents a supported JAX-RPC enumeration,
     * a Vector is returned which contains the base type and the enumeration values.
     * The first element in the vector is the base type (an Type). 
     * Subsequent elements are values (Strings).
     * If this is not an enumeration, null is returned.
     */
    public Vector getEnumerationBaseAndValues(Node node) {
        if (node == null) {
            return null;
        }

        // Get the node kind, expecting a schema simpleType
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("simpleType") &&
            Utils.isSchemaNS(nodeKind.getNamespaceURI())) {
      
            // Under the simpleType there should be a restriction.
            // (There may be other #text nodes, which we will ignore).  
            NodeList children = node.getChildNodes();
            Node restrictionNode = null;
            for (int j = 0; j < children.getLength() && restrictionNode == null; j++) {
                QName restrictionKind = Utils.getNodeQName(children.item(j));
                if (restrictionKind != null &&
                    restrictionKind.getLocalPart().equals("restriction") &&
                    Utils.isSchemaNS(restrictionKind.getNamespaceURI())) 
                    restrictionNode = children.item(j);
            } 
      
            // The restriction node indicates the type being restricted 
            // (the base attribute contains this type).
            // The base type must be a built-in type...and we only think
            // this makes sense for string.
            Type baseEType = null;
            if (restrictionNode != null) {
                QName baseType = Utils.getNodeTypeQName(restrictionNode, "base");
                baseEType = getType(baseType);
                if (baseEType != null && baseEType.getBaseType() == null &&
                    baseEType.getJavaLocalName().equals("String")) {
                    baseEType = null;
                }
            }

            // Process the enumeration elements underneath the restriction node
            if (baseEType != null) {

                Vector v = new Vector();
                v.add(baseEType);
                NodeList enums = children.item(1).getChildNodes();
                for (int i=0; i < enums.getLength(); i++) {
                    QName enumKind = Utils.getNodeQName(enums.item(i));
                    if (enumKind != null &&
                        enumKind.getLocalPart().equals("enumeration") &&
                        Utils.isSchemaNS(enumKind.getNamespaceURI())) {
              
                        // Put the enum value in the vector.
                        Node enumNode = enums.item(i);
                        String value = Utils.getAttribute(enumNode, "value");
                        if (value != null) {
                            v.add(value);
                        }
                    }
                }
                return v;
            }
        }
        return null;
    } 

    /**
     * Utility method which walks the Document and creates Types.
     * The inner parameter indicates that we are nested inside a complexType
     * (which is the only context where we need to investigate elements).
     */
    private void addTypes(Node node, boolean inner) {
        if (node == null) {
            return;
        }
        // Get the kind of node (complexType, wsdl:part, etc.)
        QName nodeKind = Utils.getNodeQName(node);

        
        if (nodeKind != null) {
            if (nodeKind.getLocalPart().equals("complexType") &&
                Utils.isSchemaNS(nodeKind.getNamespaceURI())) {
        
                // This is a definition of a complex type.
                // create the type and indicate within complexType
                createTypeFromDef(node);
                inner = true;  
            } 
            if (nodeKind.getLocalPart().equals("simpleType") &&
                Utils.isSchemaNS(nodeKind.getNamespaceURI())) {
        
                // This is a definition of a simple type, which could be an enum
                // Create the type.
                createTypeFromDef(node);
            } 
            else if (nodeKind.getLocalPart().equals("element") &&
                   Utils.isSchemaNS(nodeKind.getNamespaceURI())) {
        
                // This is an element.  Currently we only process the
                // elements that are inside a complex type.  This may need
                // to be changed if we ever support "ref" attributes.
                // If the type is specified, create an Type from the
                // reference.  Otherwise this is an anonymous type which
                // will be picked up as we proceed further down the tree.
                if (inner) {
                    if (Utils.getAttribute(node, "type") != null) {
                        createTypeFromRef(node);
                    }
                }
            }
            else if (nodeKind.getLocalPart().equals("part") &&
                     Utils.isWsdlNS(nodeKind.getNamespaceURI())) {
        
                // This is a wsdl part.  Create an Type from the reference
                createTypeFromRef(node);
            }  
        } 
        // Recurse through children nodes    
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            addTypes(children.item(i), inner);
        }
    }

    /**
     * Create an Type from the indicated node, which defines a supported type.
     */
    private void createTypeFromDef(Node node) {
        // Get the QName of the node's name attribute value
        QName qName = Utils.getNodeNameQName(node);
        if (qName != null) {
            String javaName = getJavaName(qName);
            // Since this is a defining context, force this Type into the 
            // hash map and supply the defining node.
            types.put(qName, new Type(qName, javaName, node));
        }
    }

    /**
     * Create an Type from the indicated node, which references a supported type.
     */
    private void createTypeFromRef(Node node) {
        // Get the QName of the node's type attribute value
        QName qName = Utils.getNodeTypeQName(node); 
        if (qName != null) {
            String javaName = getJavaName(qName);
            // An Type for this type may already have been encountered 
            // via another reference or defining context.  Also note that if 
            // the Type represents a Base type, the Type will change
            // java name to the appropriate java base name.
            if (types.get(qName) == null) {
                types.put(qName, new Type(qName, javaName, null));
            }
        }
    }


    /**     
     * Get the Package name for the specified namespace                   
     */
    private String getPackage(String namespace) {
        return (String) mapNamespaceToPackage.get(namespace);
    }

    /**     
     * Get the Package name for the specified QName                       
     */
    private String getPackage(QName qName) {
        return getPackage(qName.getNamespaceURI());
    }

    /**     
     * Convert the specified QName into a full Java Name.                 
     */
    private String getJavaName(QName qName) {
        String javaName = Utils.getJavaName(qName.getLocalPart());                      
        String pkg = getPackage(qName.getNamespaceURI());
        String fullJavaName = javaName;
        if (pkg != null) {
           fullJavaName = pkg + "." + javaName;
        }
        return fullJavaName;
    }

}








