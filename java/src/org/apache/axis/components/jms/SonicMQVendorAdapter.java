/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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

package org.apache.axis.components.jms;

import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.transport.jms.JMSConnector;
import org.apache.axis.transport.jms.JMSConnectorFactory;
import org.apache.axis.transport.jms.JMSURLHelper;

import progress.message.client.ENetworkFailure;
import progress.message.client.EUserAlreadyConnected;
import progress.message.jclient.ErrorCodes;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Defines SonicMQ specific constants for connnection factory creation.
 * Overrides methods in BeanVendorAdapter to fill in MQ classnames
 *
 * @author Jaime Meritt (jmeritt@sonicsoftware.com)
 * @author Ray Chun (rchun@sonicsoftware.com)
 */
public class SonicMQVendorAdapter extends BeanVendorAdapter
{
    private final static String QCF_CLASS =
        "progress.message.jclient.QueueConnectionFactory";

    private final static String TCF_CLASS =
        "progress.message.jclient.TopicConnectionFactory";

    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>.
     * This is a required property.
     * The value must be a <code>java.lang.String</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String BROKER_URL              = "brokerURL";

    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domains.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * This is a required property for secure brokers.
     * The value must be a <code>java.lang.String</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String DEFAULT_USERNAME = "defaultUser";

    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * This is a required property for secure brokers.
     * The value must be a <code>java.lang.String</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String DEFAULT_PASSWORD        = "defaultPassword";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Long</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String PING_INTERVAL           = "pingIntervalLong";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Integer</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String RECONNECT_INTERVAL      = "reconnectIntervalInteger";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Integer</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String RECONNECT_TIMEOUT       = "reconnectTimeoutInteger";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.String</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String CONNECT_ID              = "connectID";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.String</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String CONNECTION_URLS         = "connectionURLs";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Boolean</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String LOAD_BALANCING          = "loadBalancingBoolean";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Long</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String MONITOR_INTERVAL        = "monitorInterval";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Boolean</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String PERSISTENT_DELIVERY     = "persistentDeliveryBoolean";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Boolean</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String SEQUENTIAL              = "sequentialBoolean";

    /**
     * <code>SonicConnectionFactory</code> parameter valid for the PTP domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Integer</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String PREFETCH_COUNT          = "prefetchCountInteger";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for the PTP domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Integer</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String PREFETCH_THRESHOLD      = "prefetchThresholdInteger";
    /**
     * <code>SonicConnectionFactory</code> parameter valid for the PubSub domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * The value must be a <code>java.lang.Boolean</code>
     * See the SonicMQ documentation for information on this property
     */
    public final static String SELECTOR_AT_BROKER      = "selectorAtBroker";

    public QueueConnectionFactory getQueueConnectionFactory(HashMap cfConfig)
        throws Exception
    {
        cfConfig = (HashMap)cfConfig.clone();
        cfConfig.put(CONNECTION_FACTORY_CLASS, QCF_CLASS);
        return super.getQueueConnectionFactory(cfConfig);
    }

    public TopicConnectionFactory getTopicConnectionFactory(HashMap cfConfig)
        throws Exception
    {
        cfConfig = (HashMap)cfConfig.clone();
        cfConfig.put(CONNECTION_FACTORY_CLASS, TCF_CLASS);
        return super.getTopicConnectionFactory(cfConfig);
    }

    /**
     * Extract Sonic-specific properties from the JMS URL
     *
     * @param jmsurl The JMS URL representing the target endpoint address
     * @param cfProps The set of connection factory configuration properties
     */
    public void addVendorConnectionFactoryProperties(JMSURLHelper jmsurl, HashMap cfProps)
    {
        if (jmsurl.getPropertyValue(BROKER_URL) != null)
            cfProps.put(BROKER_URL, jmsurl.getPropertyValue(BROKER_URL));

        if (jmsurl.getPropertyValue(DEFAULT_USERNAME) != null)
            cfProps.put(DEFAULT_USERNAME, jmsurl.getPropertyValue(DEFAULT_USERNAME));

        if (jmsurl.getPropertyValue(DEFAULT_PASSWORD) != null)
            cfProps.put(DEFAULT_PASSWORD, jmsurl.getPropertyValue(DEFAULT_PASSWORD));

        if (jmsurl.getPropertyValue(PING_INTERVAL) != null)
            cfProps.put(PING_INTERVAL, jmsurl.getPropertyValue(PING_INTERVAL));

        if (jmsurl.getPropertyValue(RECONNECT_INTERVAL) != null)
            cfProps.put(RECONNECT_INTERVAL, jmsurl.getPropertyValue(RECONNECT_INTERVAL));

        if (jmsurl.getPropertyValue(RECONNECT_TIMEOUT) != null)
            cfProps.put(RECONNECT_TIMEOUT, jmsurl.getPropertyValue(RECONNECT_TIMEOUT));

        if (jmsurl.getPropertyValue(CONNECT_ID) != null)
            cfProps.put(CONNECT_ID, jmsurl.getPropertyValue(CONNECT_ID));

        if (jmsurl.getPropertyValue(CONNECTION_URLS) != null)
            cfProps.put(CONNECTION_URLS, jmsurl.getPropertyValue(CONNECTION_URLS));

        if (jmsurl.getPropertyValue(LOAD_BALANCING) != null)
            cfProps.put(LOAD_BALANCING, jmsurl.getPropertyValue(LOAD_BALANCING));

        if (jmsurl.getPropertyValue(MONITOR_INTERVAL) != null)
            cfProps.put(MONITOR_INTERVAL, jmsurl.getPropertyValue(MONITOR_INTERVAL));

        if (jmsurl.getPropertyValue(PERSISTENT_DELIVERY) != null)
            cfProps.put(PERSISTENT_DELIVERY, jmsurl.getPropertyValue(PERSISTENT_DELIVERY));

        if (jmsurl.getPropertyValue(SEQUENTIAL) != null)
            cfProps.put(SEQUENTIAL, jmsurl.getPropertyValue(SEQUENTIAL));

        if (jmsurl.getPropertyValue(PREFETCH_COUNT) != null)
            cfProps.put(PREFETCH_COUNT, jmsurl.getPropertyValue(PREFETCH_COUNT));

        if (jmsurl.getPropertyValue(PREFETCH_THRESHOLD) != null)
            cfProps.put(PREFETCH_THRESHOLD, jmsurl.getPropertyValue(PREFETCH_THRESHOLD));

        if (jmsurl.getPropertyValue(SELECTOR_AT_BROKER) != null)
            cfProps.put(SELECTOR_AT_BROKER, jmsurl.getPropertyValue(SELECTOR_AT_BROKER));
    }

