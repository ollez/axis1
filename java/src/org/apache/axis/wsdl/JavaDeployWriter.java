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

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.QName;
import javax.wsdl.Service;

import org.apache.axis.utils.JavaUtils;

/**
* This is Wsdl2java's deploy Writer.  It writes the deploy.java file.
*/
public class JavaDeployWriter extends JavaWriter {
    private Definition definition;
    private SymbolTable symbolTable;

    /**
     * Constructor.
     */
    protected JavaDeployWriter(Emitter emitter, Definition definition, SymbolTable symbolTable) {
        super(emitter,
                new QName(definition.getTargetNamespace(), "deploy"),
                "",
                "wsdd",
                JavaUtils.getMessage("genDeploy00"));
        this.definition = definition;
        this.symbolTable = symbolTable;
    } // ctor

    /**
     * Replace the default file header with the deployment doc file header.
     */
    protected void writeFileHeader() throws IOException {
        initializeDeploymentDoc("deploy");
    } // writeFileHeader

    /**
     * Write the body of the deploy.xml file.
     */
    protected void writeFileBody() throws IOException {
        writeDeployServices();
        writeDeployTypes();
        pw.println("</deployment>");
        pw.close();
    } // writeFileBody

    /**
     * Write out deployment and undeployment instructions for each WSDL service
     */
    private void writeDeployServices() throws IOException {
        //deploy the ports on each service
        Map serviceMap = definition.getServices();
        for (Iterator mapIterator = serviceMap.values().iterator(); mapIterator.hasNext();) {
            Service myService = (Service) mapIterator.next();

            pw.println();
            pw.println("  <!-- " + JavaUtils.getMessage(
                    "wsdlService00", myService.getQName().getLocalPart())
                    + " -->");
            pw.println();

            for (Iterator portIterator = myService.getPorts().values().iterator(); portIterator.hasNext();) {
                Port myPort = (Port) portIterator.next();
                writeDeployPort(myPort);
            }
        }
    } //writeDeployServices

    /**
     * Write out bean mappings for each type
     */
    private void writeDeployTypes() throws IOException {
        Vector types = symbolTable.getTypes();

        pw.println();
        for (int i = 0; i < types.size(); ++i) {
            Type type = (Type) types.elementAt(i);
            if (type.getBaseType() == null && type.isReferenced() && type.getShouldEmit()) {
                pw.println("  <beanMapping");
                pw.println("    xmlns:ns=\"" + type.getQName().getNamespaceURI() + "\"");
                pw.println("    qname=\"ns:" + type.getQName().getLocalPart() + '"');
                pw.println("    languageSpecificType=\"java:" + type.getJavaName() + '"');
                pw.println("  />");
            }
        }
    } //writeDeployTypes

    /**
     * Write out deployment and undeployment instructions for given WSDL port
     */
    private void writeDeployPort(Port port) throws IOException {
        Binding binding = port.getBinding();
        BindingEntry bEntry = symbolTable.getBindingEntry(binding.getQName());
        String serviceName = port.getName();

        boolean isRPC = (bEntry.getBindingStyle() == BindingEntry.STYLE_RPC);

        pw.println("  <service name=\"" + serviceName
                + "\" provider=\"" + (isRPC ? "java:RPC" : "java:MSG") + "\">");

        writeDeployBinding(binding);

        pw.println("  </service>");
    } //writeDeployPort

    /**
     * Write out deployment instructions for given WSDL binding
     */
    private void writeDeployBinding(Binding binding) throws IOException {
        QName bindingQName = binding.getQName();
        String packageName = namespaces.getCreate(bindingQName.getNamespaceURI());
        pw.println("      <parameter name=\"className\" value=\""
                         + packageName + "."
                         + bindingQName.getLocalPart() + "Skeleton" + "\"/>");

        String methodList = "";
        Iterator operationsIterator = binding.getBindingOperations().iterator();
        for (; operationsIterator.hasNext();) {
            BindingOperation op = (BindingOperation) operationsIterator.next();
            methodList = methodList + " " + op.getName();
        }

        pw.println("      <parameter name=\"methodName\" value=\"" + methodList + "\"/>");

        if (emitter.getScope() == Emitter.APPLICATION_SCOPE) {
            pw.println("      <parameter name=\"scope\" value=\"Application\"/>");
        }
        else if (emitter.getScope() == Emitter.REQUEST_SCOPE) {
            pw.println("      <parameter name=\"scope\" value=\"Request\"/>");
        }
        else if (emitter.getScope() == Emitter.SESSION_SCOPE) {
            pw.println("      <parameter name=\"scope\" value=\"Session\"/>");
        }
    } //writeDeployBinding

} // class JavaDeployWriter
