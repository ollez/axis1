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

import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.WSDLUtils;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.PortTypeEntry;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.Utils;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * This is Wsdl2java's service implementation writer.
 * It writes the <serviceName>Locator.java file.
 *
 * @author Ias (iasandcb@tmax.co.kr)
 * @deprecated no more used by J2eeGeneratorFactory
 */
public class J2eeServiceImplWriter extends J2eeClassWriter {
    private ServiceEntry sEntry;
    private SymbolTable symbolTable;

    /**
     * Constructor.
     */
    protected J2eeServiceImplWriter(J2eeEmitter emitter,
                                    ServiceEntry sEntry,
                                    SymbolTable symbolTable) {
//        super(emitter, sEntry.getName() + "Locator", "service");
        super(emitter, sEntry.getName() + "_Impl", "service");
        this.sEntry = sEntry;
        this.symbolTable = symbolTable;
    } // ctor

    /**
     * Returns "extends org.apache.axis.client.Service ".
     */
    protected String getExtendsText() {
        return "extends org.apache.axis.client.Service ";
    } // getExtendsText

    /**
     * Returns "implements <serviceInterface>".
     */
    protected String getImplementsText() {
        return "implements " + jaxRpcMapper.getServiceInterfaceName(sEntry) + ' ';
    } // getImplementsText

    /**
     * Write the body of the service file.
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {
        Service service = sEntry.getService();
        // output comments
        writeComment(pw, service.getDocumentationElement(), false);

        // Used to construct the getPort(Class) method.
        Vector getPortIfaces = new Vector();
        Vector getPortStubClasses = new Vector();
        Vector getPortPortNames = new Vector();
        boolean printGetPortNotice = false;

        // get ports
        Map portMap = service.getPorts();
        Iterator portIterator = portMap.values().iterator();

        // write a get method for each of the ports with a SOAP binding
        while (portIterator.hasNext()) {
            Port p = (Port) portIterator.next();
            Binding binding = p.getBinding();
            if (binding == null) {
                throw new IOException(Messages.getMessage("emitFailNoBinding01",
                        new String[]{p.getName()}));
            }
            BindingEntry bEntry =
                    symbolTable.getBindingEntry(binding.getQName());
            if (bEntry == null) {
                throw new IOException(Messages.getMessage("emitFailNoBindingEntry01",
                        new String[]{binding.getQName().toString()}));
            }
            PortTypeEntry ptEntry = symbolTable.getPortTypeEntry(binding.getPortType().getQName());
            if (ptEntry == null) {
                throw new IOException(Messages.getMessage("emitFailNoPortType01",
                        new String[]{binding.getPortType().getQName().toString()}));
            }

            // If this isn't an SOAP binding, skip it
            if (bEntry.getBindingType() != BindingEntry.TYPE_SOAP) {
                continue;
            }

            // JSR 101 indicates that the name of the port used
            // in the java code is the name of the wsdl:port.  It
            // does not indicate what should occur if the
            // wsdl:port name is not a java identifier.  The
            // TCK depends on the case-sensitivity being preserved,
            // and the interop tests have port names that are not
            // valid java identifiers.  Thus the following code.
            String portName = p.getName();
            if (!JavaUtils.isJavaId(portName)) {
                portName = Utils.xmlNameToJavaClass(portName);
            }
            String stubClass = bEntry.getName() + "Stub";
            String bindingType = (String) bEntry.getDynamicVar(J2eeBindingWriter.INTERFACE_NAME);

            // getPort(Class) must return a stub for an interface.  Collect all
            // the port interfaces so the getPort(Class) method can be constructed.
            if (getPortIfaces.contains(bindingType)) {
                printGetPortNotice = true;
            }
            getPortIfaces.add(bindingType);
            getPortStubClasses.add(stubClass);
            getPortPortNames.add(portName);

            // Get endpoint address and validate it
            String address = WSDLUtils.getAddressFromPort(p);
            if (address == null) {
                // now what?
                throw new IOException(Messages.getMessage("emitFail02",
                        portName, className));
            }
            try {
                new URL(address);
            } catch (MalformedURLException e) {
                throw new IOException(Messages.getMessage("emitFail03",
                        new String[]{portName, className, address}));
            }
            writeAddressInfo(pw, portName, address, p);
            String wsddServiceName = portName + "WSDDServiceName";
            writeWSDDServiceNameInfo(pw, wsddServiceName, portName);
            writeGetPortName(pw, bindingType, portName);
            writeGetPortNameURL(pw, bindingType, portName, stubClass,
                    wsddServiceName);
        }
        writeGetPortClass(pw, getPortIfaces, getPortStubClasses,
                getPortPortNames, printGetPortNotice);
        writeGetPortQNameClass(pw, getPortPortNames);
        writeGetServiceName(pw, sEntry.getQName());
        writeGetPorts(pw, getPortPortNames);
    } // writeFileBody

    /**
     * Write the private address field for this port and the public getter for it.
     */
    protected void writeAddressInfo(PrintWriter pw, String portName,
                                    String address, Port p) {
        // Write the private address field for this port
        pw.println();
        pw.println("    // " + Messages.getMessage("getProxy00", portName));
        writeComment(pw, p.getDocumentationElement(), true);
        pw.println("    private final java.lang.String " + portName + "_address = \"" + address + "\";");

        // Write the public address getter for this field
        pw.println();
        pw.println("    public java.lang.String get" + portName + "Address() {");
        pw.println("        return " + portName + "_address;");
        pw.println("    }");
        pw.println();
    } // writeAddressInfo

