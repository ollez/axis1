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

package org.apache.axis.encoding;

import org.apache.axis.Constants;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.fromJava.Namespaces;
import org.apache.axis.wsdl.fromJava.Types;
import org.apache.commons.logging.Log;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * This is the implementation of the axis TypeMapping interface (which extends
 * the JAX-RPC TypeMapping interface).
 * </p>
 * <p>
 * A TypeMapping is obtained from the singleton TypeMappingRegistry using
 * the namespace of the webservice.  The TypeMapping contains the tuples
 * {Java type, SerializerFactory, DeserializerFactory, Type QName)
 * </p>
 * <p>
 * So if you have a Web Service with the namespace "XYZ", you call
 * the TypeMappingRegistry.getTypeMapping("XYZ").
 * </p>
 * <p>
 * The wsdl in your web service will use a number of types.  The tuple
 * information for each of these will be accessed via the TypeMapping.
 * </p>
 * <p>
 * Because every web service uses the soap, schema, wsdl primitives, we could
 * pre-populate the TypeMapping with these standard tuples.  Instead,
 * if the namespace/class matches is not found in the TypeMapping
 * the request is delegated to the
 * Default TypeMapping or another TypeMapping
 * </p>
 *
 * @author Rich Scheuerle (scheu@us.ibm.com)
 */
public class TypeMappingImpl implements TypeMapping
{
    protected static Log log =
        LogFactory.getLog(TypeMappingImpl.class.getName());

    public class Pair {
        public Class javaType;
        public QName xmlType;
        public Pair(Class javaType, QName xmlType) {
            this.javaType = javaType;
            this.xmlType = xmlType;
        }
        public boolean equals(Object o) {
            if (o == null) return false;
            Pair p = (Pair) o;
            // Test straight equality
            if (p.xmlType == this.xmlType &&
                p.javaType == this.javaType) {
                return true;
            }
            return (p.xmlType.equals(this.xmlType) &&
                    p.javaType.equals(this.javaType));
        }
        public int hashCode() {
            int hashcode = 0;
            if (javaType != null) {
                hashcode ^= javaType.hashCode();
            }
            if (xmlType != null) {
                hashcode ^= xmlType.hashCode();
            }
            return hashcode;
        }
    }

    private HashMap qName2Pair;     // QName to Pair Mapping
    private HashMap class2Pair;     // Class Name to Pair Mapping
    private HashMap pair2SF;        // Pair to Serialization Factory
    private HashMap pair2DF;        // Pair to Deserialization Factory
    protected TypeMapping delegate;   // Pointer to delegate or null
    private ArrayList namespaces;   // Supported namespaces

    protected boolean doAutoTypes = false;

    /**
     * Construct TypeMapping
     */
    public TypeMappingImpl(TypeMapping delegate) {
        qName2Pair  = new HashMap();
        class2Pair  = new HashMap();
        pair2SF     = new HashMap();
        pair2DF     = new HashMap();
        this.delegate = delegate;
        namespaces  = new ArrayList();
    }

    private static boolean isArray(Class clazz)
    {
        return clazz.isArray() || java.util.Collection.class.isAssignableFrom(clazz);
    }


    /**
     * setDelegate sets the new Delegate TypeMapping
     */
    public void setDelegate(TypeMapping delegate) {
        this.delegate = delegate;
    }

    /**
     * getDelegate gets the new Delegate TypeMapping
     */
    public TypeMapping getDelegate() {
        return delegate;
    }

    /********* JAX-RPC Compliant Method Definitions *****************/

    /**
     * Gets the list of encoding styles supported by this TypeMapping object.
     *
     * @return  String[] of namespace URIs for the supported encoding
     * styles and XML schema namespaces.
     */
    public String[] getSupportedEncodings() {
        String[] stringArray = new String[namespaces.size()];
        return (String[]) namespaces.toArray(stringArray);
    }

