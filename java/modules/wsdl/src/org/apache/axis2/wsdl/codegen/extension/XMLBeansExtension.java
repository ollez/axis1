package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.net.URLClassLoader;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*
*/

public class XMLBeansExtension extends AbstractCodeGenerationExtension {
    private static final String DEFAULT_STS_NAME = "foo";


    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {
        //test whether the TCCL has the Xbeans classes
        //ClassLoader cl = Thread.currentThread().getContextClassLoader();


        try {
            WSDLTypes typesList = configuration.getWom().getTypes();
            if (typesList == null) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                this.configuration.setTypeMapper(new DefaultTypeMapper());
                return;
            }

            List typesArray = typesList.getExtensibilityElements();
            WSDLExtensibilityElement extensiblityElt = null;

            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);
                Vector xmlObjectsVector = new Vector();
                Schema schema = null;
                SchemaTypeSystem sts  = null;

                if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {
                    schema = (Schema) extensiblityElt;
                    XmlOptions options = new XmlOptions();
                    options.setLoadAdditionalNamespaces(
                            configuration.getWom().getNamespaces()); //add the namespaces

                    Stack importedSchemaStack = schema.getImportedSchemaStack();
                    //compile these schemas
                    while (!importedSchemaStack.isEmpty()){
                        xmlObjectsVector.add(
                                XmlObject.Factory.parse(
                                        ((javax.wsdl.extensions.schema.Schema)importedSchemaStack.pop()).getElement()
                                        ,options));
                    }
                }

                sts = XmlBeans.compileXmlBeans(DEFAULT_STS_NAME, null,
                        convertToXMLObjectArray(xmlObjectsVector),
                        new BindingConfig(), XmlBeans.getContextTypeLoader(),
                        new Axis2Filer(),
                        null);

                //create the type mapper
                JavaTypeMapper mapper = new JavaTypeMapper();
                SchemaType[] types = sts.documentTypes();
                int length = types.length;
                for (int j = 0; j < length; j++) {
                    mapper.addTypeMapping(types[j].getDocumentElementName(),
                            types[j].getFullJavaName());
                }
                //set the type mapper to the config
                configuration.setTypeMapper(mapper);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private XmlObject[] convertToXMLObjectArray(Vector vec){
        XmlObject[] xmlObjects = new XmlObject[vec.size()];
        for (int i = 0; i < vec.size(); i++) {
            xmlObjects[i] = (XmlObject)vec.get(i);
        }
        return xmlObjects;
    }
    /**
     * Private class to generate the filer
     */
    private class Axis2Filer implements Filer{

        public OutputStream createBinaryFile(String typename)
                throws IOException {
            File file = new File(configuration.getOutputLocation(), typename);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileOutputStream(file);
        }

        public Writer createSourceFile(String typename)
                throws IOException {
            typename =
                    typename.replace('.', File.separatorChar);
            File file = new File(configuration.getOutputLocation(),
                    typename + ".java");
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileWriter(file);
        }
    }
}