    /**
     * Write the private WSDD service name field and the public accessors for it.
     */
    protected void writeWSDDServiceNameInfo(PrintWriter pw,
                                            String wsddServiceName, String portName) {
        // Write the private WSDD service name field
        pw.println("    // " + Messages.getMessage("wsddServiceName00"));
        pw.println("    private java.lang.String " + wsddServiceName + " = \"" + portName + "\";");
        pw.println();

        // Write the public accessors for the WSDD service name
        pw.println("    public java.lang.String get" + wsddServiceName + "() {");
        pw.println("        return " + wsddServiceName + ";");
        pw.println("    }");
        pw.println();
        pw.println("    public void set" + wsddServiceName + "(java.lang.String name) {");
        pw.println("        " + wsddServiceName + " = name;");
        pw.println("    }");
        pw.println();
    } // writeWSDDServiceNameInfo

    /**
     * Write the get<portName>() method.
     */
    protected void writeGetPortName(PrintWriter pw, String bindingType,
                                    String portName) {
        pw.println("    public " + bindingType + " get" + portName + "() throws " + javax.xml.rpc.ServiceException.class.getName() + " {");
        pw.println("       java.net.URL endpoint;");
        pw.println("        try {");
        pw.println("            endpoint = new java.net.URL(" + portName + "_address);");
        pw.println("        }");
        pw.println("        catch (java.net.MalformedURLException e) {");
        pw.println("            throw new javax.xml.rpc.ServiceException(e);");
        pw.println("        }");
        pw.println("        return get" + portName + "(endpoint);");
        pw.println("    }");
        pw.println();
    } // writeGetPortName

    /**
     * Write the get<portName>(URL) method.
     */
    protected void writeGetPortNameURL(PrintWriter pw, String bindingType,
                                       String portName, String stubClass, String wsddServiceName) {
        pw.println("    public " + bindingType + " get" + portName + "(java.net.URL portAddress) throws " + javax.xml.rpc.ServiceException.class.getName() + " {");
        pw.println("        try {");
        pw.println("            " + stubClass + " _stub = new " + stubClass + "(portAddress, this);");
        pw.println("            _stub.setPortName(get" + wsddServiceName + "());");
        pw.println("            return _stub;");
        pw.println("        }");
        pw.println("        catch (org.apache.axis.AxisFault e) {");
        pw.println("            return null;");
        pw.println("        }");
        pw.println("    }");
        pw.println();
    } // writeGetPortNameURL