    /**
     * Sets the list of encoding styles supported by this TypeMapping object.
     * (Not sure why this is useful...this information is automatically updated
     * during registration.
     *
     * @param namespaceURIs String[] of namespace URI's
     */
    public void setSupportedEncodings(String[] namespaceURIs) {
        namespaces.clear();
        for (int i =0; i< namespaceURIs.length; i++) {
            if (!namespaces.contains(namespaceURIs[i])) {
                namespaces.add(namespaceURIs[i]);
            }
        }
    }

    /**
     * isRegistered returns true if the [javaType, xmlType]
     * pair is registered.
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     * @return true if there is a mapping for the given pair, or
     * false if the pair is not specifically registered.
     *
     * For example if called with (java.lang.String[], soapenc:Array)
     * this routine will return false because this pair is
     * probably not specifically registered.
     * However if getSerializer is called with the same pair,
     * the default TypeMapping will use extra logic to find
     * a serializer (i.e. array serializer)
     */
    public boolean isRegistered(Class javaType, QName xmlType) {
        if (javaType == null || xmlType == null) {
            // REMOVED_FOR_TCK
            // return false;
            throw new JAXRPCException(
                    Messages.getMessage(javaType == null ?
                                         "badJavaType" : "badXmlType"));
        }
        if (pair2SF.keySet().contains(new Pair(javaType, xmlType))) {
            return true;
        }
        if (delegate != null) {
            return delegate.isRegistered(javaType, xmlType);
        }
        return false;
    }

    /**
     * Registers SerializerFactory and DeserializerFactory for a
     * specific type mapping between an XML type and Java type.
     *
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     * @param sf - SerializerFactory
     * @param dsf - DeserializerFactory
     *
     * @throws JAXRPCException - If any error during the registration
     */
    public void register(Class javaType, QName xmlType,
                         javax.xml.rpc.encoding.SerializerFactory sf,
                         javax.xml.rpc.encoding.DeserializerFactory dsf)
        throws JAXRPCException {
        // At least a serializer or deserializer factory must be specified.
        if (sf == null && dsf == null) {
            throw new JAXRPCException(Messages.getMessage("badSerFac"));
        }

        internalRegister(javaType, xmlType, sf, dsf);
    }

    /**
     * Internal version of register(), which allows null factories.
     *
     * @param javaType
     * @param xmlType
     * @param sf
     * @param dsf
     * @throws JAXRPCException
     */
    protected void internalRegister(Class javaType, QName xmlType,
                         javax.xml.rpc.encoding.SerializerFactory sf,
                         javax.xml.rpc.encoding.DeserializerFactory dsf)
            throws JAXRPCException {
        // Both javaType and xmlType must be specified.
        if (javaType == null || xmlType == null) {
            throw new JAXRPCException(
                    Messages.getMessage(javaType == null ?
                                         "badJavaType" : "badXmlType"));
        }

        //REMOVED_FOR_TCK
        //if (sf != null &&
        //    !(sf instanceof javax.xml.rpc.encoding.SerializerFactory)) {
        //    throw new JAXRPCException(message text);
        //}
        //if (dsf != null &&
        //    !(dsf instanceof javax.xml.rpc.encoding.DeserializerFactory)) {
        //    throw new JAXRPCException(message text);
        //}

        Pair pair = new Pair(javaType, xmlType);

        // Only register the appropriate mappings.
        if ((dsf != null) || (qName2Pair.get(xmlType) == null))
            qName2Pair.put(xmlType, pair);
        if ((sf != null) || (class2Pair.get(javaType) == null))
            class2Pair.put(javaType, pair);

        if (sf != null)
            pair2SF.put(pair, sf);
        if (dsf != null)
            pair2DF.put(pair, dsf);
    }

