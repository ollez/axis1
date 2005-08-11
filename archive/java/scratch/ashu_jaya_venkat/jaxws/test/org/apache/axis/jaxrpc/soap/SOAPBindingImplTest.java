package org.apache.axis.jaxrpc.soap;

import junit.framework.TestCase;
import org.apache.axis.jaxrpc.soap.SOAPBindingImpl;

public class SOAPBindingImplTest extends TestCase
{
  org.apache.axis.jaxrpc.soap.SOAPBindingImpl soapbindingimpl = null;
  
  public SOAPBindingImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis.jaxrpc.soap.SOAPBindingImpl createInstance() throws Exception {
    return new org.apache.axis.jaxrpc.soap.SOAPBindingImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    soapbindingimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    soapbindingimpl = null;
    super.tearDown();
  }
  
  public void testSetGetRoles() throws Exception {
    java.util.Set[] tests = {new java.util.Set(), null};
    
    for (int i = 0; i < tests.length; i++) {
      soapbindingimpl.setRoles(tests[i]);
      assertEquals(tests[i], soapbindingimpl.getRoles());
    }
  }
  
  public void testSetGetBinding() throws Exception {
    String[] tests = {new String(), null};
    
    for (int i = 0; i < tests.length; i++) {
      soapbindingimpl.setBinding(tests[i]);
      assertEquals(tests[i], soapbindingimpl.getBinding());
    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(SOAPBindingImplTest.class);
  }
}