    /**
     * Check that the attributes of the candidate connection factory match the
     * requested connection factory properties.
     *
     * @param cf the candidate connection factory
     * @param jmsurl the JMS URL associated with the candidate connection factory
     * @param cfProps the properties associated with the current request
     * @return true or false
     */
    public boolean isMatchingConnectionFactory(javax.jms.ConnectionFactory cf,
                                               JMSURLHelper jmsurl,
                                               HashMap cfProps)
    {
        String brokerURL = null;
        String connectionURLs = null;
        boolean loadBalancing = false;
        boolean sequential = false;

        if (cf instanceof progress.message.jclient.QueueConnectionFactory)
        {
            progress.message.jclient.QueueConnectionFactory qcf =
                (progress.message.jclient.QueueConnectionFactory)cf;

            // get existing queue connection factory properties
            brokerURL = qcf.getBrokerURL();
            connectionURLs = qcf.getConnectionURLs();
            loadBalancing = qcf.getLoadBalancing();
            sequential = qcf.getSequential();
        }
        else if (cf instanceof progress.message.jclient.TopicConnectionFactory)
        {
            progress.message.jclient.TopicConnectionFactory tcf =
                (progress.message.jclient.TopicConnectionFactory)cf;

            // get existing topic connection factory properties
            brokerURL = tcf.getBrokerURL();
            connectionURLs = tcf.getConnectionURLs();
            loadBalancing = tcf.getLoadBalancing();
            sequential = tcf.getSequential();
        }

        // compare broker url
        String propertyBrokerURL = (String)cfProps.get(BROKER_URL);
        if (!brokerURL.equals(propertyBrokerURL))
            return false;

        // compare connection url list
        String propertyConnectionURLs = (String)cfProps.get(CONNECTION_URLS);
        if ((connectionURLs != null) && (propertyConnectionURLs != null))
        {
            if (!connectionURLs.equalsIgnoreCase(propertyConnectionURLs))
                return false;

            // check sequential if connection urls have been set
            String tmpSequential = (String)cfProps.get(SEQUENTIAL);
            boolean propertySequential = true;
            if (tmpSequential != null)
                propertySequential = Boolean.getBoolean(tmpSequential);
            if (sequential != propertySequential)
                return false;
        }
        else if ((connectionURLs != null) || (propertyConnectionURLs != null))
            return false;

        // compare load balancing flag
        String tmpLoadBalancing = (String)cfProps.get(LOAD_BALANCING);
        boolean propertyLoadBalancing = false;
        if (tmpLoadBalancing != null)
            propertyLoadBalancing = Boolean.getBoolean(tmpLoadBalancing);
        if (loadBalancing != propertyLoadBalancing)
            return false;

        return true;
    }

    public boolean isRecoverable(Throwable thrown, int action)
    {
        //the super class cannot be trusted for on exception because it always
        //returns false
        if(action != ON_EXCEPTION_ACTION && !super.isRecoverable(thrown, action))
            return false;

        if(!(thrown instanceof JMSException))
            return true;

        JMSException jmse = (JMSException)thrown;
        switch(action)
        {
            case CONNECT_ACTION:
                if(isNetworkFailure(jmse))
                    return false;
                break;
            case SUBSCRIBE_ACTION:

                if(isQueueMissing(jmse) || isAnotherSubscriberConnected(jmse))
                    return false;
                break;

            case ON_EXCEPTION_ACTION:
                if(isConnectionDropped(jmse))
                    return false;
                break;

        }

        return true;
    }

    public boolean isConnectionDropped(JMSException jmse)
    {
        return ErrorCodes.testException(jmse, ErrorCodes.ERR_CONNECTION_DROPPED);
    }

    private boolean isQueueMissing(JMSException jmse)
    {
        String message = jmse.getMessage();
        if(message != null && message.startsWith("Queue not found"))
        {
            return true;
        }
        return false;
    }

    private boolean isAnotherSubscriberConnected(JMSException jmse)
    {
        Exception linkedException = jmse.getLinkedException();
        if(linkedException != null &&
           linkedException instanceof EUserAlreadyConnected)
        {
            return true;
        }
        return false;
    }

    private boolean isNetworkFailure(JMSException jmse)
    {
        Exception linkedException = jmse.getLinkedException();
        if(linkedException != null &&
           linkedException instanceof ENetworkFailure)
        {
            return true;
        }
        return false;
    }
}