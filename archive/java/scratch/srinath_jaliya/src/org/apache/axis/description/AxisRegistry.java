package org.apache.axis.description;

import org.apache.axis.clientapi.CallbacksBag;
import org.apache.axis.clientapi.TransportEndpointManager;

/*
 * Created on Mar 23, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author srinath
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AxisRegistry {
    private TransportEndpointManager epm;
    public TransportEndpointManager getEndPointManager(){
        return epm;
    }
    
    public void addService(AxisService service){
    
    }
    
    public CallbacksBag getCallBackBag(){
        return null;
    }
}