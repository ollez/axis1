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

import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis.wsdl.toJava.JavaClassWriter;
import org.apache.axis.wsdl.toJava.Utils;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.xml.rpc.holders.BooleanHolder;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * This is Wsdl2java's implementation template writer.  It writes the <BindingName>Impl.java
 * file which contains the <bindingName>Impl class.
 * 
 * @author Ias (iasandcb@tmax.co.kr)
 */
public class J2eeImplWriter extends JavaClassWriter {
    protected Binding binding;
    protected SymbolTable symbolTable;
    protected BindingEntry bEntry;

    /**
     * Constructor.
     */
    protected J2eeImplWriter(J2eeEmitter emitter,
                             BindingEntry bEntry,
                             SymbolTable symbolTable) {
        super(emitter, bEntry.getName() + "Impl", "templateImpl");
        this.binding = bEntry.getBinding();
        this.symbolTable = symbolTable;
        this.bEntry = bEntry;
    } // ctor

    /**
     * Write the body of the binding's stub file.
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {
        List operations = binding.getBindingOperations();
        for (int i = 0; i < operations.size(); ++i) {
            BindingOperation operation = (BindingOperation) operations.get(i);
            Operation ptOperation = operation.getOperation();
            OperationType type = ptOperation.getStyle();
            Parameters parameters =
                    bEntry.getParameters(operation.getOperation());

            // These operation types are not supported.  The signature
            // will be a string stating that fact.
            if (type == OperationType.NOTIFICATION
                    || type == OperationType.SOLICIT_RESPONSE) {
                pw.println(parameters.signature);
                pw.println();
            } else {
                writeOperation(pw, parameters);
            }
        }
    } // writeFileBody

    /**
     * Returns the appropriate implements text
     * 
     * @return " implements <classes>"
     */
    protected String getImplementsText() {
        String portTypeName = (String) bEntry.getDynamicVar(J2eeBindingWriter.INTERFACE_NAME);
        String implementsText = "implements " + portTypeName;
        return implementsText;
    }

    /**
     * Write the implementation template for the given operation.
     */
    protected void writeOperation(PrintWriter pw, Parameters parms) throws IOException {
        pw.println(parms.signature + " {");

        // Fill in any out parameter holders
        Iterator iparam = parms.list.iterator();
        while (iparam.hasNext()) {
            Parameter param = (Parameter) iparam.next();
            if (param.getMode() == Parameter.OUT) {
                // write a constructor for each of the parameters
                
                BooleanHolder bThrow = new BooleanHolder(false);
                String constructorString =
                        Utils.getConstructorForParam(param, symbolTable, bThrow);
                if (bThrow.value) {
                    pw.println("        try {");
                }
                pw.println("        " + Utils.xmlNameToJava(param.getName())
                        + ".value = " + constructorString + ";");
                if (bThrow.value) {
                    pw.println("        } catch (Exception e) {");
                    pw.println("        }");
                }
            }
        }

        // Print the return statement
        if (parms.returnParam != null) {
            TypeEntry returnType = parms.returnParam.getType();
            pw.print("        return ");

            if (Utils.isPrimitiveType(returnType)) {
                String returnString = returnType.getName();
                if ("boolean".equals(returnString)) {
                    pw.println("false;");
                } else if ("byte".equals(returnString)) {
                    pw.println("(byte)-3;");
                } else if ("short".equals(returnString)) {
                    pw.println("(short)-3;");
                } else {
                    pw.println("-3;");
                }
            } else {
                pw.println("null;");
            }
        }
        pw.println("    }");
        pw.println();
    } // writeOperation

}