    /**
     * Gets the SerializerFactory registered for the specified pair
     * of Java type and XML data type.
     *
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     *
     * @return Registered SerializerFactory
     *
     * @throws JAXRPCException - If there is no registered SerializerFactory
     * for this pair of Java type and XML data type
     * java.lang.IllegalArgumentException -
     * If invalid or unsupported XML/Java type is specified
     */
    public javax.xml.rpc.encoding.SerializerFactory
        getSerializer(Class javaType, QName xmlType)
        throws JAXRPCException {

        javax.xml.rpc.encoding.SerializerFactory sf = null;

        // If the xmlType was not provided, get one
        if (xmlType == null) {
            xmlType = getTypeQName(javaType);
            // If we couldn't find one, we're hosed, since getTypeQName()
            // already asked all of our delegates.
            if (xmlType == null) {
                return null;
            }
        }

        // Try to get the serializer associated with this pair
        Pair pair = new Pair(javaType, xmlType);

        // Now get the serializer with the pair
        sf = (javax.xml.rpc.encoding.SerializerFactory) pair2SF.get(pair);

        // Need to look into hierarchy of component type.
        // ex) java.util.GregorianCalendar[]
        //     -> java.util.Calendar[]
        if (sf == null && javaType.isArray()) {
            int dimension = 1;
            Class componentType = javaType.getComponentType();
            while (componentType.isArray()) {
                dimension += 1;
                componentType = componentType.getComponentType();
            }
            int[] dimensions = new int[dimension];
            componentType = componentType.getSuperclass();
            Class superJavaType = null;
            while (componentType != null) {
    			superJavaType = Array.newInstance(componentType, dimensions).getClass();
                pair = new Pair(superJavaType, xmlType);
                sf = (javax.xml.rpc.encoding.SerializerFactory) pair2SF.get(pair);
                if (sf != null) {
                    break;
                }
                componentType = componentType.getSuperclass();
            }
        }

        if (sf == null && delegate != null) {
            sf = delegate.getSerializer(javaType, xmlType);
        }

        // If not successful, use the javaType to get another Pair unless
        // we've got an array, in which case make sure we get the
        // ArraySerializer.
        if (sf == null) {
            if (isArray(javaType)) {
                pair = (Pair) qName2Pair.get(Constants.SOAP_ARRAY);
            } else {
                pair = (Pair) class2Pair.get(pair.javaType);
            }
            if (pair != null) {
                sf = (javax.xml.rpc.encoding.SerializerFactory) pair2SF.get(pair);
            }
        }

        return sf;
    }

    /**
     * Get the exact XML type QName which will be used when serializing a
     * given Class to a given type QName.  In other words, if we have:
     *
     * Class        TypeQName
     * ----------------------
     * Base         myNS:Base
     * Child        myNS:Child
     *
     * and call getXMLType(Child.class, BASE_QNAME), we should get
     * CHILD_QNAME.
     *
     * @param javaType
     * @param xmlType
     * @return the type's QName
     * @throws JAXRPCException
     */
    public QName getXMLType(Class javaType, QName xmlType, boolean encoded)
        throws JAXRPCException
    {
        javax.xml.rpc.encoding.SerializerFactory sf = null;

        // If the xmlType was not provided, get one
        if (xmlType == null) {
            xmlType = getTypeQNameRecursive(javaType);

            // If we couldn't find one, we're hosed, since getTypeQName()
            // already asked all of our delegates.
            if (xmlType == null) {
                return null;
            }
        }

        // Try to get the serializer associated with this pair
        Pair pair = new Pair(javaType, xmlType);

        // Now get the serializer with the pair
        sf = (javax.xml.rpc.encoding.SerializerFactory) pair2SF.get(pair);
        if (sf != null)
            return xmlType;

        // If not successful, use the xmlType to get
        // another pair.  For some xmlTypes (like SOAP_ARRAY)
        // all of the possible javaTypes are not registered.
        if (isArray(javaType)) {
            if (encoded) {
                return Constants.SOAP_ARRAY;
            } else {
                pair = (Pair) qName2Pair.get(xmlType);
            }
        }

        if (pair == null) {
            pair = (Pair) class2Pair.get(javaType);
        }

        if (pair != null) {
            xmlType = pair.xmlType;
        } else if (delegate != null) {
            return delegate.getXMLType(javaType, xmlType, encoded);
        }

        return xmlType;
    }

