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

package org.apache.axis.encoding.ser;

import org.apache.axis.Constants;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerFactory;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.namespace.QName;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

/**
 * Base class for Axis Deserialization Factory classes for code reuse
 *
 * @author Rich Scheuerle <scheu@us.ibm.com>
 */
public abstract class BaseDeserializerFactory 
    implements DeserializerFactory {

    static Vector mechanisms = null;
    
    protected Class deserClass = null;
    protected QName xmlType = null;
    protected Class javaType = null;
    protected Constructor deserClassConstructor = null;
    protected Method getDeserializer = null;
    protected boolean firstCall = true;

    /**
     * Constructor
     * @param deserClass is the class of the Deserializer
     */
    public BaseDeserializerFactory(Class deserClass) {
        this.deserClass = deserClass;
    }
    public BaseDeserializerFactory(Class deserClass,
                                   QName xmlType,
                                   Class javaType) {
        this.deserClass = deserClass;
        this.xmlType = xmlType;
        this.javaType = javaType;
    }

    public javax.xml.rpc.encoding.Deserializer
        getDeserializerAs(String mechanismType)
        throws JAXRPCException {
        Deserializer deser = null;

        // Need to add code to check against mechanisms vector.
        try {
            // Try getting a specialized Deserializer
            deser = getSpecialized(mechanismType);
            
            // Try getting a general purpose Deserializer via constructor
            // invocation
            if (deser == null) {
                deser = getGeneralPurpose(mechanismType);
            }
            
            // If not successfull, try newInstance
            if (deser == null) {
                deser = (Deserializer) deserClass.newInstance();
            }
        } catch (Exception e) {
            throw new JAXRPCException(e);
        }
        firstCall = false;
        return deser;
    }

   /**
     * Obtains a deserializer by invoking <constructor>(javaType, xmlType) 
     * on the deserClass.
     */
    protected Deserializer getGeneralPurpose(String mechanismType) {
        if (javaType != null && xmlType != null) {
            if (deserClassConstructor == null && firstCall) {
                try {
                    deserClassConstructor = 
                        deserClass.getConstructor(
                            new Class[] {Class.class, QName.class});
                } catch (NoSuchMethodException e) {}
            }
            if (deserClassConstructor != null) {
                try {
                    return (Deserializer) 
                        deserClassConstructor.newInstance(
                            new Object[] {javaType, xmlType});
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {}
            }
        }
        return null;
    }

    /**
     * Obtains a deserializer by invoking getDeserializer method in the 
     * javaType class or its Helper class.
     */
    protected Deserializer getSpecialized(String mechanismType) {
        if (javaType != null && xmlType != null) {
            if (getDeserializer == null && firstCall) {
                try {
                    getDeserializer = 
                        javaType.getMethod("getDeserializer",
                                           new Class[] {String.class, 
                                                        Class.class, 
                                                        QName.class});
                } catch (NoSuchMethodException e) {}
                if (getDeserializer == null) {
                    try {
                        ClassLoader cl = 
                            Thread.currentThread().getContextClassLoader();
                        Class helper = Class.forName(
                            javaType.getName() + "_Helper", true, cl);
                        getDeserializer =
                            helper.getMethod("getDeserializer", 
                                             new Class[] {String.class, 
                                                          Class.class, 
                                                          QName.class});
                    } catch (NoSuchMethodException e) {
                    } catch (ClassNotFoundException e) {}
                }
            }
            if (getDeserializer != null) {
                try {
                    return (Deserializer) 
                        getDeserializer.invoke(
                                             null,
                                             new Object[] {mechanismType, 
                                                           javaType, 
                                                           xmlType});
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {}
            }
        }
        return null;
    }

    /**
     * Returns a list of all XML processing mechanism types supported by this DeserializerFactory.
     *
     * @return List of unique identifiers for the supported XML processing mechanism types
     */
    public Iterator getSupportedMechanismTypes() {
        if (mechanisms == null) {
            mechanisms = new Vector();
            mechanisms.add(Constants.AXIS_SAX);
        }
        return mechanisms.iterator();
    }

    /**
     * Utility method that intospects on a factory class to decide how to 
     * create the factory.  Tries in the following order:
     * public static create(Class javaType, QName xmlType)
     * public <constructor>(Class javaType, QName xmlType)
     * public <constructor>()
     * @param factory class
     * @param javaType
     * @param xmlType
     */
    public static DeserializerFactory createFactory(Class factory, 
                                                    Class javaType, 
                                                    QName xmlType) {

        DeserializerFactory df = null;
        try {
            Method method = 
                factory.getMethod(
                    "create", 
                    new Class[] {Class.class, QName.class});
            df = (DeserializerFactory) 
                method.invoke(null, 
                              new Object[] {javaType, xmlType});
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {}

        if (df == null) {
            try {
                Constructor constructor =  
                    factory.getConstructor(
                        new Class[] {Class.class, QName.class});
                df = (DeserializerFactory) 
                    constructor.newInstance(
                        new Object[] {javaType, xmlType});
            } catch (NoSuchMethodException e) {
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {}
        }
        
        if (df == null) {
            try {
                df = (DeserializerFactory) factory.newInstance();
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {}
        }
        return df;
    }
}
