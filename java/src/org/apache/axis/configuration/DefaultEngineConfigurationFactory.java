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

package org.apache.axis.configuration;

import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.Constants;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.InputStream;

/**
 * This is a default implementation of EngineConfigurationFactory.
 * It is user-overrideable by a system property without affecting
 * the caller. If you decide to override it, use delegation if
 * you want to inherit the behaviour of this class as using
 * class extension will result in tight loops. That is, your
 * class should implement EngineConfigurationFactory and keep
 * an instance of this class in a member field and delegate
 * methods to that instance when the default behaviour is
 * required.
 *
 * @author Glyn Normington (glyn@apache.org)
 */
public class DefaultEngineConfigurationFactory implements EngineConfigurationFactory {
    private static final String CLIENT_CONFIG_FILE = "client-config.wsdd";
    private static final String SERVER_CONFIG_FILE = "server-config.wsdd";

    private EngineConfigurationFactory userFactory = null;

    private String clientConfigFile;

    private String serverConfigFile;

    /**
     * Create the default engine configuration and detect whether the user
     * has overridden this with their own.
     */
    public DefaultEngineConfigurationFactory() {
        String fClassName = System.getProperty("axis.EngineConfigFactory");

        if (fClassName != null) {
            try {
                userFactory = (EngineConfigurationFactory)Class.
                    forName(fClassName).newInstance();
            } catch (Exception e) {
                // Report diagnostics but use the default factory.
                e.printStackTrace(System.err);
            }
        }

        clientConfigFile = System.getProperty("axis.ClientConfigFile");
        if (clientConfigFile == null) {
            clientConfigFile = CLIENT_CONFIG_FILE;
        }

        serverConfigFile = System.getProperty("axis.ServerConfigFile");
        if (serverConfigFile == null) {
            serverConfigFile = SERVER_CONFIG_FILE;
        }
        
    }

     /**
     * Get a default client engine configuration.
     *
     * @return a client EngineConfiguration
     */
    public EngineConfiguration getClientEngineConfig() {
        if (userFactory == null) {
            return new FileProvider(clientConfigFile);
        } else {
            return userFactory.getClientEngineConfig();
        }
    }

    /**
     * Get a default server engine configuration.
     *
     * @return a server EngineConfiguration
     */
    public EngineConfiguration getServerEngineConfig() {
        if (userFactory == null) {
            return new FileProvider(serverConfigFile);
        } else {
            return userFactory.getServerEngineConfig();
        }
    }

    /**
     * Get a default server engine configuration in a servlet environment.
     *
     * @param ctx a ServletContext
     * @return a server EngineConfiguration
     */
    public EngineConfiguration getServerEngineConfig(ServletContext ctx) {
        if (userFactory == null) {
            /*
             * Use the WEB-INF directory (so the config files can't get
             * snooped by a browser)
             */
            String webInfPath = ctx.getRealPath("/WEB-INF");
 
            FileProvider config = null ;

            if (!(new File(webInfPath,
                           SERVER_CONFIG_FILE)).exists()){
                InputStream is = null ;
                is = ctx.getResourceAsStream("/WEB-INF/"+
                                                 SERVER_CONFIG_FILE);
                if (is != null) config = new FileProvider(is);
            }
            if ( config == null )
                config = new FileProvider(webInfPath,
                                          SERVER_CONFIG_FILE);
            return config;
        } else {
            return userFactory.getServerEngineConfig(ctx);
        }
    }
}
