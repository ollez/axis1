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
 *    Apache Software Foundation (http://www.apache.org/)."
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

package org.apache.axis;

import org.apache.axis.deployment.DeploymentRegistry;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.deployment.wsdd.*;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SOAPTypeMappingRegistry;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.registries.HandlerRegistry;
import org.apache.axis.registries.SupplierRegistry;
import org.apache.axis.session.Session;
import org.apache.axis.session.SimpleSession;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.providers.java.MsgProvider;
import org.apache.log4j.Category;

import javax.xml.rpc.namespace.QName;
import java.util.Hashtable;

/**
 * An <code>AxisEngine</code> is the base class for AxisClient and
 * AxisServer.  Handles common functionality like dealing with the
 * handler/service registries and loading properties.
 *
 * @author Glen Daniels (gdaniels@macromedia.com)
 */
public abstract class AxisEngine extends BasicHandler
{
    static Category category =
            Category.getInstance(AxisEngine.class.getName());

    // Engine property names
    public static final String PROP_XML_DECL = "sendXMLDeclaration";
    public static final String PROP_DEBUG_LEVEL = "debugLevel";
    public static final String PROP_DEBUG_FILE = "debugFile";
    public static final String PROP_DOMULTIREFS = "sendMultiRefs";
    public static final String PROP_PASSWORD = "adminPassword";
    public static final String PROP_SYNC_CONFIG = "syncConfiguration";
    public static final String PROP_SEND_XSI = "sendXsiTypes";

    /** Our go-to guy for configuration... */
    protected ConfigurationProvider configProvider;

    protected DeploymentRegistry myRegistry =
            new SimpleWsddDeploymentManager();

    /** Has the user changed the password yet? */
    protected boolean _hasSafePassword = false;

    /** Should we save the engine config each time we modify it? */
    protected boolean shouldSaveConfig = false;

    //protected SupplierRegistry listenerRegistry = new SupplierRegistry();

    /**
     * This engine's Session.  This Session supports "application scope"
     * in the Apache SOAP sense... if you have a service with "application
     * scope", have it store things in this Session.
     */
    private Session session = new SimpleSession();

    /**
     * No-arg constructor.
     *
     */
    public AxisEngine()
    {
        // !!! Set up default configuration?
        init();

        category.debug(JavaUtils.getMessage("exit01", "AxisEngine"));
    }

    public AxisEngine(ConfigurationProvider configProvider)
    {
        this.configProvider = configProvider;
        init();
    }

    /**
     * (re)initialize - What should really go in here???
     */
    public void init() {
        category.debug(JavaUtils.getMessage("enter00", "AxisEngine::init"));

        getTypeMappingRegistry().setParent(SOAPTypeMappingRegistry.getSingleton());

        try {
            configProvider.configureEngine(this);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(JavaUtils.getMessage("problemDeploying00"));
        }
        
        category.debug(JavaUtils.getMessage("exit00", "AxisEngine::init"));
    }

    /**
     * Load up our engine's configuration of Handlers, Chains,
     * Services, etc.
     *
     * NOTE: Right now this can only read an "engine-config.xml" in an
     * appropriate place on the classpath (org/apache/axis/client or
     * org/apache/axis/server).  This should be modified to do something like
     * look in the server startup directory first, or perhaps check a
     * system property for the repository location.  (OK, now it checks the
     * local directory first.)
     *
     * We need to complete discussions about the packaging and deployment
     * patterns for Axis before this code solidifies.
     */
    /*
    private void readConfiguration()
    {
        InputStream is;
        try {
            is = new FileInputStream(engineBasePath + sep +
                                     engineConfigFilename);
        } catch (Exception e) {
            is = getResourceStream(engineConfigFilename);
        }

        if (is == null) {
            // TODO: Deal with this in a nicer way...
            System.err.println("No engine configuration in " +
                               this.getClass().getPackage().getName() +
                               " - aborting!");
            return;
        }

        Document doc = XMLUtils.newDocument(is);
        try {
            // ??? Clear registries first?
            Admin.processEngineConfig(doc, this);
        } catch (Exception e) {
            System.err.println("Couldn't read engine config!");
            e.printStackTrace();
            return;
        }
    }
    */

    /** Write out our engine configuration.
     */
    public void saveConfiguration()
    {
        if (!shouldSaveConfig)
            return;

        try {
            configProvider.writeEngineConfig(this);
        } catch (Exception e) {
            System.err.println(JavaUtils.getMessage("saveConfigFail00"));
            e.printStackTrace();
        }
    }

    public boolean hasSafePassword()
    {
        return _hasSafePassword;
    }

    public void setAdminPassword(String pw)
    {
        addOption(PROP_PASSWORD, pw);
        _hasSafePassword = true;
        saveConfiguration();
    }

    public void setShouldSaveConfig(boolean shouldSaveConfig)
    {
        this.shouldSaveConfig = shouldSaveConfig;
    }
    
    /**
     * (should throw more specific exceptions)
     */ 
    public Handler getHandler(String name) throws AxisFault
    {
        return myRegistry.getHandler(new QName(null, name));
    }
    
    /**
     * (should throw more specific exceptions)
     */ 
    public Handler getService(String name) throws AxisFault
    {
        return myRegistry.getService(new QName(null, name));
    }
    
    public Handler getTransport(String name) throws AxisFault
    {
        return myRegistry.getTransport(new QName(null, name));
    }

    public DeploymentRegistry getDeploymentRegistry()
    {
        return myRegistry;
    }
    
    public TypeMappingRegistry getTypeMappingRegistry()
    {
        TypeMappingRegistry tmr = null;
        try {
            tmr = myRegistry.getTypeMappingRegistry("");
            if (tmr == null) {
                tmr = new TypeMappingRegistry();
                myRegistry.addTypeMappingRegistry("", tmr);
            }
        } catch (DeploymentException e) {
            category.error(e);
        }
        
        return tmr;
    }

    /*********************************************************************
     * Client engine access
     *
     * An AxisEngine may define another specific AxisEngine to be used
     * by newly created Clients.  For instance, a server may
     * create an AxisClient and allow deployment to it.  Then
     * the server's services may access the AxisClient's deployed 
     * handlers and transports.
     *********************************************************************
     */

    public abstract AxisEngine getClientEngine ();

    /*********************************************************************
    * Administration and management APIs
    *
    * These can get called by various admin adapters, such as JMX MBeans,
    * our own Admin client, web applications, etc...
    *
    *********************************************************************
    */

    /**
     * Register a new global type mapping
     */
    public void registerTypeMapping(QName qName,
                                    Class cls,
                                    DeserializerFactory deserFactory,
                                    Serializer serializer)
    {
        category.info(JavaUtils.getMessage("registerTypeMap00",
                qName.toString(), cls.getName()));
        if (deserFactory != null)
            getTypeMappingRegistry().addDeserializerFactory(qName,
                                                        cls,
                                                        deserFactory);
        if (serializer != null)
            getTypeMappingRegistry().addSerializer(cls, qName, serializer);
    }

    /**
     * Unregister a global type mapping
     */
    public void unregisterTypeMapping(QName qName, Class cls)
    {
        getTypeMappingRegistry().removeDeserializer(qName);
        getTypeMappingRegistry().removeSerializer(cls);
    }

    /**
     * List of options which should be converted from Strings to Booleans
     * automatically.
     */ 
    static String [] booleanOptions = new String [] {
        PROP_DOMULTIREFS, PROP_SEND_XSI, PROP_XML_DECL
    };
    
    public void deployWSDD(WSDDDocument doc) throws DeploymentException
    {
        myRegistry.deploy(doc);
        WSDDGlobalConfiguration global = myRegistry.getGlobalConfiguration();
        if (global != null)
            setOptions(global.getParametersTable());
        
        // Convert boolean options to Booleans so we don't need to use
        // string comparisons.  Default is "true".
        
        for (int i = 0; i < booleanOptions.length; i++) {
            String val = (String)getOption(booleanOptions[i]);
            if (val != null && val.equalsIgnoreCase("false")) {
                addOption(booleanOptions[i], Boolean.FALSE);
            } else {
                addOption(booleanOptions[i], Boolean.TRUE);
            }
        }
        
        // Deal with admin password's default value.
        if (getOption(PROP_PASSWORD) == null) {
            addOption(PROP_PASSWORD, "admin");
        } else {
            setAdminPassword((String)getOption(PROP_PASSWORD));
        }
    }
    
    /**
     * Deploy a Handler into our handler registry
     */
    public void deployHandler(String key, Handler handler)
        throws DeploymentException
    {
        handler.setName(key);
        WSDDDocument doc = (WSDDDocument)myRegistry.getConfigDocument();
        WSDDHandler newHandler = new WSDDHandler();
        newHandler.setName(key);
        newHandler.setType(new QName(WSDDConstants.WSDD_JAVA,
                                     handler.getClass().getName()));
        myRegistry.deployHandler(newHandler);
    }

    /**
     * Undeploy (remove) a Handler from the handler registry
     */
    public void undeployHandler(String key)
        throws DeploymentException
    {
        myRegistry.removeDeployedItem(new QName("", key));
    }

    /**
     * Deploy a Service into our service registry
     */
    public void deployService(String key, SOAPService service)
        throws DeploymentException
    {
        category.info(JavaUtils.getMessage("deployService00", key, this.toString()));
        service.setName(key);
        service.setEngine(this);
        
        WSDDService newService = new WSDDService();
        newService.setName(key);
        newService.setOptionsHashtable(service.getOptions());
        newService.setCachedService(service);
        
        Handler pivot = service.getPivotHandler();
        if (pivot instanceof RPCProvider) {
            newService.setProviderQName(WSDDConstants.JAVARPC_PROVIDER);
        } else if (pivot instanceof MsgProvider) {
            newService.setProviderQName(WSDDConstants.JAVAMSG_PROVIDER);
        } else {
            newService.setParameter("handlerClass", pivot.getClass().getName());
            newService.setProviderQName(WSDDConstants.HANDLER_PROVIDER);
        }
//        WSDDProvider provider = newService.createProvider(WSDDJavaRPCProvider.class);
//        provider.setAttribute("type", "java:" + service.getPivotHandler().getClass().getName());
//        provider.setProviderAttribute("className", (String)service.getOption("className"));
        myRegistry.deployService(newService);
    }

    /**
     * Undeploy (remove) a Service from the handler registry
     */
    public void undeployService(String key)
        throws DeploymentException
    {
        myRegistry.undeployService(new QName("", key));
    }

    /**
     * Deploy a Transport
     */
    public void deployTransport(String key, SimpleTargetedChain transport)
        throws DeploymentException
    {
        transport.setName(key);
        WSDDTransport wt = new WSDDTransport();
        wt.setName(key);
        wt.setOptionsHashtable(transport.getOptions());
        // !!! Request flow?
        // !!! Response flow?
        wt.setPivotQName(new QName(WSDDConstants.WSDD_JAVA,
                                   transport.getPivotHandler().getClass().getName()));
        
        myRegistry.deployTransport(wt);
    }

    /**
     * Undeploy (remove) a client Transport
     */
    public void undeployTransport(String key)
        throws DeploymentException
    {
        myRegistry.undeployTransport(new QName("", key));
    }

    /**
     * accessor only, for application session
     * (could call it "engine session" instead, but named with reference
     * to Apache SOAP's notion of "application scope")
     */
    public Session getApplicationSession () {
        return session;
    }

};
