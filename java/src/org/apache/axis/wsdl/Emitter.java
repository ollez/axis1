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

import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Document;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.QName;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This class produces java files for stubs, skeletons, and types from a
 * WSDL document.
 *
 * @author Russell Butek (butek@us.ibm.com)
 * @author Tom Jordahl (tjordahl@macromedia.com)
 * @author Steve Graham (sggraham@us.ibm.com)
 */
public class Emitter {
    // Scope constants
    public static final byte NO_EXPLICIT_SCOPE = 0x00;
    public static final byte APPLICATION_SCOPE = 0x01;
    public static final byte REQUEST_SCOPE     = 0x10;
    public static final byte SESSION_SCOPE     = 0x11;

    protected Document doc = null;
    protected Definition def = null;
    protected WsdlAttributes wsdlAttr = null;
    protected boolean bEmitSkeleton = false;
    protected boolean bMessageContext = false;
    protected boolean bEmitTestCase = false;
    protected boolean bVerbose = false;
    protected boolean bGenerateImports = true;
    protected String outputDir = null;
    protected byte scope = NO_EXPLICIT_SCOPE;
    protected ArrayList classList = new ArrayList();
    protected ArrayList fileList = new ArrayList();
    protected Namespaces namespaces = null;
    protected HashMap delaySetMap = null;
    protected TypeFactory emitFactory = null;
    protected WriterFactory writerFactory = null;

    // portTypesInfo is a Hashmap of <PortType, HashMap2> pairs where HashMap2 is a
    // Hashmap of <Operation, Parameters> pairs.
    protected HashMap portTypesInfo = null;

    /**
     * Default constructor.
     */
    public Emitter(WriterFactory writerFactory) {
        this.writerFactory = writerFactory;
        portTypesInfo = new HashMap();
    } // ctor

    /**
     * Construct an Emitter that initially looks like the given Emitter.
     */
    public Emitter(Emitter that) {
        this.bEmitSkeleton        = that.bEmitSkeleton;
        this.bMessageContext      = that.bMessageContext;
        this.bEmitTestCase        = that.bEmitTestCase;
        this.bVerbose             = that.bVerbose;
        this.bGenerateImports     = that.bGenerateImports;
        this.outputDir            = that.outputDir;
        this.scope                = that.scope;
        this.namespaces           = that.namespaces;
        this.emitFactory          = that.emitFactory;
        this.portTypesInfo        = that.portTypesInfo;
        this.writerFactory        = that.writerFactory;
    } // ctor

    /**
     * Call this method if you have a uri for the WSDL document
     */
    public void emit(String uri) throws IOException {
        if (bVerbose)
            System.out.println(JavaUtils.getMessage("parsing00", uri));

        try {
            emit(XMLUtils.newDocument(uri));
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.out.println(
                    JavaUtils.getMessage("parseError00", t.getMessage()));
        }
    } // emit