    /**
     * Gets the DeserializerFactory registered for the specified pair
     * of Java type and XML data type.
     *
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     *
     * @return Registered DeserializerFactory
     *
     * @throws JAXRPCException - If there is no registered DeserializerFactory
     * for this pair of Java type and  XML data type
     * java.lang.IllegalArgumentException -
     * If invalid or unsupported XML/Java type is specified
     */
    public javax.xml.rpc.encoding.DeserializerFactory
        getDeserializer(Class javaType, QName xmlType)
        throws JAXRPCException {
        javax.xml.rpc.encoding.DeserializerFactory df = null;

        if (javaType == null) {
            javaType = getClassForQName(xmlType);
            // If we don't have a mapping, we're hosed since getClassForQName()
            // has already asked all our delegates.
            if (javaType == null) {
                return null;
            }
        }

        Pair pair = new Pair(javaType, xmlType);

        df = (javax.xml.rpc.encoding.DeserializerFactory) pair2DF.get(pair);

        if (df == null && delegate != null) {
            df = delegate.getDeserializer(javaType, xmlType);
        }
        return df;
    }

    /**
     * Removes the SerializerFactory registered for the specified
     * pair of Java type and XML data type.
     *
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     *
     * @throws JAXRPCException - If there is error in
     * removing the registered SerializerFactory
     */
    public void removeSerializer(Class javaType, QName xmlType)
        throws JAXRPCException {
        if (javaType == null || xmlType == null) {
            throw new JAXRPCException(
                    Messages.getMessage(javaType == null ?
                                         "badJavaType" : "badXmlType"));
        }

        Pair pair = new Pair(javaType, xmlType);
        pair2SF.remove(pair);
    }

    /**
     * Removes the DeserializerFactory registered for the specified
     * pair of Java type and XML data type.
     *
     * @param javaType - Class of the Java type
     * @param xmlType - Qualified name of the XML data type
     *
     * @throws JAXRPCException - If there is error in
     * removing the registered DeserializerFactory
     */
    public void removeDeserializer(Class javaType, QName xmlType)
        throws JAXRPCException {
        if (javaType == null || xmlType == null) {
            throw new JAXRPCException(
                    Messages.getMessage(javaType == null ?
                                         "badJavaType" : "badXmlType"));
        }
        Pair pair = new Pair(javaType, xmlType);
        pair2DF.remove(pair);
    }


     /********* End JAX-RPC Compliant Method Definitions *****************/

    /**
     * Gets the QName for the type mapped to Class.
     * @param javaType class or type
     * @return xmlType qname or null
     */
    public QName getTypeQNameRecursive(Class javaType) {
        QName ret = null;
        while (javaType != null) {
            ret = getTypeQName(javaType);
            if (ret != null)
                return ret;

            // Walk my interfaces...
            Class [] interfaces = javaType.getInterfaces();
            if (interfaces != null) {
                for (int i = 0; i < interfaces.length; i++) {
                    Class iface = interfaces[i];
                    ret = getTypeQName(iface);
                    if (ret != null)
                        return ret;
                }
            }

            javaType = javaType.getSuperclass();
        }
        return null;
    }

    /**
     * Get the QName for this Java class, but only return a specific
     * mapping if there is one.  In other words, don't do special array
     * processing, etc.
     * 
     * @param javaType
     * @return
     */
    public QName getTypeQNameExact(Class javaType) {
        if (javaType == null)
            return null;
       
        QName xmlType = null;
        Pair pair = (Pair) class2Pair.get(javaType);
        if (pair == null && delegate != null) {
            xmlType = delegate.getTypeQNameExact(javaType);
        } else if (pair != null) {
            xmlType = pair.xmlType;
        }

        return xmlType;
    }

