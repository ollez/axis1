/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
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
 *
 */
package org.apache.geronimo.ews.ws4j2ee.toWs.wrapperWs.geronimo;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.ews.ws4j2ee.context.J2EEWebServiceContext;
import org.apache.geronimo.ews.ws4j2ee.context.SEIOperation;
import org.apache.geronimo.ews.ws4j2ee.toWs.GenerationFault;
import org.apache.geronimo.ews.ws4j2ee.toWs.JavaClassWriter;
import org.apache.geronimo.ews.ws4j2ee.utils.Utils;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class InternalBasedWrapperClassWriter extends JavaClassWriter {
    protected static Log log =
        LogFactory.getLog(InternalBasedWrapperClassWriter.class.getName());
    protected final String seiName;
    protected final String ejbName;
    /**
     * @param j2eewscontext
     * @param qulifiedName
     * @throws GenerationFault
     */
    public InternalBasedWrapperClassWriter(J2EEWebServiceContext j2eewscontext)
        throws GenerationFault {
        super(j2eewscontext, getName(j2eewscontext) + "Impl");
        seiName = j2eewscontext.getMiscInfo().getJaxrpcSEI();
        ejbName = j2eewscontext.getEJBDDContext().getEjbName();

    }

    private static String getName(J2EEWebServiceContext j2eewscontext) {
        String name =
            j2eewscontext.getWSDLContext().gettargetBinding().getName();
        if (name == null) {
            name = j2eewscontext.getMiscInfo().getJaxrpcSEI();
        }
        return name;
    }

    protected String getimplementsPart() {
        return " implements " + j2eewscontext.getMiscInfo().getJaxrpcSEI()+","+"org.apache.geronimo.ews.ws4j2ee.wsutils.ContextAccssible";
    }

    protected void writeAttributes() throws GenerationFault {
        out.write("private org.apache.axis.MessageContext msgcontext;\n");
        out.write("\tprivate " + seiName + " bean = null;\n");
        out.write("\tprivate org.openejb.EJBContainer container;\n");
    }

    protected void writeConstructors() throws GenerationFault {
        out.write("\tpublic " + classname + "(){\n");
        out.write("\t}\n");
    }

    protected void writeMethods() throws GenerationFault {
        out.write("\tpublic void setMessageContext(org.apache.axis.MessageContext msgcontext){;\n");
        out.write("\t\tthis.msgcontext = msgcontext;\n");
        out.write("\t}\n");
        
        String parmlistStr = null;
        ArrayList operations = j2eewscontext.getMiscInfo().getSEIOperations();
        for (int i = 0; i < operations.size(); i++) {
            parmlistStr = "";
            SEIOperation op = (SEIOperation) operations.get(i);
            String returnType = op.getReturnType();
            if (returnType == null)
                returnType = "void";
            out.write(
                "\tpublic " + returnType + " " + op.getMethodName() + "(");

            Iterator pas = op.getParameterNames().iterator();
            boolean first = true;
            while (pas.hasNext()) {
                String name = (String) pas.next();
                String type = op.getParameterType(name);
                if (first) {
                    first = false;
                    out.write(type + " " + name);
                    parmlistStr = parmlistStr + name;
                } else {
                    out.write("," + type + " " + name);
                    parmlistStr = parmlistStr + "," + name;
                }

            }

            out.write(") throws java.rmi.RemoteException");
            ArrayList faults = op.getFaults();
            for (int j = 0; j < faults.size(); j++) {
                out.write("," + (String) faults.get(i));
            }
            out.write("{\n");

            out.write(
                "\t\tString methodName = \"" + op.getMethodName() + "\";\n");
            out.write(
                "\t\tjava.lang.reflect.Method callMethod = Utils.getJavaMethod(\""
                    + seiName
                    + "\",methodName);\n");
            out.write(
                "\t\tClass[] classes = callMethod.getParameterTypes();\n");

            out.write("\t\tObject[] arguments = new Object[]{");
            pas = op.getParameterNames().iterator();
            first = true;
            while (pas.hasNext()) {
                String name = (String) pas.next();
                String type = op.getParameterType(name);
                if (first) {
                    first = false;
                    out.write(Utils.getParameter(type, name));
                } else {
                    out.write("," + Utils.getParameter(type, name));
                }
            }
            out.write("};\n");
            if (!"void".equals(returnType)) {
                out.write(
                    "\t\t\treturn "
                        + Utils.getReturnCode(returnType, "AxisGeronimoUtils.invokeEJB(\""
                        + ejbName
                        + "\",methodName,classes,arguments)")
                        + ";\n");
            } else {
                out.write(
                    "\t\t\tAxisGeronimoUtils.invokeEJB(\""
                        + ejbName
                        + "\",methodName,classes,arguments);\n");
            }
            out.write("\t}\n");
        }
    }
    /* (non-Javadoc)
     * @see org.apache.geronimo.ews.ws4j2ee.toWs.JavaClassWriter#writeImportStatements()
     */
    protected void writeImportStatements() throws GenerationFault {
        out.write("import org.apache.geronimo.axis.AxisGeronimoUtils;\n");
        out.write("import org.apache.geronimo.ews.ws4j2ee.utils.Utils;\n");
    }

}