    /**
     * Call this method if your WSDL document has already been parsed as an XML DOM document.
     */
    public void emit(Document doc) throws IOException {
        try {
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setVerbose(bVerbose);
            def = reader.readWSDL(null, doc);
            namespaces = new Namespaces(outputDir);

            // First, read the namespace mapping file - NStoPkg.properties - if it
            // exists, and load the namespaceMap HashMap with its data.
            getNStoPkgFromPropsFile(namespaces);

            if (delaySetMap != null) {
                namespaces.putAll(delaySetMap);
            }
            emitFactory = new TypeFactory(namespaces);
            emit(def, doc);

            // Output deploy.xml and undeploy.xml outside of the recursive emit method.
            if (bEmitSkeleton) {
                Writer writer = writerFactory.getWriter(def);
                writer.write();
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.out.println(
                    JavaUtils.getMessage("parseError00", t.getMessage()));
        }
    } // emit

    private void emit(Definition def, Document doc) throws IOException {
        this.def = def;
        this.doc = doc;

        // Generate types from doc
        if (doc != null) {
            emitFactory.buildTypes(doc);
            if (bVerbose) {
                System.out.println(JavaUtils.getMessage("types00"));
                emitFactory.dump();
            }
            // Output Java classes for types
            writeTypes();
        }


        if (def != null) {
            // Generated all the imported XML
            if (bGenerateImports) {

                // Generate the imported WSDL documents
                Map imports = def.getImports();
                Object[] importKeys = imports.keySet().toArray();
                for (int i = 0; i < importKeys.length; ++i) {
                    Vector v = (Vector) imports.get(importKeys[i]);
                    for (int j = 0; j < v.size(); ++j) {
                        Import imp = (Import) v.get(j);
                        Emitter emitter = new Emitter(this);
                        writerFactory.setEmitter(emitter);
                        emitter.emit(imp.getDefinition(),
                                     XMLUtils.newDocument(imp.getLocationURI()));
                        writerFactory.setEmitter(this);
                    }
                }
            }

            // Collect information about ports and operations
            wsdlAttr = new WsdlAttributes(def, new HashMap());

            firstPass();

            // Output interfaces for portTypes
            writePortTypes();

            // Output factory classes for services
            writeServices();

            // Output Stub classes for bindings
            writeBindings();
        }
    } // emit

    /**
     * Look for a NStoPkg.properties file in the CLASSPATH.  If it exists,
     * then collect the namespace->package mappings from it.
     */
    private static void getNStoPkgFromPropsFile(HashMap namespaces)
    {
        try {
            ResourceBundle mappings = ResourceBundle.getBundle("NStoPkg");
            Enumeration keys = mappings.getKeys();
            while (keys.hasMoreElements()) {
                try {
                    String key = (String) keys.nextElement();
                    namespaces.put(key, mappings.getString(key));
                }
                catch (Throwable t) {
                }
            }
        }
        catch (Throwable t) {
        }
    } // getNStoPkgFromPropsFile

    /**
     * Do some cleanup of the 'symbol table' and add our own symbol table structures
     */
    protected void firstPass() throws IOException {

        // PortTypes and Services can share the same name.  If they do in this Definition,
        // force their names to be suffixed with _PortType and _Service, respectively.
        resolvePortTypeServiceNameClashes();

        // portTypesInfo is a Hashmap of <PortType, HashMap2> pairs where HashMap2 is a
        // Hashmap of <Operation, Parameters> pairs.  
        createPortTypesInfo();
    } // firstPass

    // portTypesInfo is a Hashmap of <PortType, HashMap2> pairs where HashMap2 is a HashMap of
    // <Operation, Parameters> pairs.  Walk through the symbol table and create this HashMap of
    // a HashMap of Parameters.
    private void createPortTypesInfo() throws IOException {
        Iterator i = def.getPortTypes().values().iterator();
        while (i.hasNext()) {
            PortType portType = (PortType) i.next();

            // If the portType is undefined, then we're parsing a Definition
            // that didn't contain a portType, merely a binding that referred
            // to a non-existent port type.  Don't bother with it.
            if (!portType.isUndefined()) {
                HashMap portTypeInfo = new HashMap();

            // Remove Duplicates - happens with only a few WSDL's. No idea why!!! 
            // (like http://www.xmethods.net/tmodels/InteropTest.wsdl) 
            // TODO: Remove this patch...
            // NOTE from RJB:  this is a WSDL4J bug and the WSDL4J guys have been notified.
                Iterator operations = (new HashSet(portType.getOperations())).iterator();
                while(operations.hasNext()) {
                    Operation operation = (Operation) operations.next();
                    String namespace = portType.getQName().getNamespaceURI();
                    Parameters parms = parameters(operation, namespace);
                    portTypeInfo.put(operation.getName(), parms);
                }
                portTypesInfo.put(portType.getQName(), portTypeInfo);
            }
        }
    } // createPortTypesInfo

    ///////////////////////////////////////////////////
    //
    // Command line switches
    //

    /**
     * Turn on/off server skeleton creation
     * @param boolean value
     */
    public void generateSkeleton(boolean value) {
        this.bEmitSkeleton = value;
    }

    /**
     * Turn on/off test case creation
     * @param boolean value
     */
    public void generateTestCase(boolean value) {
        this.bEmitTestCase = value;
    }

    /**
     * Turn on/off server Message Context parm creation in skel
     * @param boolean value
     */
    public void generateMessageContext(boolean value) {
        this.bMessageContext = value;
    }

    /**
     * Turn on/off generation of elements from imported files.
     * @param boolean generateImports
     */
    public void generateImports(boolean generateImports) {
        this.bGenerateImports = generateImports;
    } // generateImports

    /**
     * Turn on/off verbose messages
     * @param boolean value
     */
    public void verbose(boolean value) {
        this.bVerbose = value;
    }

    public void setNamespaceMap(HashMap map) {
        delaySetMap = map;
    }


    /**
     * Set the output directory to use in emitted source files
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Set the scope for the deploy.xml file.
     * @param scope One of Emitter.NO_EXPLICIT_SCOPE, Emitter.APPLICATION_SCOPE, Emitter.REQUEST_SCOPE, Emitter.SESSION_SCOPE.  Anything else is equivalent to NO_EXPLICIT_SCOPE and no explicit scope tag will appear in deploy.xml.
     */
    public void setScope(byte scope) {
        this.scope = scope;
    } // setScope

    /**
     * Get the scope for the deploy.xml file.
     */
    public byte getScope() {
        return scope;
    } // getScope

    ///////////////////////////////////////////////////
    //
    // Implementation
    //

    /**
     * This method returns a list of all generated class names.
     */
    public List getGeneratedClassNames() {
        return this.classList;
    }

    /**
     * This method returns a list of all generated file names.
     */
    public List getGeneratedFileNames() {
        return this.fileList;
    }

    /**
     * PortTypes and Services can share the same name.  If they do in this Definition,
     * force their names to be suffixed with _PortType and _Service, respectively.  These names
     * are placed back in the QName of the PortType and Service objects themselves.
     */
    private void resolvePortTypeServiceNameClashes() {
        Map portTypes = def.getPortTypes();
        Map services  = def.getServices();
        Iterator pti = portTypes.keySet().iterator();
        while (pti.hasNext()) {
            QName ptName = (QName) pti.next();
            Iterator si = services.keySet().iterator();
            while (si.hasNext()) {
                QName sName = (QName) si.next();
                if (ptName.equals(sName)) {
                    ptName.setLocalPart(ptName.getLocalPart() + "_Port");
                    sName.setLocalPart(sName.getLocalPart() + "_Service");
                }
            }
        }
    } // resolvePortTypeServiceNameClashes

    /**
     * Generate the bindings for all port types.
     */
    protected void writePortTypes() throws IOException {
        Map portTypes = def.getPortTypes();
        Iterator i = portTypes.values().iterator();

        while (i.hasNext()) {
            PortType portType = (PortType) i.next();

            // If the portType is undefined, then we're parsing a Definition
            // that didn't contain a portType, merely a binding that referred
            // to a non-existent port type.  Don't bother writing it.
            if (!portType.isUndefined()) {
                HashMap operationParameters = (HashMap) portTypesInfo.get(portType.getQName());
                Writer writer = writerFactory.getWriter(portType, operationParameters);
                writer.write();
            }
        }
    } // writePortTypes

    /**
     * This class simply collects
     */
    protected static class Parameter {

        // constant values for the parameter mode.
        public static final byte IN = 1;
        public static final byte OUT = 2;
        public static final byte INOUT = 3;

        public String name;
        public String type;
        public byte mode = IN;

        public String toString() {
            return "(" + type + ", " + name + ", "
                    + (mode == IN ? "IN)" : mode == INOUT ? "INOUT)" : "OUT)");
        } // toString
    } // class Parameter


    /**
     * For the given operation, this method returns the parameter info conveniently collated.
     * There is a bit of processing that is needed to write the interface, stub, and skeleton.
     * Rather than do that processing 3 times, it is done once, here, and stored in the
     * Parameters object.
     */
    protected Parameters parameters(Operation operation, String namespace) throws IOException {
        Parameters parameters = new Parameters();

        // The input and output Vectors, when filled in, will be of the form:
        // {<parmType0>, <parmName0>, <parmType1>, <parmName1>, ..., <parmTypeN>, <parmNameN>}
        Vector inputs = new Vector();
        Vector outputs = new Vector();

        List parameterOrder = operation.getParameterOrdering();

        // Handle parameterOrder="", which is techinically illegal
        if (parameterOrder != null && parameterOrder.isEmpty()) {
            parameterOrder = null;
        }

        // All input parts MUST be in the parameterOrder list.  It is an error otherwise.
        if (parameterOrder != null) {
            Input input = operation.getInput();
            if (input != null) {
                Message inputMsg = input.getMessage();
                Map allInputs = inputMsg.getParts();
                Collection orderedInputs = inputMsg.getOrderedParts(parameterOrder);
                if (allInputs.size() != orderedInputs.size()) {
                    throw new IOException(JavaUtils.getMessage("emitFail00", operation.getName()));
                }
            }
        }

        // Collect all the input parameters
        Input input = operation.getInput();
        if (input != null) {
            partStrings(inputs,
                    input.getMessage().getOrderedParts(null),
                    (wsdlAttr.getInputBodyType(operation) == WsdlAttributes.USE_LITERAL));
        }

        // Collect all the output parameters
        Output output = operation.getOutput();
        if (output != null) {
            partStrings(outputs,
                    output.getMessage().getOrderedParts(null),
                    (wsdlAttr.getOutputBodyType(operation) == WsdlAttributes.USE_LITERAL));
        }

        if (parameterOrder != null) {
            // Construct a list of the parameters in the parameterOrder list, determining the
            // mode of each parameter and preserving the parameterOrder list.
            for (int i = 0; i < parameterOrder.size(); ++i) {
                String name = (String) parameterOrder.get(i);

                // index in the inputs Vector of the given name, -1 if it doesn't exist.
                int index = getPartIndex(name, inputs);

                // index in the outputs Vector of the given name, -1 if it doesn't exist.
                int outdex = getPartIndex(name, outputs);

                if (index > 0) {
                    // The mode of this parameter is either in or inout
                    addInishParm(inputs, outputs, index, outdex, parameters, true);
                }
                else if (outdex > 0) {
                    addOutParm(outputs, outdex, parameters, true);
                }
                else {
                    System.err.println(JavaUtils.getMessage("noPart00", name));
                }
            }
        }

        // Get the mode info about those parts that aren't in the parameterOrder list.
        // Since they're not in the parameterOrder list, the order doesn't matter.
        // Add the input and inout parts first, then add the output parts.
        for (int i = 1; i < inputs.size(); i += 2) {
            int outdex = getPartIndex((String) inputs.get(i), outputs);
            addInishParm(inputs, outputs, i, outdex, parameters, false);
        }

        // Now that the remaining in and inout parameters are collected, the first entry in the
        // outputs Vector is the return value.  The rest are out parameters.
        if (outputs.size() > 0) {
            parameters.returnType = (String) outputs.get(0);
            parameters.returnName = (String) outputs.get(1);
            ++parameters.outputs;
            for (int i = 3; i < outputs.size(); i += 2) {
                addOutParm(outputs, i, parameters, false);
            }
        }

        // Collect the list of faults into a single string, separated by commas.
        Map faults = operation.getFaults();
        Iterator i = faults.values().iterator();
        while (i.hasNext()) {
            Fault fault = (Fault) i.next();
            String exceptionName = Utils.capitalizeFirstChar(Utils.xmlNameToJava((String) fault.getName()));
            if (parameters.faultString == null)
                parameters.faultString = exceptionName;
            else
                parameters.faultString = parameters.faultString + ", " + exceptionName;
        }

        if (parameters.returnType == null)
            parameters.returnType = "void";
        constructSignatures(parameters, operation.getName());
        return parameters;
    } // parameters

    /**
     * Return the index of the given name in the given Vector, -1 if it doesn't exist.
     */
    private int getPartIndex(String name, Vector v) {
        for (int i = 1; i < v.size(); i += 2) {
            if (name.equals(v.get(i))) {
                return i;
            }
        }
        return -1;
    } // getPartIndex

    /**
     * Add an in or inout parameter to the parameters object.
     */
    private void addInishParm(Vector inputs, Vector outputs, int index, int outdex, Parameters parameters, boolean trimInput) {
        Parameter p = new Parameter();
        p.name = (String) inputs.get(index);
        p.type = (String) inputs.get(index - 1);

        // Should we remove the given parameter type/name entries from the Vector?
        if (trimInput) {
            inputs.remove(index);
            inputs.remove(index - 1);
        }

        // At this point we know the name and type of the parameter, and that it's at least an
        // in parameter.  Now check to see whether it's also in the outputs Vector.  If it is,
        // then it's an inout parameter.
        if (outdex > 0 && p.type.equals(outputs.get(outdex - 1))) {
            outputs.remove(outdex);
            outputs.remove(outdex - 1);
            p.mode = Parameter.INOUT;
            ++parameters.inouts;
        }
        else {
            ++parameters.inputs;
        }
        parameters.list.add(p);
    } // addInishParm

    /**
     * Add an output parameter to the parameters object.
     */
    private void addOutParm(Vector outputs, int outdex, Parameters parameters, boolean trim) {
        Parameter p = new Parameter();
        p.name = (String) outputs.get(outdex);
        p.type = (String) outputs.get(outdex - 1);
        if (trim) {
            outputs.remove(outdex);
            outputs.remove(outdex - 1);
        }
        p.mode = Parameter.OUT;
        ++parameters.outputs;
        parameters.list.add(p);
    } // addOutParm

    /**
     * Construct the signatures.  signature is used by both the interface and the stub.
     * skelSig is used by the skeleton.
     */
    private void constructSignatures(Parameters parms, String name) {
        int allOuts = parms.outputs + parms.inouts;
        String signature = "    public " + parms.returnType + " " + name + "(";
        String axisSig = "    public " + parms.returnType + " " + name + "(";
        String skelSig = null;

        if (allOuts == 0)
            skelSig = "    public void " + name + "(";
        else
            skelSig = "    public Object " + name + "(";

        if (bMessageContext) {
            skelSig = skelSig + "org.apache.axis.MessageContext ctx";
            axisSig = axisSig + "org.apache.axis.MessageContext ctx";
            if ((parms.inputs + parms.inouts) > 0) {
                skelSig = skelSig + ", ";
            }
            if (parms.list.size() > 0) {
                axisSig = axisSig + ", ";
            }
        }
        boolean needComma = false;

        for (int i = 0; i < parms.list.size(); ++i) {
            Parameter p = (Parameter) parms.list.get(i);

            if (needComma) {
                signature = signature + ", ";
                axisSig = axisSig + ", ";
                if (p.mode != Parameter.OUT)
                    skelSig = skelSig + ", ";
            }
            else
                needComma = true;
            if (p.mode == Parameter.IN) {
                signature = signature + p.type + " " + p.name;
                axisSig = axisSig + p.type + " " + p.name;
                skelSig = skelSig + p.type + " " + p.name;
            }
            else if (p.mode == Parameter.INOUT) {
                signature = signature + Utils.holder(p.type) + " " + p.name;
                axisSig = axisSig + Utils.holder(p.type) + " " + p.name;
                skelSig = skelSig + p.type + " " + p.name;
            }
            else// (p.mode == Parameter.OUT)
            {
                signature = signature + Utils.holder(p.type) + " " + p.name;
                axisSig = axisSig + Utils.holder(p.type) + " " + p.name;
            }
        }
        signature = signature + ") throws java.rmi.RemoteException";
        axisSig = axisSig + ") throws java.rmi.RemoteException";
        skelSig = skelSig + ") throws java.rmi.RemoteException";
        if (parms.faultString != null) {
            signature = signature + ", " + parms.faultString;
            axisSig = axisSig + ", " + parms.faultString;
            skelSig = skelSig + ", " + parms.faultString;
        }
        parms.signature = signature;
        parms.axisSignature = axisSig;
        parms.skelSignature = skelSig;
    } // constructSignatures

    /**
     * This method returns a vector containing the Java types (even indices) and
     * names (odd indices) of the parts.
     */
    protected void partStrings(Vector v, Collection parts, boolean literal) {
        Iterator i = parts.iterator();

        while (i.hasNext()) {
            Part part = (Part) i.next();
            QName elementName = part.getElementName();
            QName typeName = part.getTypeName();
            if (literal) {
                if (elementName != null) {
                    v.add(Utils.capitalizeFirstChar(elementName.getLocalPart()));
                    v.add(part.getName());
                }
            } else {
                // Encoded
                if (typeName != null) {
                    // Handle the special "java" namespace for types
                    if (typeName.getNamespaceURI().equalsIgnoreCase("java")) {
                        v.add(typeName.getLocalPart());
                    } else {
                        v.add(emitFactory.getType(typeName).getJavaName());
                    }
                    v.add(part.getName());
                } else if (elementName != null) {
                    // Handle the special "java" namespace for types
                    if (elementName.getNamespaceURI().equalsIgnoreCase("java")) {
                        v.add(elementName.getLocalPart());
                    } else {
                        v.add(emitFactory.getType(elementName).getJavaName());
                    }
                    v.add(part.getName());
                }
            }
        }
    } // partStrings

    /**
     * Generate the stubs and skeletons for all binding tags.
     */
    protected void writeBindings() throws IOException {
        Map bindings = def.getBindings();
        Iterator i = bindings.values().iterator();

        while (i.hasNext()) {
            Binding binding = (Binding) i.next();

            // If the binding is undefined, then we're parsing a Definition
            // that didn't contain a binding, merely a service that referred
            // to a non-existent binding.  Don't bother writing it.
            if (!binding.isUndefined()) {

            // If this isn't a SOAP binding, skip it
                if (wsdlAttr.getBindingType(binding) != WsdlAttributes.TYPE_SOAP) {
                    continue;
                }

                HashMap operationParameters = (HashMap) portTypesInfo.get(binding.getPortType().getQName());
                Writer writer = writerFactory.getWriter(binding, operationParameters);
                writer.write();
            }
        }
    } // writeBindings

    /**
     * Create the service class or classes
     */
    protected void writeServices() throws IOException {
        Map services = def.getServices();
        Iterator i = services.values().iterator();

        while (i.hasNext()) {
            Service service = (Service) i.next();
            Writer writer = writerFactory.getWriter(service, portTypesInfo);
            writer.write();
       }
    }

    //////////////////////////////
    //
    // Methods using types (non WSDL)
    //

    /**
     * Generate bindings (classes and class holders) for the complex types.
     * If generating serverside (skeleton) spit out beanmappings
     */
    protected void writeTypes() throws IOException {
        HashMap types = emitFactory.getTypes();
        Iterator i = types.values().iterator();
        while (i.hasNext()) {
            Type type = (Type) i.next();
            if (type.isDefined() && type.getShouldEmit() && type.getBaseType() == null) {
                Writer writer = writerFactory.getWriter(type);
                writer.write();
            }
        }
    } // writeTypes

    //
    // Methods using types (non WSDL)
    //
    //////////////////////////////

    ///////////////////////////////////////////////////
    //
    // Utility methods
    //

    public Namespaces getNamespaces() {
        return namespaces;
    } // getNamespaces

    public TypeFactory getTypeFactory() {
        return emitFactory;
    } // getTypeFactory
}
