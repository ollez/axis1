/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package test.message;

import junit.framework.TestCase;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.Name;
import javax.xml.namespace.QName;

import org.apache.axis.soap.SOAPConstants;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.EnvelopeBuilder;
import org.apache.axis.Message;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.DeserializationContextImpl;
import org.xml.sax.Attributes;

import java.util.Iterator;

/**
 * Test MessageElement class.
 *
 * @author Glyn Normington (glyn@apache.org)
 */
public class TestMessageElement extends TestCase {

    public TestMessageElement(String name) {
        super(name);
    }

    // Test JAXM methods...

    public void testParentage() throws Exception {
        SOAPElement parent = new MessageElement("ns", "parent");
        SOAPElement child = new MessageElement("ns", "child");
        child.setParentElement(parent);
        assertEquals("Parent is not as set", parent, child.getParentElement());
    }

    public void testAddChild() throws Exception {
        SOAPConstants sc = SOAPConstants.SOAP11_CONSTANTS;
        EnvelopeBuilder eb = new EnvelopeBuilder(Message.REQUEST, sc);
        DeserializationContext dc = new DeserializationContextImpl(null,
                                                                   eb); 
        MessageElement parent = new MessageElement("parent.names",
                                                "parent",
                                                "parns",
                                                null,
                                                dc);
        Name c1 = new PrefixedQName("child1.names", "child1" ,"c1ns");
        SOAPElement child1 = parent.addChildElement(c1);
        SOAPElement child2 = parent.addChildElement("child2");
        SOAPElement child3 = parent.addChildElement("child3.names", "parns");
        SOAPElement child4 = parent.addChildElement("child4",
                                                    "c4ns",
                                                    "child4.names");
        SOAPElement child5 = new MessageElement("ns", "child5");
        parent.addChildElement(child5);
        SOAPElement c[] = {child1, child2, child3, child4, child5}; 
        
        Iterator children = parent.getChildElements();
        for (int i = 0; i < 5; i++) {
            assertEquals("Child " + (i+1) + " not found",
                         c[i],
                         children.next());
        }
        assertTrue("Unexpected child", !children.hasNext());
       
        Iterator c1only = parent.getChildElements(c1);
        assertEquals("Child 1 not found", child1, c1only.next());
        assertTrue("Unexpected child", !c1only.hasNext());
    }
    public void testDetachNode() throws Exception {
        SOAPConstants sc = SOAPConstants.SOAP11_CONSTANTS;
        EnvelopeBuilder eb = new EnvelopeBuilder(Message.REQUEST, sc);
        DeserializationContext dc = new DeserializationContextImpl(null,
                                                                   eb); 
        SOAPElement parent = new MessageElement("parent.names",
                                                "parent",
                                                "parns",
                                                null,
                                                dc);
        SOAPElement child1 = parent.addChildElement("child1");
        SOAPElement child2 = parent.addChildElement("child2");
        SOAPElement child3 = parent.addChildElement("child3");

        child2.detachNode();
        SOAPElement c[] = {child1, child3}; 
        
        Iterator children = parent.getChildElements();
        for (int i = 0; i < 2; i++) {
            assertEquals("Child not found",
                         c[i],
                         children.next());
        }
        assertTrue("Unexpected child", !children.hasNext());
    }

    public void testGetCompleteAttributes() throws Exception {
        MessageElement me = 
            new MessageElement("http://www.wolfram.com","Test");
        me.addNamespaceDeclaration("pre", "http://www.wolfram2.com");
        Attributes attrs = me.getCompleteAttributes();
        assertEquals(attrs.getLength(), 1);
    }
    
    public void testAddNamespaceDeclaration() throws Exception {
        MessageElement me = 
            new MessageElement("http://www.wolfram.com","Test");
        me.addNamespaceDeclaration("pre", "http://www.wolfram2.com");
        me.addAttribute(
            "http://www.w3.org/2001/XMLSchema-instance", 
            "type",
            "pre:test1");
        boolean found = false;
        Iterator it = me.getNamespacePrefixes();
        while(!found && it.hasNext()){ 
            String prefix = (String)it.next();
            if (prefix.equals("pre") && 
                me.getNamespaceURI(prefix).equals("http://www.wolfram2.com")) {
                found = true;
            }
        }
        assertTrue("Did not find namespace declaration \"pre\"", found);
    }
    
    public void testQNameAttrTest() throws Exception {
        MessageElement me = 
            new MessageElement("http://www.wolfram.com","Test");
        me.addAttribute(
            "http://www.w3.org/2001/XMLSchema-instance", 
            "type",
            new QName("http://www.wolfram2.com", "type1"));
        MessageElement me2 = 
            new MessageElement("http://www.wolfram.com", "Child", (Object)"1");
        me2.addAttribute(
            "http://www.w3.org/2001/XMLSchema-instance", 
            "type",
            new QName("http://www.w3.org/2001/XMLSchema", "int"));
        me.addChildElement(me2);
        String s1 = me.toString();
        String s2 = me.toString();
        assertEquals(s1, s2);
    }
    
    public static void main(String[] args) throws Exception {
        TestMessageElement tester = new TestMessageElement("TestMessageElement");
        tester.testQNameAttrTest();
    }
}
