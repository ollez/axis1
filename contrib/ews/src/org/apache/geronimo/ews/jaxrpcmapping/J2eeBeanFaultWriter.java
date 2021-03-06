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

package org.apache.geronimo.ews.jaxrpcmapping;

import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis.wsdl.toJava.JavaWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * This is Wsdl2java's Complex Fault Writer.
 * It generates bean-like class for complexTypes used
 * in a operation fault message.
 *
 * @author Ias (iasandcb@tmax.co.kr)
 * @deprecated no more used by J2eeGeneratorFactory
 */
public class J2eeBeanFaultWriter extends J2eeBeanWriter {
    /**
     * Constructor.
     *
     * @param emitter
     * @param type       The type representing this class
     * @param elements   Vector containing the Type and name of each property
     * @param extendType The type representing the extended class (or null)
     * @param attributes Vector containing the attribute types and names
     * @param helper     Helper class writer
     */
    protected J2eeBeanFaultWriter(J2eeEmitter emitter,
                                  TypeEntry type,
                                  Vector elements,
                                  TypeEntry extendType,
                                  Vector attributes,
                                  JavaWriter helper) {
        super(emitter, type, elements,
                extendType, attributes, helper);

        // The Default Constructor is not JSR 101 v1.0 compliant, but
        // is the only way that Axis can get something back over the wire.
        // This will need to be changed when fault contents are supported
        // over the wire.
        enableDefaultConstructor = true;

        // JSR 101 v1.0 requires a full constructor
        enableFullConstructor = true;

        // JSR 101 v1.0 does not support write access methods
        enableSetters = true;
    } // ctor

    /**
     * Returns the appropriate extends text
     *
     * @return "" or " extends <class> "
     */
    protected String getExtendsText() {
        // See if this class extends another class
        String extendsText = super.getExtendsText();
        if (extendsText.equals("")) {
            // JSR 101 compliant code should extend java.lang.Exception!
            //extendsText = " extends java.lang.Exception ";
            extendsText = " extends org.apache.axis.AxisFault ";
        }
        return extendsText;
    }

    /**
     * Write the Exception serialization code
     * NOTE: This function is written in JavaFaultWriter.java also.
     */
    protected void writeFileFooter(PrintWriter pw) throws IOException {
        // We need to have the Exception class serialize itself
        // with the correct namespace, which can change depending on which
        // operation the exception is thrown from.  We therefore have the
        // framework call this generated routine with the correct QName,
        // and allow it to serialize itself.

        // method that serializes this exception (writeDetail)
        pw.println();
        pw.println("    /**");
        pw.println("     * Writes the exception data to the faultDetails");
        pw.println("     */");
        pw.println("    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {");
        pw.println("        context.serialize(qname, null, this);");
        pw.println("    }");
        super.writeFileFooter(pw);
    } // writeFileFooter
} // class JavaBeanFaultWriter
