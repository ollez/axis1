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
package org.apache.geronimo.ews.ws4j2ee;

import java.io.FileInputStream;

import org.apache.geronimo.ews.AbstractTestCase;
import org.apache.geronimo.ews.ws4j2ee.context.J2EEWebServiceContext;
import org.apache.geronimo.ews.ws4j2ee.context.JaxRpcMapperContext;
import org.apache.geronimo.ews.ws4j2ee.context.MiscInfo;
import org.apache.geronimo.ews.ws4j2ee.context.impl.MiscInfoImpl;
import org.apache.geronimo.ews.ws4j2ee.context.j2eeDD.EJBContext;
import org.apache.geronimo.ews.ws4j2ee.context.j2eeDD.WebContext;
import org.apache.geronimo.ews.ws4j2ee.context.webservices.client.interfaces.ServiceReferanceContext;
import org.apache.geronimo.ews.ws4j2ee.context.webservices.server.interfaces.WSCFContext;
import org.apache.geronimo.ews.ws4j2ee.context.wsdl.WSDLContext;
import org.apache.geronimo.ews.ws4j2ee.parsers.DomEJBDDParser;
import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2eeFactory;

/**
 * @author hemapani
 */
public class EJBDDTest extends AbstractTestCase{
    private J2EEWebServiceContext con = new J2EEWebServiceContext() {
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
        public void setFactory(Ws4J2eeFactory factory){}
        public Ws4J2eeFactory getFactory(){
            return null;
        }

        public EJBContext getEJBDDContext(){
            return null;
        }
        public void setEJBDDContext(EJBContext context){}

        public WebContext getWebDDContext(){
            return null;
        }
        public void setWebDDContext(WebContext context){}

        public ServiceReferanceContext getServiceReferanceContext(int index){
            return null;
        }
        public void addServiceReferanceContext(ServiceReferanceContext context){}
        public int getServiceReferanceContextCount(){
            return 0;
        }
    };

    /**
     * @param testName
     */
    public EJBDDTest(String testName) {
        super(testName);
    }

//	public void testMathSample() throws Exception{
//		try{
//		   EJBDDParser pars = new EJBDDParser(con);
//		   pars.parse(new FileInputStream(
//		   	getTestFile(testDir+"testData/math/ejb-jar.xml")));
//		   //Assert.assertEquals(con.getMiscInfo().getEjbName(),"MathFace"); 
//		}catch(Exception e){
//			e.printStackTrace();
//			throw e;
//		}
//	}
    public void testMathSampleWithDOM() throws Exception{
        try{
            DomEJBDDParser pars = new DomEJBDDParser(con);
           pars.parse(new FileInputStream(
            getTestFile(testDir+"testData/math/ejb-jar.xml")));
           //Assert.assertEquals(con.getMiscInfo().getEjbName(),"MathFace"); 
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
//TODO
//    public void testMathSampleWithDOMWithDOCTYPE() throws Exception{
//        try{
//            DomEJBDDParser pars = new DomEJBDDParser(con);
//           pars.parse(new FileInputStream(
//            getTestFile(testDir+"testData/math/ejb-jar1.xml")));
//           //Assert.assertEquals(con.getMiscInfo().getEjbName(),"MathFace"); 
//        }catch(Exception e){
//            e.printStackTrace();
//            throw e;
//        }
//    }

}
