/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
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
 *
 */
package org.apache.geronimo.ews.ws4j2ee;

import java.io.FileInputStream;

import org.apache.geronimo.ews.AbstractTestCase;
import org.apache.geronimo.ews.ws4j2ee.context.J2EEWebServiceContext;
import org.apache.geronimo.ews.ws4j2ee.context.JaxRpcMapperContext;
import org.apache.geronimo.ews.ws4j2ee.context.MiscInfo;
import org.apache.geronimo.ews.ws4j2ee.context.impl.MiscInfoImpl;
import org.apache.geronimo.ews.ws4j2ee.context.webservices.server.interfaces.WSCFContext;
import org.apache.geronimo.ews.ws4j2ee.context.wsdl.WSDLContext;
import org.apache.geronimo.ews.ws4j2ee.parsers.EJBDDParser;

/**
 * @author hemapani
 */
public class EJBDDTest extends AbstractTestCase{
    /**
     * @param testName
     */
    public EJBDDTest(String testName) {
        super(testName);
    }

	public void testGoogleSample() throws Exception{
		try{
			J2EEWebServiceContext con = new J2EEWebServiceContext() {
				public WSDLContext getWSDLContext() {return null;}
				public void setWSDLContext(WSDLContext wsdlcontext) {}
				public WSCFContext getWSCFContext() {return null;}
				public void setWSCFContext(WSCFContext wscfcontext) {}
				public JaxRpcMapperContext getJAXRPCMappingContext() {return null;}
				public void setJAXRPCMappingContext(JaxRpcMapperContext context) {}
				public MiscInfo getMiscInfo() {return new MiscInfoImpl();}
				public void setMiscInfo(MiscInfo info) {}
				public void validate() {
				}
			};
		   EJBDDParser pars = new EJBDDParser(con);
		   pars.parse(new FileInputStream(testDir+"testData/Math/ejb-jar.xml"));
		   //Assert.assertEquals(con.getMiscInfo().getEjbName(),"MathFace"); 
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
}
