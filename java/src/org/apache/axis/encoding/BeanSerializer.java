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

package org.apache.axis.encoding;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.axis.encoding.*;
import org.apache.axis.utils.QName;

import org.xml.sax.*;

/**
 * General purpose serializer/deserializerFactory for an arbitrary java bean.
 *
 * @author Sam Ruby <rubys@us.ibm.com>
 */
public class BeanSerializer extends DeserializerBase implements Serializer {

    /**
     * Class being serialized/deserialized
     */
    private Class cls;

    /**
     * Property Descriptors.  Retrieved once and cached in the serializer.
     */
    private PropertyDescriptor[] pd;
    
    /**
     * An array of nothing, defined only once.
     */
    private static final Object[] noArgs = new Object[] {};

    /**
     * Constructor that takes a class.  Called only for serializers, so it
     * does NOT creates an instance of the bean.
     */
    public BeanSerializer(Class cls) throws Exception {
        this.cls = cls;
        this.pd = Introspector.getBeanInfo(cls).getPropertyDescriptors();
    }

    /**
     * Constructor that takes a class and a pre-computed PropertyDescriptor.
     * Called only for deserializers, so it DOES create an instance.
     */
    public BeanSerializer(Class cls, PropertyDescriptor[] pd) throws Exception {
        this.cls = cls;
        this.pd = pd;
        this.value = cls.newInstance();
    }

    /**
     * BeanSerializer Factory that creates instances with the specified
     * class.  Caches the PropertyDescriptor
     */
    private static class BeanSerFactory implements DeserializerFactory {
        private Class cls;
        private PropertyDescriptor[] pd;

        public BeanSerFactory(Class cls) throws Exception{
            this.cls = cls;
            this.pd = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        }

        public DeserializerBase getDeserializer() {
            try {
                return new BeanSerializer(cls, pd);
            } catch (Exception e) {
                // I'm not allowed to throw much, so I throw what I can!
                throw new NullPointerException(e.toString());
            }
        }
    }

    /**
     * Accessor for the BeanSerializerFactory
     */
    public static DeserializerFactory getFactory(Class cls) throws Exception {
        return new BeanSerFactory(cls);
    }

    /**
     * Class which knows how to update a bean property
     */
    class PropertyTarget implements Target {
        private Object object;
        private PropertyDescriptor pd;

        public PropertyTarget(Object object, PropertyDescriptor pd) {
            this.object = object;
            this.pd     = pd;
        }

        public void set(Object value) throws SAXException {
            try {
                pd.getWriteMethod().invoke(object, new Object[] {value});
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
    }
    
    /** 
     * Deserializer interface called on each child element encountered in
     * the XML stream.
     */
    public void onStartChild(String namespace, String localName,
                             String qName, Attributes attributes)
        throws SAXException
    {

        // look for a field by this name.  Assumes the the number of
        // properties in a bean is (relatively) small, so uses a linear
        // search.
        for (int i=0; i<pd.length; i++) {
            if (pd[i].getName().equals(localName)) {

                // determine the QName for this child element
                TypeMappingRegistry tmr = context.getTypeMappingRegistry();
                QName qn = tmr.getTypeQName(pd[i].getPropertyType());
                if (qn == null)
                    throw new SAXException("Unregistered type: " + 
                                           pd[i].getPropertyType());

                // get the deserializer
                DeserializerBase dSer = context.getDeserializer(qn);
                if (dSer == null)
                    throw new SAXException("No deserializer for " + 
                                           pd[i].getPropertyType());

                // Success!  Register the target and deserializer.
                dSer.registerValueTarget(new PropertyTarget(value, pd[i]));
                context.pushElementHandler(dSer);
                return;
            }
        }

        // No such field
        throw new SAXException("Invalid element in " + cls.getName() +
                               " - " + localName);
    }
    
    /** 
     * Serialize a bean.  Done simply by serializing each bean property.
     */
    public void serialize(QName name, Attributes attributes,
                          Object value, SerializationContext context)
        throws IOException
    {
        context.startElement(name, attributes);

        try {
            for (int i=0; i<pd.length; i++) {
                String propName = pd[i].getName();
                if (propName.equals("class")) continue;
                Object propValue = pd[i].getReadMethod().invoke(value,noArgs);
                context.serialize(new QName("", propName), null, propValue);
            }
        } catch (Exception e) {
        e.printStackTrace();
            throw new IOException(e.toString());
        }

        context.endElement();
    }
}
