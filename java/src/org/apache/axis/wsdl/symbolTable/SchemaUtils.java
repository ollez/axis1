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
package org.apache.axis.wsdl.symbolTable;

import org.apache.axis.Constants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.IntHolder;
import java.util.Vector;

/**
 * This class contains static utility methods specifically for schema type queries.
 *
 * @author Rich Scheuerle  (scheu@us.ibm.com)
 */
public class SchemaUtils {

    /**
     * If the specified node represents a supported JAX-RPC complexType or 
     * simpleType, a Vector is returned which contains ElementDecls for the 
     * child element names. 
     * If the element is a simpleType, an ElementDecls is built representing
     * the restricted type with the special name "value".
     * If the element is a complexType which has simpleContent, an ElementDecl
     * is built representing the extended type with the special name "value".
     * This method does not return attribute names and types
     * (use the getContainedAttributeTypes)
     * If the specified node is not a supported 
     * JAX-RPC complexType/simpleType/element null is returned.
     */
    public static Vector getContainedElementDeclarations(Node node, SymbolTable symbolTable) {
        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;
            for (int j = 0; j < children.getLength() && complexNode == null; j++) {
                QName complexKind = Utils.getNodeQName(children.item(j));
                if (complexKind != null &&
                    complexKind.getLocalPart().equals("complexType") &&
                    Constants.isSchemaXSD(complexKind.getNamespaceURI())) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }

        // Expecting a schema complexType or simpleType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node complexContent = null;
            Node simpleContent = null;
            Node extension = null;
            for (int j = 0; j < children.getLength() && complexContent == null; j++) {
                QName complexContentKind = Utils.getNodeQName(children.item(j));
                if (complexContentKind != null &&
                    Constants.isSchemaXSD(complexContentKind.getNamespaceURI())) {
                    if (complexContentKind.getLocalPart().equals("complexContent") )
                        complexContent = children.item(j);
                    else if (complexContentKind.getLocalPart().equals("simpleContent"))
                        simpleContent = children.item(j);
                }
            }
            if (complexContent != null) {
                children = complexContent.getChildNodes();
                for (int j = 0; j < children.getLength() && extension == null; j++) {
                    QName extensionKind = Utils.getNodeQName(children.item(j));
                    if (extensionKind != null &&
                        extensionKind.getLocalPart().equals("extension") &&
                        Constants.isSchemaXSD(extensionKind.getNamespaceURI()))
                        extension = children.item(j);
                }
            }
            if (simpleContent != null) {
                children = simpleContent.getChildNodes();
                for (int j = 0; j < children.getLength() && extension == null; j++) {
                    QName extensionKind = Utils.getNodeQName(children.item(j));
                    if (extensionKind != null &&
                        extensionKind.getLocalPart().equals("extension") &&
                        Constants.isSchemaXSD(extensionKind.getNamespaceURI())) {
                        
                        // get the type of the extension from the "base" attribute
                        QName extendsType =
                            Utils.getTypeQName(children.item(j), new BooleanHolder(), false);
                        
                        // Return an element declaration with a fixed name
                        // ("value") and the correct type.                        
                        Vector v = new Vector();
                        ElementDecl elem = new ElementDecl();
                        elem.setType(symbolTable.getTypeEntry(extendsType, false));
                        elem.setName(new javax.xml.namespace.QName("", "value"));
                        v.add(elem);
                        return v;
                    }
                        
                }
            }

            if (extension != null) {
                node = extension;  // Skip over complexContent and extension
            }

            // Under the complexType there may be choice, sequence, group and/or all nodes.      
            // (There may be other #text nodes, which we will ignore).
            children = node.getChildNodes();
            Vector v = new Vector();
            for (int j = 0; j < children.getLength(); j++) {
                QName subNodeKind = Utils.getNodeQName(children.item(j));
                if (subNodeKind != null &&
                    Constants.isSchemaXSD(subNodeKind.getNamespaceURI())) {
                    if (subNodeKind.getLocalPart().equals("sequence")) {
                        v.addAll(processSequenceNode(children.item(j), symbolTable));
                    } else if (subNodeKind.getLocalPart().equals("all")) {
                        v.addAll(processAllNode(children.item(j), symbolTable));
                    } else if (subNodeKind.getLocalPart().equals("choice")) {
                        v.addAll(processChoiceNode(children.item(j), symbolTable));
                    } else if (subNodeKind.getLocalPart().equals("group")) {
                        v.addAll(processGroupNode(children.item(j), symbolTable));
                    }
                }
            }
            return v;
        } else {
            // This may be a simpleType, return the type with the name "value"
            QName simpleQName = getSimpleTypeBase(node);
            if (simpleQName != null) {
                TypeEntry simpleType = symbolTable.getType(simpleQName);
                if (simpleType != null) {
                    Vector v = new Vector();
                    ElementDecl elem = new ElementDecl();
                    elem.setType(simpleType);
                    elem.setName(new javax.xml.namespace.QName("", "value"));
                    v.add(elem);
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a Choice Node
     */
    private static Vector processChoiceNode(Node choiceNode, 
                                            SymbolTable symbolTable) {
        Vector v = new Vector();
        NodeList children = choiceNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            QName subNodeKind = Utils.getNodeQName(children.item(j));
            if (subNodeKind != null &&
                Constants.isSchemaXSD(subNodeKind.getNamespaceURI())) {
                if (subNodeKind.getLocalPart().equals("choice")) {
                    v.addAll(processChoiceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("sequence")) {
                    v.addAll(processSequenceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("group")) {
                    v.addAll(processGroupNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("element")) {
                    ElementDecl elem = 
                            processChildElementNode(children.item(j), 
                                                    symbolTable);
                    if (elem != null)
                        v.add(elem);
                }
            }
        }
        return v;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a Sequence Node
     */
    private static Vector processSequenceNode(Node sequenceNode, 
                                              SymbolTable symbolTable) {
        Vector v = new Vector();
        NodeList children = sequenceNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            QName subNodeKind = Utils.getNodeQName(children.item(j));
            if (subNodeKind != null &&
                Constants.isSchemaXSD(subNodeKind.getNamespaceURI())) {
                if (subNodeKind.getLocalPart().equals("choice")) {
                    v.addAll(processChoiceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("sequence")) {
                    v.addAll(processSequenceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("group")) {
                    v.addAll(processGroupNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("any")) {
                    // Represent this as an element named any of type any type.
                    // This will cause it to be serialized with the element 
                    // serializer.
                    TypeEntry type = symbolTable.getType(Constants.XSD_ANY);
                    ElementDecl elem = 
                        new ElementDecl(type, new QName("","any"));
                    elem.setAnyElement(true);
                    v.add(elem);
                } else if (subNodeKind.getLocalPart().equals("element")) {
                    ElementDecl elem = 
                            processChildElementNode(children.item(j), 
                                                    symbolTable);
                    if (elem != null)
                        v.add(elem);
                }
            }
        }
        return v;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a group node.
     * (Currently the code only supports a defined group it does not
     * support a group that references a previously defined group)
     */
    private static Vector processGroupNode(Node groupNode, SymbolTable symbolTable) {
        Vector v = new Vector();
        NodeList children = groupNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            QName subNodeKind = Utils.getNodeQName(children.item(j));
            if (subNodeKind != null &&
                Constants.isSchemaXSD(subNodeKind.getNamespaceURI())) {
                if (subNodeKind.getLocalPart().equals("choice")) {
                    v.addAll(processChoiceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("sequence")) {
                    v.addAll(processSequenceNode(children.item(j), symbolTable));
                } else if (subNodeKind.getLocalPart().equals("all")) {
                    v.addAll(processAllNode(children.item(j), symbolTable));
                }
            }
        }
        return v;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath an all node.
     */
    private static Vector processAllNode(Node allNode, SymbolTable symbolTable) {
        Vector v = new Vector();
        NodeList children = allNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            QName subNodeKind = Utils.getNodeQName(children.item(j));
            if (subNodeKind != null &&
                Constants.isSchemaXSD(subNodeKind.getNamespaceURI())) {
                if (subNodeKind.getLocalPart().equals("element")) {
                    ElementDecl elem = 
                            processChildElementNode(children.item(j), 
                                                    symbolTable);
                    if (elem != null)
                        v.add(elem);
                }
            }
        }
        return v;
    }


    /**
     * Invoked by getContainedElementDeclarations to get the child element type
     * and child element name for a child element node.
     *
     * If the specified node represents a supported JAX-RPC child element,
     * we return an ElementDecl containing the child element name and type.
     */
    private static ElementDecl processChildElementNode(Node elementNode, 
                                                  SymbolTable symbolTable) {
        // Get the name qnames.
        QName nodeName = Utils.getNodeNameQName(elementNode);
        BooleanHolder forElement = new BooleanHolder();

        // The type qname is used to locate the TypeEntry, which is then
        // used to retrieve the proper java name of the type.
        QName nodeType = Utils.getTypeQName(elementNode, forElement, false);

        TypeEntry type = symbolTable.getTypeEntry(nodeType, forElement.value);

        // An element inside a complex type is either qualified or unqualified.
        // If the ref= attribute is used, the name of the ref'd element is used
        // (which must be a root element).  If the ref= attribute is not
        // used, the name of the element is unqualified.

        if (!forElement.value) {
            // check the Form (or elementFormDefault) attribute of this node to
            // determine if it should be namespace quailfied or not.
            String form = Utils.getAttribute(elementNode, "form");
            if (form != null && form.equals("unqualified")) {
                // Unqualified nodeName
                nodeName = new QName("", nodeName.getLocalPart());            
            } else if (form == null) {
                // check elementForDefault on schema element
                String def = Utils.getScopedAttribute(elementNode, 
                                                      "elementFormDefault");
                if (def == null || def.equals("unqualified")) {
                    // Unqualified nodeName
                    nodeName = new QName("", nodeName.getLocalPart());            
                }
            }
        }

        if (type != null) {
            ElementDecl elem = new ElementDecl(type, nodeName);
            String minOccurs = Utils.getAttribute(elementNode, "minOccurs");
            if (minOccurs != null && minOccurs.equals("0")) {
                elem.setMinOccursIs0(true);
            }
            return elem;
        }
        
        return null;
    }

    /**
     * Returns the WSDL2Java QName for the anonymous type of the element
     * or null.
     */
    public static QName getElementAnonQName(Node node) {
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                QName kind = Utils.getNodeQName(children.item(j));
                if (kind != null &&
                    (kind.getLocalPart().equals("complexType") ||
                     kind.getLocalPart().equals("simpleType")) &&
                    Constants.isSchemaXSD(kind.getNamespaceURI())) {
                    return Utils.getNodeNameQName(children.item(j));
                }
            }
        }
        return null;
    }

    /**
     * Returns the WSDL2Java QName for the anonymous type of the attribute
     * or null.
     */
    public static QName getAttributeAnonQName(Node node) {
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("attribute") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                QName kind = Utils.getNodeQName(children.item(j));
                if (kind != null &&
                    (kind.getLocalPart().equals("complexType") ||
                     kind.getLocalPart().equals("simpleType")) &&
                    Constants.isSchemaXSD(kind.getNamespaceURI())) {
                    return Utils.getNodeNameQName(children.item(j));
                }
            }
        }
        return null;
    }

    /**
     * If the specified node is a simple type or contains simpleContent, return true
     */
    public static boolean isSimpleTypeOrSimpleContent(Node node) {
        if (node == null) {
            return false;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;
            for (int j = 0; j < children.getLength() && complexNode == null; j++) {
                QName kind = Utils.getNodeQName(children.item(j));
                if (kind != null &&
                    kind.getLocalPart().equals("complexType") &&
                    Constants.isSchemaXSD(kind.getNamespaceURI())) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
                if (kind != null &&
                    kind.getLocalPart().equals("simpleType") &&
                    Constants.isSchemaXSD(kind.getNamespaceURI())) {
                    return true;
                }
            }
        }

        // Expecting a schema complexType or simpleType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("simpleType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            return true;
        }

        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node complexContent = null;
            Node simpleContent = null;
            for (int j = 0; j < children.getLength() && complexContent == null; j++) {
                QName complexContentKind = Utils.getNodeQName(children.item(j));
                if (complexContentKind != null &&
                    Constants.isSchemaXSD(complexContentKind.getNamespaceURI())) {
                    if (complexContentKind.getLocalPart().equals("complexContent") )
                        complexContent = children.item(j);
                    else if (complexContentKind.getLocalPart().equals("simpleContent"))
                        simpleContent = children.item(j);
                }
            }
            if (complexContent != null) {
                return false;
            }
            if (simpleContent != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the specified node represents a supported JAX-RPC complexType/element
     * which extends another complexType.  The Type of the base is returned.
     */
    public static TypeEntry getComplexElementExtensionBase(Node node, SymbolTable symbolTable) {
        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;
            for (int j = 0; j < children.getLength() && complexNode == null; j++) {
                QName complexKind = Utils.getNodeQName(children.item(j));
                if (complexKind != null &&
                    complexKind.getLocalPart().equals("complexType") &&
                    Constants.isSchemaXSD(complexKind.getNamespaceURI())) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }

        // Expecting a schema complexType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there could be should be a complexContent &
            // extension elements if this is a derived type. 
            NodeList children = node.getChildNodes();
            Node content = null;
            Node extension = null;
            for (int j = 0; j < children.getLength() && content == null; j++) {
                QName contentKind = Utils.getNodeQName(children.item(j));
                if (contentKind != null &&
                    contentKind.getLocalPart().equals("complexContent") &&
                    Constants.isSchemaXSD(contentKind.getNamespaceURI()))
                    content = children.item(j);
                if (contentKind != null &&
                    contentKind.getLocalPart().equals("simpleContent") &&
                    Constants.isSchemaXSD(contentKind.getNamespaceURI()))
                    content = children.item(j);
            }
            if (content != null) {
                children = content.getChildNodes();
                for (int j = 0; j < children.getLength() && extension == null; j++) {
                    QName extensionKind = Utils.getNodeQName(children.item(j));
                    if (extensionKind != null &&
                        extensionKind.getLocalPart().equals("extension") &&
                        Constants.isSchemaXSD(extensionKind.getNamespaceURI()))
                        extension = children.item(j);
                }
            }
            if (extension == null) {
                return null;  // No extension                               
            }

            // Get the QName of the extension base
            QName extendsType = Utils.getTypeQName(extension, new BooleanHolder(), false);
            if (extendsType == null) {
                return null; // No extension base
            }
            // Return associated Type
            return symbolTable.getType(extendsType);
        }
        return null;
    }

    /**
     * If the specified node represents a 'normal' non-enumeration simpleType,
     * the QName of the simpleType base is returned.
     */
    public static QName getSimpleTypeBase(Node node) {
        QName baseQName = null;

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node simpleNode = null;
            for (int j = 0; j < children.getLength() && simpleNode == null; j++) {
                QName simpleKind = Utils.getNodeQName(children.item(j));
                if (simpleKind != null &&
                    simpleKind.getLocalPart().equals("simpleType") &&
                    Constants.isSchemaXSD(simpleKind.getNamespaceURI())) {
                    simpleNode = children.item(j);
                    node = simpleNode;
                }
            }
        }
        // Get the node kind, expecting a schema simpleType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("simpleType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the simpleType there should be a restriction.
            // (There may be other #text nodes, which we will ignore).
            NodeList children = node.getChildNodes();
            Node restrictionNode = null;
            for (int j = 0; j < children.getLength() && restrictionNode == null; j++) {
                QName restrictionKind = Utils.getNodeQName(children.item(j));
                if (restrictionKind != null &&
                    restrictionKind.getLocalPart().equals("restriction") &&
                    Constants.isSchemaXSD(restrictionKind.getNamespaceURI()))
                    restrictionNode = children.item(j);
            }

            // The restriction node indicates the type being restricted
            // (the base attribute contains this type).
            
            if (restrictionNode != null) {
                baseQName = Utils.getTypeQName(restrictionNode, new BooleanHolder(), false);
            }
            
            // Look for enumeration elements underneath the restriction node
            if (baseQName != null && restrictionNode != null) {
                NodeList enums = restrictionNode.getChildNodes();
                for (int i=0; i < enums.getLength(); i++) {
                    QName enumKind = Utils.getNodeQName(enums.item(i));
                    if (enumKind != null &&
                        enumKind.getLocalPart().equals("enumeration") &&
                        Constants.isSchemaXSD(enumKind.getNamespaceURI())) {
                        
                        // Found an enumeration, this isn't a 
                        // 'normal' simple type.
                        return null;
                    }
                }
            }
        }
        return baseQName;
    }

    /**
     * Returns the contained restriction or extension node underneath
     * the specified node.  Returns null if not found
     */
    public static Node getRestrictionOrExtensionNode(Node node) {
        Node re = null;
        if (node == null) {
            return re;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node node2 = null;
            for (int j = 0; j < children.getLength() && node2 == null; j++) {
                QName kind2 = Utils.getNodeQName(children.item(j));
                if (kind2 != null &&
                    (kind2.getLocalPart().equals("simpleType") ||
                     kind2.getLocalPart().equals("complexType") ||
                     kind2.getLocalPart().equals("simpleContent")) &&
                    Constants.isSchemaXSD(kind2.getNamespaceURI())) {
                    node2 = children.item(j);
                    node = node2;
                }
            }
        }
        // Get the node kind, expecting a schema simpleType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            (nodeKind.getLocalPart().equals("simpleType") ||
             nodeKind.getLocalPart().equals("complexType")) &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there could be a complexContent.
            NodeList children = node.getChildNodes();
            Node complexContent = null;
            if (nodeKind.getLocalPart().equals("complexType")) {
                for (int j = 0; j < children.getLength() && complexContent == null; j++) {
                    QName complexContentKind = Utils.getNodeQName(children.item(j));
                    if (complexContentKind != null &&
                        (complexContentKind.getLocalPart().equals("complexContent") ||
                         complexContentKind.getLocalPart().equals("simpleContent"))&&
                        Constants.isSchemaXSD(complexContentKind.getNamespaceURI()))
                        complexContent = children.item(j);
                }
                node = complexContent;
            }
            // Now get the extension or restriction node
            if (node != null) {
                children = node.getChildNodes();
                for (int j = 0; j < children.getLength() && re == null; j++) {
                    QName reKind = Utils.getNodeQName(children.item(j));
                    if (reKind != null &&
                        (reKind.getLocalPart().equals("extension") ||
                         reKind.getLocalPart().equals("restriction")) &&
                        Constants.isSchemaXSD(reKind.getNamespaceURI()))
                        re = children.item(j);
                }
            }
        }
            
        return re;
    }

    /**
     * If the specified node represents an array encoding of one of the following
     * forms, then return the qname repesenting the element type of the array.
     * @param node is the node
     * @param dims is the output value that contains the number of dimensions if return is not null
     * @return QName or null
     */
    public static QName getArrayComponentQName(Node node, IntHolder dims) {
        dims.value = 1;  // assume 1 dimension
        QName qName = getCollectionComponentQName(node);
        if (qName == null) {
            qName = getArrayComponentQName_JAXRPC(node, dims);
        }
        return qName;
    }

    /**
     * If the specified node represents an element that references a collection
     * then return the qname repesenting the component of the collection.
     *
     *  <xsd:element name="alias" type="xsd:string" maxOccurs="unbounded"/>
     *    returns qname for"xsd:string"
     *  <xsd:element ref="alias"  maxOccurs="unbounded"/>
     *    returns qname for "alias"
     * @param node is the Node
     * @return QName of the compoent of the collection
     */
    private static QName getCollectionComponentQName(Node node) {
        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive get its type.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Compare the componentQName with the name of the
            // full name.  If different, return componentQName
            BooleanHolder forElement = new BooleanHolder();
            QName componentQName = Utils.getTypeQName(node, forElement, true);
            if (componentQName != null) {
                QName fullQName = Utils.getTypeQName(node, forElement, false);
                if (!componentQName.equals(fullQName)) {
                    return componentQName;
                }
            }
        }
        return null;
    }

