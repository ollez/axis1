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
package org.apache.axis.deployment;

import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.InternalException;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.providers.java.MsgProvider;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDHandler;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.deployment.wsdd.WSDDTransport;
import org.apache.axis.deployment.wsdd.WSDDTypeMapping;
import org.apache.axis.deployment.wsdd.WSDDRequestFlow;
import org.apache.axis.deployment.wsdd.WSDDResponseFlow;
import org.apache.axis.encoding.SOAPTypeMappingRegistry;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.utils.JavaUtils;
import org.apache.log4j.Category;
import org.w3c.dom.Document;

import javax.xml.rpc.namespace.QName;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.IOException;


/**
 * This is a simple implementation of the DeploymentRegistry class
 *
 * @author James Snell
 * @author Glen Daniels (gdaniels@macromedia.com)
 */
public class SimpleDeploymentManager
    extends DeploymentRegistry
{
    static Category category =
            Category.getInstance(SimpleDeploymentManager.class.getName());

    /** Our global configuration */
    WSDDGlobalConfiguration globalConfig;

    /** Storage for our handlers */
    Hashtable handlers = new Hashtable();
    Hashtable services = new Hashtable();
    Hashtable transports = new Hashtable();

    /** Storage for our TypeMappingRegistries */
    Hashtable mappings;
    
    /** The deployment document we're rooted off of.
     * This will be updated as new items are deployed into the registry.
     */
    DeploymentDocument doc = null;

    /**
     * Constructor - sets up a default encoding style
     */
    public SimpleDeploymentManager()
    {
        mappings = new Hashtable();

        try {
            mappings.put(Constants.URI_SOAP_ENC, new SOAPTypeMappingRegistry());
        } catch (Exception e) {
            // if this ever occurs, we have an internal error
            throw new InternalException(e);
        }
    }

    /**
     * Deploy a deployment document into this registry.
     * 
     * @param deployment the DeploymentDocument we'll operate on
     * @throws DeploymentException if there was a problem
     */
    public void deploy(DeploymentDocument deployment)
        throws DeploymentException
    {
        deployment.deploy(this);
        doc = deployment;
    }

    /**
     * Obtain our "root" deployment document.
     */ 
    public DeploymentDocument getConfigDocument()
            throws DeploymentException {
        return doc;
    }

    /**
     * Set the global configuration
     * @param global XXX
     */
    public void setGlobalConfiguration(WSDDGlobalConfiguration global)
    {
        globalConfig = global;
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployItem(DeployableItem item)
        throws DeploymentException
    {
        QName qn = item.getQName();
        handlers.put(qn, item);
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployHandler(DeployableItem item)
        throws DeploymentException
    {
        handlers.put(item.getQName(), item);
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployService(DeployableItem item)
        throws DeploymentException
    {
        services.put(item.getQName(), item);
    }

    /**
     * Deploy the given WSDD Transport
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployTransport(DeployableItem item)
        throws DeploymentException
    {
        transports.put(item.getQName(), item);
    }

    /**
     * Return an instance of the deployed handler
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getHandler(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)handlers.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }
    
    /**
     * Return a deployment item of the deployed handler
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public DeployableItem getHandlerDeployableItem(QName qname)
        throws DeploymentException
    {
        DeployableItem item = (DeployableItem)handlers.get(qname);
        return item;
    }

    /**
     * Return an instance of the deployed service
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getService(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)services.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.toString());
        }
    }
    
    /**
     * Return a deployment item of the deployed service
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public DeployableItem getServiceDeployableItem(QName qname)
        throws DeploymentException
    {
        DeployableItem item = (DeployableItem)services.get(qname);
        return item;
    }

    /**
     * Return an instance of the deployed transport
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getTransport(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)transports.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }
    
    /**
     * Return a deployment item of the deployed transport
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public DeployableItem getTransportDeployableItem(QName qname)
        throws DeploymentException
    {
        DeployableItem item = (DeployableItem)transports.get(qname);
        return item;
    }

    /**
     * remove the given item
     * @param name XXX
     * @throws DeploymentException XXX
     */
    public void removeDeployedItem(QName qname)
        throws DeploymentException
    {
        handlers.remove(qname);
    }
    
    /**
     * remove the given handler
     * @param qname XXX
     * @throws DeploymentException XXX
     */
    public void undeployHandler(QName qname)
        throws DeploymentException
    {
        handlers.remove(qname);
    }
    
    /**
     * remove the given item
     * @param qname XXX
     * @throws DeploymentException XXX
     */
    public void undeployService(QName qname)
        throws DeploymentException
    {
        services.remove(qname);
    }

    /**
     * remove the given item
     * @param qname XXX
     * @throws DeploymentException XXX
     */
    public void undeployTransport(QName qname)
        throws DeploymentException
    {
        transports.remove(qname);
    }

    /**
     * return the named mapping registry
     * @param encodingStyle XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public TypeMappingRegistry getTypeMappingRegistry(String encodingStyle)
        throws DeploymentException
    {
        if (encodingStyle == null)
            encodingStyle = "";
        
        TypeMappingRegistry tmr =
            (TypeMappingRegistry) mappings.get(encodingStyle);

        return tmr;
    }

    /**
     * adds a new mapping registry
     * @param encodingStyle XXX
     * @param tmr XXX
     */
    public void addTypeMappingRegistry(String encodingStyle,
                                       TypeMappingRegistry tmr)
    {
        mappings.put(encodingStyle, tmr);
    }

    /**
     * remove the named mapping registry
     * @param encodingStyle XXX
     */
    public void removeTypeMappingRegistry(String encodingStyle)
    {
        mappings.remove(encodingStyle);
    }
    
    public void writeToContext(SerializationContext context)
        throws IOException
    {
        context.registerPrefixForURI("", WSDDConstants.WSDD_NS);
        context.registerPrefixForURI("java", WSDDConstants.WSDD_JAVA);
        context.startElement(new QName(WSDDConstants.WSDD_NS, "deployment"),
                             null);
        
        if (globalConfig != null) {
            globalConfig.writeToContext(context);
        }
        
        Iterator i = handlers.values().iterator();
        while (i.hasNext()) {
            WSDDHandler handler = (WSDDHandler)i.next();
            handler.writeToContext(context);
        }
        
        i = services.values().iterator();
        while (i.hasNext()) {
            WSDDService service = (WSDDService)i.next();
            service.writeToContext(context);
        }
        
        i = transports.values().iterator();
        while (i.hasNext()) {
            WSDDTransport transport = (WSDDTransport)i.next();
            transport.writeToContext(context);
        }
        
        TypeMappingRegistry tmr = getTypeMappingRegistry("");
        tmr.dumpToSerializationContext(context);
        
        context.endElement();
    }

    /**
     * Returns an Enumeration of the QNames for the list of deployed services
     * @return Enumeration of QNames
     * @throws DeploymentException
     */
    public Enumeration getServices() throws DeploymentException {
        return services == null ? null : services.keys();
    }

    /**
     * Returns an Enumeration of the QNames for the list of deployed handlers
     * @return Enumeration of QNames
     * @throws DeploymentException
     */
    public Enumeration getHandlers() throws DeploymentException {
        return handlers == null ? null : handlers.keys();
    }

    /**
     * Returns an Enumeration of the QNames for the list of deployed transports
     * @return Enumeration of QNames
     * @throws DeploymentException
     */
    public Enumeration getTransports() throws DeploymentException {
        return transports == null ? null : transports.keys();
    }

   /**
     * Deploy a Handler into the registry.
     */
    public void deployHandler(String key, Handler handler)
        throws DeploymentException
    {
        handler.setName(key);
        WSDDDocument doc = (WSDDDocument)getConfigDocument();
        WSDDHandler newHandler = new WSDDHandler();
        newHandler.setName(key);
        newHandler.setType(new QName(WSDDConstants.WSDD_JAVA,
                                     handler.getClass().getName()));
        newHandler.setOptionsHashtable(handler.getOptions());
        deployHandler(newHandler);
    }

    /**
     * Undeploy (remove) a Handler from the registry.
     */
    public void undeployHandler(String key)
        throws DeploymentException
    {
        removeDeployedItem(new QName("", key));
    }

    /**
     * Deploy a Service into the registry.
     */
     public void deployService(String key, SOAPService service)
        throws DeploymentException
    {
        category.info(JavaUtils.getMessage("deployService00", key, this.toString()));
        service.setName(key);
        
        WSDDService newService = new WSDDService();
        newService.setName(key);
        newService.setOptionsHashtable(service.getOptions());
        newService.setCachedService(service);
        
        Handler pivot = service.getPivotHandler();
        if (pivot == null) {
            throw new DeploymentException(JavaUtils.getMessage("noPivot01"));
        }
        
        if (pivot instanceof RPCProvider) {
            newService.setProviderQName(WSDDConstants.JAVARPC_PROVIDER);
        } else if (pivot instanceof MsgProvider) {
            newService.setProviderQName(WSDDConstants.JAVAMSG_PROVIDER);
        } else {
            newService.setProviderQName(WSDDConstants.HANDLER_PROVIDER);
            newService.setParameter("handlerClass", pivot.getClass().getName());
        }
        deployService(newService);
    }

    /**
     * Undeploy (remove) a Service from the registry.
     */
    public void undeployService(String key)
        throws DeploymentException
    {
        undeployService(new QName("", key));
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
        deployTransport(wt);
    }

    /**
     * Undeploy (remove) a client Transport
     */
    public void undeployTransport(String key)
        throws DeploymentException
    {
        undeployTransport(new QName("", key));
    }

    /**
     * Returns a global request handler.
     */
    public Handler getGlobalRequest()
        throws Exception
    {
        Handler h = null;
        if (globalConfig != null) {
            WSDDRequestFlow reqFlow = globalConfig.getRequestFlow();
            if (reqFlow != null)
                h = reqFlow.getInstance(this);
        }
        return h;
    }

   /**
     * Returns a global response handler.
     */
    public Handler getGlobalResponse()
        throws Exception
    {
        Handler h = null;
        if (globalConfig != null) {
            WSDDResponseFlow respFlow = globalConfig.getResponseFlow();
            if (respFlow != null)
                h = respFlow.getInstance(this);
        }
        return h;
    }

    /**
     * Returns the global configuration options.
     */
    public Hashtable getGlobalOptions()
    {
        Hashtable go = null;
        if (globalConfig != null) {
            go = globalConfig.getParametersTable();
        }
        return go;
    }
}
