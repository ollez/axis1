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
package org.apache.axis.transport;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;

/**
 * Class TransportSenderLocator
 */
public class TransportSenderLocator {
    /**
     * Method locate
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public static TransportSender locate(MessageContext msgContext) throws AxisFault {
        try {
            String type = (String) msgContext.getProperty(MessageContext.TRANSPORT_TYPE);
            if (Constants.TRANSPORT_HTTP.equals(type)) {
                Class className =
                    Class.forName("org.apache.axis.transport.http.HTTPTransportSender");
                return (TransportSender) className.newInstance();
            } else if (Constants.TRANSPORT_MAIL.equals(type)) {
                Class className =
                    Class.forName("org.apache.axis.transport.mail.MailTransportSender");
                return (TransportSender) className.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e.getMessage(), e);
        }
        throw new AxisFault("No transport found");
    }
}