    /**
     * If the specified node represents an array encoding of one of the following
     * forms, then return the qname repesenting the element type of the array.
     *
     * @param node is the node
     * @param dims is the output value that contains the number of dimensions if return is not null
     * @return QName or null
     *
     * JAX-RPC Style 2:
     *<xsd:complexType name="hobbyArray">
     *  <xsd:complexContent>
     *    <xsd:restriction base="soapenc:Array">
     *      <xsd:attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
     *    </xsd:restriction>
     *  </xsd:complexContent>
     *</xsd:complexType>
     *
     * JAX-RPC Style 3:
     *<xsd:complexType name="petArray">
     *  <xsd:complexContent>
     *    <xsd:restriction base="soapenc:Array">
     *      <xsd:sequence>
     *        <xsd:element name="alias" type="xsd:string" maxOccurs="unbounded"/>
     *      </xsd:sequence>
     *    </xsd:restriction>
     *  </xsd:complexContent>
     *</xsd:complexType>
     *
     */
    private static QName getArrayComponentQName_JAXRPC(Node node, IntHolder dims) {
        dims.value = 0;  // Assume 0
        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;
            for (int j = 0; j < children.getLength() && complexNode == null; j++) {
                QName complexKind = Utils.getNodeQName(children.item(j));
                if (complexKind != null &&
                    complexKind.getLocalPart().equals("complexType") &&
                    Constants.isSchemaXSD(complexKind.getNamespaceURI())) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }
        // Get the node kind, expecting a schema complexType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there should be a complexContent.
            // (There may be other #text nodes, which we will ignore).
            NodeList children = node.getChildNodes();
            Node complexContentNode = null;
            for (int j = 0; j < children.getLength() && complexContentNode == null; j++) {
                QName complexContentKind = Utils.getNodeQName(children.item(j));
                if (complexContentKind != null &&
                    (complexContentKind.getLocalPart().equals("complexContent") ||
                    complexContentKind.getLocalPart().equals("simpleContent")) &&
                    Constants.isSchemaXSD(complexContentKind.getNamespaceURI()))
                    complexContentNode = children.item(j);
            }

            // Under the complexContent there should be a restriction.
            // (There may be other #text nodes, which we will ignore).
            Node restrictionNode = null;
            if (complexContentNode != null) {
                children = complexContentNode.getChildNodes();
                for (int j = 0; j < children.getLength() && restrictionNode == null; j++) {
                    QName restrictionKind = Utils.getNodeQName(children.item(j));
                    if (restrictionKind != null &&
                        restrictionKind.getLocalPart().equals("restriction") &&
                        Constants.isSchemaXSD(restrictionKind.getNamespaceURI()))
                        restrictionNode = children.item(j);
                }
            }

            // The restriction node must have a base of soapenc:Array.              
            QName baseType = null;
            if (restrictionNode != null) {
                baseType = Utils.getTypeQName(restrictionNode, new BooleanHolder(), false);
                if (baseType != null &&
                    baseType.getLocalPart().equals("Array") &&
                    Constants.isSOAP_ENC(baseType.getNamespaceURI()))
                    ; // Okay
                else
                    baseType = null;  // Did not find base=soapenc:Array
            }

            
            // Under the restriction there should be an attribute OR a sequence/all group node.
            // (There may be other #text nodes, which we will ignore).
            Node groupNode = null;
            Node attributeNode = null;
            if (baseType != null) {
                children = restrictionNode.getChildNodes();
                for (int j = 0;
                     j < children.getLength() && groupNode == null && attributeNode == null;
                     j++) {
                    QName kind = Utils.getNodeQName(children.item(j));
                    if (kind != null &&
                        (kind.getLocalPart().equals("sequence") ||
                         kind.getLocalPart().equals("all")) &&
                        Constants.isSchemaXSD(kind.getNamespaceURI())) {
                        groupNode = children.item(j);
                        if (groupNode.getChildNodes().getLength() == 0) {
                            // This covers the rather odd but legal empty sequence.
                            // <complexType name="ArrayOfString">
                            //   <complexContent>
                            //     <restriction base="soapenc:Array">
                            //       <sequence/>
                            //       <attribute ref="soapenc:arrayType" wsdl:arrayType="string[]"/>
                            //     </restriction>
                            //   </complexContent>
                            // </complexType>
                            groupNode = null;
                        }
                    }
                    if (kind != null &&
                        kind.getLocalPart().equals("attribute") &&
                        Constants.isSchemaXSD(kind.getNamespaceURI())) {
                        // If the attribute node does not have ref="soapenc:arrayType"
                        // then keep looking.
                        BooleanHolder isRef = new BooleanHolder();
                        QName refQName = Utils.getTypeQName(children.item(j), isRef, false);
                        if (refQName != null &&
                            isRef.value &&
                            refQName.getLocalPart().equals("arrayType") &&
                            Constants.isSOAP_ENC(refQName.getNamespaceURI())) {
                            attributeNode = children.item(j);
                        }
                    }
                }
            }

            // If there is an attribute node, look at wsdl:arrayType to get the element type
            if (attributeNode != null) {
                String wsdlArrayTypeValue = null;
                Vector attrs = Utils.getAttributesWithLocalName(attributeNode, "arrayType");
                for (int i=0; i < attrs.size() && wsdlArrayTypeValue == null; i++) {
                    Node attrNode = (Node) attrs.elementAt(i);
                    String attrName = attrNode.getNodeName();
                    QName attrQName = Utils.getQNameFromPrefixedName(attributeNode, attrName);
                    if (Constants.isWSDL(attrQName.getNamespaceURI())) {
                        wsdlArrayTypeValue = attrNode.getNodeValue();
                    }
                }

                // The value could have any number of [] or [,] on the end
                // Strip these off to get the prefixed name.
                // The convert the prefixed name into a qname.
                // Count the number of [ and , to get the dim information.
                if (wsdlArrayTypeValue != null) {
                    int i = wsdlArrayTypeValue.indexOf('[');
                    if (i > 0) {
                        String prefixedName = wsdlArrayTypeValue.substring(0,i);
                        String mangledString = wsdlArrayTypeValue.replace(',', '[');
                        dims.value = 0;
                        int index = mangledString.indexOf('[');
                        while (index > 0) {
                            dims.value++;
                            index = mangledString.indexOf('[',index+1);
                        }
                        
                        return Utils.getQNameFromPrefixedName(restrictionNode, prefixedName);
                    }
                }
            } else if (groupNode != null) {

                // Get the first element node under the group node.       
                NodeList elements = groupNode.getChildNodes();
                Node elementNode = null;
                for (int i=0; i < elements.getLength() && elementNode == null; i++) {
                    QName elementKind = Utils.getNodeQName(elements.item(i));
                    if (elementKind != null &&
                        elementKind.getLocalPart().equals("element") &&
                        Constants.isSchemaXSD(elementKind.getNamespaceURI())) {
                        elementNode = elements.item(i);
                    }
                }
                 
                // The element node should have maxOccurs="unbounded" and
                // a type
                if (elementNode != null) {
                    String maxOccursValue = Utils.getAttribute(elementNode, "maxOccurs");
                    if (maxOccursValue != null &&
                        maxOccursValue.equalsIgnoreCase("unbounded")) {
                        // Get the QName of the type without considering maxOccurs
                        dims.value = 1;
                        return Utils.getTypeQName(elementNode, new BooleanHolder(), true);
                    }
                }
            }
            
        }
        return null;
    }

    /**
     * Return the attribute names and types if any in the node
     * The even indices are the element types (TypeEntry) and
     * the odd indices are the corresponding names (Strings).
     * 
     * Example:
     * <complexType name="Person">
     *   <sequence>
     *     <element minOccurs="1" maxOccurs="1" name="Age" type="double" />
     *     <element minOccurs="1" maxOccurs="1" name="ID" type="xsd:float" />
     *   </sequence>
     *   <attribute name="Name" type="string" />
     *   <attribute name="Male" type="boolean" />
     * </complexType>
     * 
     */ 
    public static Vector getContainedAttributeTypes(Node node, 
                                                    SymbolTable symbolTable) 
    {
        Vector v = null;    // return value
        
        if (node == null) {
            return null;
        }
        // Check for SimpleContent
        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("element") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;
            for (int j = 0; j < children.getLength() && complexNode == null; j++) {
                QName complexKind = Utils.getNodeQName(children.item(j));
                if (complexKind != null &&
                    complexKind.getLocalPart().equals("complexType") &&
                    Constants.isSchemaXSD(complexKind.getNamespaceURI())) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }

        // Expecting a schema complexType
        nodeKind = Utils.getNodeQName(node);
        if (nodeKind != null &&
            nodeKind.getLocalPart().equals("complexType") &&
            Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node content = null;
            Node extension = null;
            for (int j = 0; j < children.getLength() && content == null; j++) {
                QName complexContentKind = Utils.getNodeQName(children.item(j));
                if (complexContentKind != null &&
                    Constants.isSchemaXSD(complexContentKind.getNamespaceURI())) {
                    if (complexContentKind.getLocalPart().equals("complexContent") ||
                        complexContentKind.getLocalPart().equals("simpleContent")) {
                        content = children.item(j);
                    }
                }
            }
            // Check for extensions
            if (content != null) {
                children = content.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    QName extensionKind = Utils.getNodeQName(children.item(j));
                    if (extensionKind != null &&
                            extensionKind.getLocalPart().equals("extension") &&
                            Constants.isSchemaXSD(extensionKind.getNamespaceURI())) {
                        extension = children.item(j);
                        break;
                    }
                }
            }
            
            if (extension != null) {
                node = extension;
            }
            
            // examine children of the node for <attribute> elements
            children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                nodeKind = Utils.getNodeQName(child);
                if (nodeKind == null ||
                        ! nodeKind.getLocalPart().equals("attribute"))
                    continue;
                
                // we have an attribute node
                if (v == null)
                    v = new Vector();

                // Get the name and type qnames.
                // The type qname is used to locate the TypeEntry, which is then
                // used to retrieve the proper java name of the type.
                QName attributeName = Utils.getNodeNameQName(child);
                BooleanHolder forElement = new BooleanHolder();
                QName attributeType = Utils.getTypeQName(child, forElement, false);

                // An attribute is either qualified or unqualified.
                // If the ref= attribute is used, the name of the ref'd element is used
                // (which must be a root element).  If the ref= attribute is not
                // used, the name of the attribute is unqualified.
                if (!forElement.value) {
                    // check the Form (or attributeFormDefault) attribute of 
                    // this node to determine if it should be namespace 
                    // quailfied or not.
                    String form = Utils.getAttribute(child, "form");
                    if (form != null && form.equals("unqualified")) {
                        // Unqualified nodeName
                        attributeName = new QName("", attributeName.getLocalPart());            
                    } else if (form == null) {
                        // check attributeFormDefault on schema element
                        String def = Utils.getScopedAttribute(child, 
                                                              "attributeFormDefault");
                        if (def == null || def.equals("unqualified")) {
                            // Unqualified nodeName
                            attributeName = new QName("", attributeName.getLocalPart());            
                        }
                    }
                } else {
                    attributeName = attributeType;
                }
                
                // Get the corresponding TypeEntry from the symbol table
                TypeEntry type = symbolTable.getTypeEntry(attributeType, 
                                                          forElement.value);
                
                // add type and name to vector, skip it if we couldn't parse it
                // XXX - this may need to be revisited.
                if (type != null && attributeName != null) {
                    v.add(type);
                    v.add(attributeName);
                }
            }
        }            
        return v;
    }

    // list of all of the XSD types in Schema 2001
    private static String schemaTypes[] = {
        "string",
        "normalizedString",
        "token",        
        "byte",
        "unsignedByte",
        "base64Binary",
        "hexBinary",    
        "integer",
        "positiveInteger",
        "negativeInteger",
        "nonNegativeInteger",
        "nonPositiveInteger",
        "int",
        "unsignedInt",  
        "long",
        "unsignedLong",
        "short",
        "unsignedShort",
        "decimal",
        "float",
        "double",
        "boolean",
        "time",
        "dateTime",
        "duration",
        "date",
        "gMonth",
        "gYear",
        "gYearMonth",
        "gDay",
        "gMonthDay",
        "Name",
        "QName",
        "NCName",
        "anyURI",
        "language",
        "ID",
        "IDREF",
        "IDREFS",
        "ENTITY",
        "ENTITIES",
        "NOTATION",
        "NMTOKEN",
        "NMTOKENS"
    };
    
    /**
     * Determine if a string is a simple XML Schema type 
     */ 
    private static boolean isSimpleSchemaType(String s) {
        if (s == null)
            return false;
        
        // This is rather expensive.  Probably should be
        // a hash lookup.  
        for (int i = 0; i < schemaTypes.length; i++) {
            if (schemaTypes[i].equals(s))
                return true;
        }
        return false;
    }
    /**
     * Determine if a QName is a simple XML Schema type 
     */ 
    public static boolean isSimpleSchemaType(QName qname) {
        if (qname == null || 
            !Constants.isSchemaXSD(qname.getNamespaceURI())) {
            return false;
        }
        return isSimpleSchemaType(qname.getLocalPart());
    }
}
