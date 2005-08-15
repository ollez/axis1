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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.codegen.emitter.CSharpEmitter;
import org.apache.axis2.wsdl.codegen.emitter.Emitter;
import org.apache.axis2.wsdl.codegen.emitter.JavaEmitter;
import org.apache.axis2.wsdl.codegen.extension.*;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.WSDLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chathura@opensource.lk
 */
public class CodeGenerationEngine {
    private List moduleEndpoints = new ArrayList();


    private CodeGenConfiguration configuration;

    public CodeGenerationEngine(CodeGenConfiguration config) throws CodeGenerationException{
       this.configuration = config;
       loadExtensions();
    }
    
    public CodeGenerationEngine(CommandLineOptionParser parser) throws CodeGenerationException {
        WSDLDescription wom;
        try {
            wom = this.getWOM(parser);
        } catch (WSDLException e) {
            throw new CodeGenerationException("Error parsing WSDL", e);
        } catch (IOException e1) {
            throw new CodeGenerationException("Invalid WSDL Location ", e1);
        }

        this.configuration = new CodeGenConfiguration(wom, parser);
        loadExtensions();
    }

    private void loadExtensions() {
        //Ideally these extensions should be loaded through a configuration taken
        //from some external location. Say a config file.
        addExtension(new AxisBindingBuilder());
        addExtension(new WSDLValidatorExtension());
        addExtension(new PackageFinder());
        //Xbeans extension
        addExtension(new XMLBeansExtension());
        //simple databinding extension
        //addExtension(new SimpleDBExtension());
        //default extension. Does the cleanup
        addExtension(new DefaultDatabindingExtension());

    }

    private void addExtension(AbstractCodeGenerationExtension ext){
        ext.init(this.configuration);
        this.moduleEndpoints.add(ext);
    }
    public void generate() throws CodeGenerationException {
        try {
            for (int i = 0; i < this.moduleEndpoints.size(); i++) {
                ((CodeGenExtension) this.moduleEndpoints.get(i)).engage();
            }

            Emitter emitter = null;
            TypeMapper mapper = configuration.getTypeMapper();

            switch (configuration.getOutputLanguage()) {
                case XSLTConstants.LanguageTypes.JAVA:
                    emitter = new JavaEmitter(this.configuration, mapper);
                    break;
                case XSLTConstants.LanguageTypes.C_SHARP:
                    emitter = new CSharpEmitter(this.configuration, mapper);
                    break;
                case XSLTConstants.LanguageTypes.C_PLUS_PLUS:
                case XSLTConstants.LanguageTypes.VB_DOT_NET:

                default:
                    throw new UnsupportedOperationException();

            }
            if (this.configuration.isServerSide())
                emitter.emitSkeleton();
            else
                emitter.emitStub();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }


    }


    private WSDLDescription getWOM(CommandLineOptionParser parser) throws WSDLException,
            IOException {
        String uri = ((CommandLineOption) parser.getAllOptions().get(
                CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION)).getOptionValue();
        return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(uri)
                .getDescription();
    }


}