    public QName getTypeQName(Class javaType) {
        QName xmlType = getTypeQNameExact(javaType);

        /* If auto-typing is on and the array has the default SOAP_ARRAY QName,
         * then generate a namespace for this array intelligently.   Also
         * register it's javaType and xmlType. List classes and derivitives
         * can't be used because they should be serialized as an anyType array.
         */
        if ( doAutoTypes && 
             javaType != List.class &&
             !List.class.isAssignableFrom(javaType) &&
             xmlType != null &&
             xmlType.equals(Constants.SOAP_ARRAY) )
        {
            xmlType = new QName(
                Namespaces.makeNamespace( javaType.getName() ),
                Types.getLocalNameFromFullName( javaType.getName() ) );
                
            register( javaType,
                      xmlType, 
                      new ArraySerializerFactory(),
                      new ArrayDeserializerFactory() );
        }
        
        // Can only detect arrays via code
        if (xmlType == null && isArray(javaType)) {

            // get the registered array if any
            Pair pair = (Pair) class2Pair.get(Object[].class);
            // TODO: it always returns the last registered one,
            //  so that's why the soap 1.2 typemappings have to 
            //  move to an other registry to differentiate them
            if (pair != null) {
                xmlType = pair.xmlType;
            } else {
                xmlType = Constants.SOAP_ARRAY;
            }
        }
        
        /* If the class isn't an array or List and auto-typing is turned on,
         * register the class and it's type as beans.
         */
        if (xmlType == null && doAutoTypes)
        {   
            xmlType = new QName(
                Namespaces.makeNamespace( javaType.getName() ),
                Types.getLocalNameFromFullName( javaType.getName() ) );
            
            /* If doAutoTypes is set, register a new type mapping for the
             * java class with the above QName.  This way, when getSerializer()
             * and getDeserializer() are called, this QName is returned and
             * these methods do not need to worry about creating a serializer.
             */
            register( javaType,
                      xmlType, 
                      new BeanSerializerFactory(javaType, xmlType),
                      new BeanDeserializerFactory(javaType, xmlType) );
        }

        //log.debug("getTypeQName xmlType =" + xmlType);
        return xmlType;
    }

    /**
     * Gets the Class mapped to QName.
     * @param xmlType qname or null
     * @return javaType class for type or null for no mapping
     */
    public Class getClassForQName(QName xmlType) {
        if (xmlType == null) {
            return null;
        }

        //log.debug("getClassForQName xmlType =" + xmlType);
        Class javaType = null;
        //look for it in our map
        Pair pair = (Pair) qName2Pair.get(xmlType);
        if (pair == null && delegate != null) {
            //on no match, delegate
            javaType = delegate.getClassForQName(xmlType);
        } else if (pair != null) {
            javaType = pair.javaType;
        }

        //log.debug("getClassForQName javaType =" + javaType);
        return javaType;
    }

    /**
     * Gets the SerializerFactory registered for the Java type.
     *
     * @param javaType - Class of the Java type
     *
     * @return Registered SerializerFactory
     *
     * @throws JAXRPCException - If there is no registered SerializerFactory
     * for this pair of Java type and XML data type
     * java.lang.IllegalArgumentException -
     * If invalid or unsupported XML/Java type is specified
     */
    public javax.xml.rpc.encoding.SerializerFactory
        getSerializer(Class javaType)
        throws JAXRPCException
    {
        return getSerializer(javaType, null);
    }

    /**
     * Gets the DeserializerFactory registered for the xmlType.
     *
     * @param xmlType - Qualified name of the XML data type
     *
     * @return Registered DeserializerFactory
     *
     * @throws JAXRPCException - If there is no registered DeserializerFactory
     * for this pair of Java type and  XML data type
     * java.lang.IllegalArgumentException -
     * If invalid or unsupported XML/Java type is specified
     */
    public javax.xml.rpc.encoding.DeserializerFactory
        getDeserializer(QName xmlType)
        throws JAXRPCException {
        return getDeserializer(null, xmlType);
    }

    public void setDoAutoTypes(boolean doAutoTypes) {
        this.doAutoTypes = doAutoTypes;
    }

    /**
     * Returns an array of all the classes contained within this mapping
     */
    public Class [] getAllClasses()
    {
        java.util.HashSet temp = new java.util.HashSet();
        if (delegate != null)
        {
            temp.addAll(java.util.Arrays.asList(delegate.getAllClasses()));
        }
        temp.addAll(class2Pair.keySet());
        return (Class[])temp.toArray(new Class[temp.size()]);
    }
}
