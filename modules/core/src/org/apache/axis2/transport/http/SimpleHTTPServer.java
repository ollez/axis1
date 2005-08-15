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
 */
package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 */
public class SimpleHTTPServer extends TransportListener {
    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());

    /**
     * Field systemContext
     */
    protected ConfigurationContext configurationContext;

    /**
     * Embedded commons http client based server
     */
    SimpleHttpServer embedded = null;

    int port = -1;

    /**
     * Constructor SimpleHTTPServer
     */
    public SimpleHTTPServer() {
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param systemContext
     */
    public SimpleHTTPServer(ConfigurationContext systemContext,
                            int port) throws IOException {
        this.configurationContext = systemContext;
        this.port = port;
    }

    /**
     * Constructor SimpleHTTPServer
     *
     * @param dir
     * @throws AxisFault
     */
    public SimpleHTTPServer(String dir, int port) throws AxisFault {
        try {
            this.port = port;
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            this.configurationContext = erfac.buildConfigurationContext(dir);
            Thread.sleep(2000);
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }

    /**
     * Checks if this HTTP server instance is running.
     *
     * @return  true/false
     */
    public boolean isRunning() {
        if(embedded == null) {
            return false;
        }
        return embedded.isRunning();
    }

    /**
     * stop the server if not already told to.
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws AxisFault {
        try {
            embedded = new SimpleHttpServer(port);
            embedded.setRequestHandler(new HTTPWorker(configurationContext));
        } catch (IOException e) {
            log.error(e);
            throw new AxisFault(e);
        }
    }

    /**
     * Stop this server. Can be called safely if the system is already stopped,
     * or if it was never started.
     * This will interrupt any pending accept().
     */
    public void stop() {
        log.info("stop called");
        if(embedded != null) {
            embedded.destroy();
        }
        log.info("Simple Axis Server Quits");
    }

    /**
     * Method getSystemContext
     *
     * @return the system context
     */
    public ConfigurationContext getSystemContext() {
        return configurationContext;
    }

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("SimpleHTTPServer repositoryLocation port");
            System.exit(1);
        }
        SimpleHTTPServer receiver = new SimpleHTTPServer(args[0], Integer.parseInt(args[1]));
        System.out.println("starting SimpleHTTPServer in port "
                + args[1]
                + " using the repository "
                + new File(args[0]).getAbsolutePath());
        try {
            System.out.println(
                    "[Axis2] Using the Repository " +
                    new File(args[0]).getAbsolutePath());
            System.out.println(
                    "[Axis2] Starting the SimpleHTTPServer on port " + args[1]);
            receiver.start();
            System.out.println("[Axis2] SimpleHTTPServer started");
            System.in.read();
        } finally {
            receiver.stop();
        }
    }

    /**
     * replyToEPR
     *
     * @param serviceName
     * @return an EndpointReference
     *
     * @see org.apache.axis2.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference replyToEPR(String serviceName) {
        return new EndpointReference("http://127.0.0.1:" + (embedded.getLocalPort()) +
                "/axis/services/" +
                serviceName);
    }

    /**
     * init method in TransportListener
     *
     * @param axisConf
     * @param transprtIn
     * @throws AxisFault
     */
    public void init(ConfigurationContext axisConf,
                     TransportInDescription transprtIn)
            throws AxisFault {
        try {
            this.configurationContext = axisConf;
            Parameter param = transprtIn.getParameter(PARAM_PORT);
            if (param != null) {
                this.port = Integer.parseInt((String) param.getValue());
            }
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }
}