    /**
     * Write the getPort(Class serviceInterfaceWriter) method.
     */
    protected void writeGetPortClass(PrintWriter pw, Vector getPortIfaces,
                                     Vector getPortStubClasses, Vector getPortPortNames,
                                     boolean printGetPortNotice) {
        pw.println("    /**");
        pw.println("     * " + Messages.getMessage("getPortDoc00"));
        pw.println("     * " + Messages.getMessage("getPortDoc01"));
        pw.println("     * " + Messages.getMessage("getPortDoc02"));
        if (printGetPortNotice) {
            pw.println("     * " + Messages.getMessage("getPortDoc03"));
            pw.println("     * " + Messages.getMessage("getPortDoc04"));
        }
        pw.println("     */");
        pw.println("    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws " + javax.xml.rpc.ServiceException.class.getName() + " {");
        if (getPortIfaces.size() == 0) {
            pw.println("        throw new " + javax.xml.rpc.ServiceException.class.getName() + "(\""
                    + Messages.getMessage("noStub") + "  \" + (serviceEndpointInterface == null ? \"null\" : serviceEndpointInterface.getName()));");
        } else {
            pw.println("        try {");
            for (int i = 0; i < getPortIfaces.size(); ++i) {
                String iface = (String) getPortIfaces.get(i);
                String stubClass = (String) getPortStubClasses.get(i);
                String portName = (String) getPortPortNames.get(i);
                pw.println("            if (" + iface + ".class.isAssignableFrom(serviceEndpointInterface)) {");
                pw.println("                " + stubClass + " _stub = new " + stubClass + "(new java.net.URL(" + portName + "_address), this);");
                pw.println("                _stub.setPortName(get" + portName + "WSDDServiceName());");
                pw.println("                return _stub;");
                pw.println("            }");
            }
            pw.println("        }");
            pw.println("        catch (java.lang.Throwable t) {");
            pw.println("            throw new " + javax.xml.rpc.ServiceException.class.getName() + "(t);");
            pw.println("        }");
            pw.println("        throw new " + javax.xml.rpc.ServiceException.class.getName() + "(\""
                    + Messages.getMessage("noStub") + "  \" + (serviceEndpointInterface == null ? \"null\" : serviceEndpointInterface.getName()));");
        }
        pw.println("    }");
        pw.println();
    } // writeGetPortClass

    /**
     * Write the getPort(QName portName, Class serviceInterfaceWriter) method.
     */
    protected void writeGetPortQNameClass(PrintWriter pw,
                                          Vector getPortPortNames) {
        pw.println("    /**");
        pw.println("     * " + Messages.getMessage("getPortDoc00"));
        pw.println("     * " + Messages.getMessage("getPortDoc01"));
        pw.println("     * " + Messages.getMessage("getPortDoc02"));
        pw.println("     */");
        pw.println("    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws " + javax.xml.rpc.ServiceException.class.getName() + " {");
        pw.println("        if (portName == null) {");
        pw.println("            return getPort(serviceEndpointInterface);");
        pw.println("        }");
        pw.println("        String inputPortName = portName.getLocalPart();");
        pw.print("        ");
        for (int i = 0; i < getPortPortNames.size(); ++i) {
            String portName = (String) getPortPortNames.get(i);
            pw.println("if (\"" + portName + "\".equals(inputPortName)) {");
            pw.println("            return get" + portName + "();");
            pw.println("        }");
            pw.print("        else ");
        }
        pw.println(" {");
        /*
        pw.println("            java.rmi.Remote _stub = getPort(serviceEndpointInterface);");
        pw.println("            ((org.apache.axis.client.Stub) _stub).setPortName(portName);");
        pw.println("            return _stub;");
        */
        pw.println("        throw new " + javax.xml.rpc.ServiceException.class.getName() + "(\""
                + "Invalid QName" + "\");");
        pw.println("        }");
        pw.println("    }");
        pw.println();
    } // writeGetPortQNameClass

    /**
     * Write the getServiceName method.
     */
    protected void writeGetServiceName(PrintWriter pw, QName qname) {
        pw.println("    public javax.xml.namespace.QName getServiceName() {");
        pw.println("        return " + Utils.getNewQName(qname) + ";");
        pw.println("    }");
        pw.println();
    } // writeGetServiceName

    /**
     * Write the getPorts method.
     */
    protected void writeGetPorts(PrintWriter pw, Vector portNames) {
        pw.println("    private java.util.HashSet ports = null;");
        pw.println();
        pw.println("    public java.util.Iterator getPorts() {");
        pw.println("        if (ports == null) {");
        pw.println("            ports = new java.util.HashSet();");
        for (int i = 0; i < portNames.size(); ++i) {
            pw.println("            ports.add(new javax.xml.namespace.QName(\"" +
                    portNames.get(i) + "\"));");
        }
        pw.println("        }");
        pw.println("        return ports.iterator();");
        pw.println("    }");
        pw.println();
    } // writeGetPorts

}
