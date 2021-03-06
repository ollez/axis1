/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

package performence;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import services.parchase.Invoice;
import services.parchase.PurchaseOrder;

public class BrokerTest {
    public static void main(String[] args) throws Exception {
        String port1 = "4444";
        String port2 = "8080"; //port the TCP mon listens

        String endpoint1 = "http://127.0.0.1:" + port1 + "/service/servlet/AxisServlet";
        String endpoint2 = "http://127.0.0.1:" + port2 + "/axismora/servlet/AxisServlet";
        String SOAPAction = "Broker"; //service name

        QName method = new QName("supply");

        PurchaseOrder po = new PurchaseOrder("1123", 4, 345.00);

        QName q1 =
            new QName("urn:lk.opensource.service.wrappers.parchase", "PurchaseOrder");
        QName q2 = new QName("urn:lk.opensource.service.wrappers.parchase", "Invoice");

        Service service = new Service();
        Call call1 = (Call) service.createCall();

        call1.registerTypeMapping(
            PurchaseOrder.class,
            q1,
            new org.apache.axis.encoding.ser.BeanSerializerFactory(
                PurchaseOrder.class,
                q1),
            new org.apache.axis.encoding.ser.BeanDeserializerFactory(
                PurchaseOrder.class,
                q1));
        call1.registerTypeMapping(
            Invoice.class,
            q2,
            new org.apache.axis.encoding.ser.BeanSerializerFactory(Invoice.class, q2),
            new org.apache.axis.encoding.ser.BeanDeserializerFactory(Invoice.class, q2));

        call1.setTargetEndpointAddress(new java.net.URL(endpoint1));
        call1.setSOAPActionURI(SOAPAction);
        call1.setOperationName(method);
        call1.addParameter("op1", q1, ParameterMode.IN);
        call1.setReturnType(q2);

        Call call2 = (Call) service.createCall();

        call2.registerTypeMapping(
            PurchaseOrder.class,
            q1,
            new org.apache.axis.encoding.ser.BeanSerializerFactory(
                PurchaseOrder.class,
                q1),
            new org.apache.axis.encoding.ser.BeanDeserializerFactory(
                PurchaseOrder.class,
                q1));
        call2.registerTypeMapping(
            Invoice.class,
            q2,
            new org.apache.axis.encoding.ser.BeanSerializerFactory(Invoice.class, q2),
            new org.apache.axis.encoding.ser.BeanDeserializerFactory(Invoice.class, q2));

        call2.setTargetEndpointAddress(new java.net.URL(endpoint2));
        call2.setSOAPActionURI(SOAPAction);
        call2.setOperationName(method);
        call2.addParameter("op1", q1, ParameterMode.IN);
        call2.setReturnType(q2);

        PerformanceMonitor p = new PerformanceMonitor();
        while (true) {
            p.measure(call2, new Object[] { po }, call1, new Object[] { po });
        }

    }
}
