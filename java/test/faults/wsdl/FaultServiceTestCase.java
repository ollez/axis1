/**
 * FaultServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis Wsdl2java emitter.
 */

package test.faults.wsdl;

import junit.framework.AssertionFailedError;
import junit.framework.Assert;
import junit.framework.TestCase;


public class FaultServiceTestCase extends junit.framework.TestCase {
    public FaultServiceTestCase(String name) {
        super(name);
    }

    public void testFaultService() {
        test.faults.wsdl.FaultServicePortType binding = new FaultService().getFaultService();
        assertTrue("binding is null", binding != null);
        String symbol = new String("MACR");
        try {
            float value = 0;
            value = binding.getQuote(symbol);
            fail("Should raise an InvalidTickerFault"); 
        } 
        catch (InvalidTickerFaultMessage tickerFault) {
            // We don't support fault data yet!
            //assertEquals("Ticker Symbol in Fault doesn't match original argument", 
            //        symbol, tickerFault.getTickerSymbol());
        } 
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re );
        }
    }
}

