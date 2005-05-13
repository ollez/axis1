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
package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * <p>This is Phase, a orderd collection of Handlers.
 * seems this is Handler Chain with order.</p>
 * Should this exttends Hanlders?
 */
public class Phase {
    /**
     * Field DISPATCH_PHASE
     */
    public static final String DISPATCH_PHASE = "DispatchPhase";

    /**
     * Field SERVICE_INVOCATION
     */
    public static final String SERVICE_INVOCATION = "ServiceInvocationPhase";

    /**
     * Field SENDING_PHASE
     */
    public static final String SENDING_PHASE = "SendPhase";

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org",
            "Phase");

    /**
     * Field phaseName
     */
    private String phaseName;

    /**
     * Field handlers
     */
    private ArrayList handlers;

    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    private int indexOfHandlerToExecute = 0;

    /**
     * Constructor Phase
     *
     * @param phaseName
     */
    public Phase(String phaseName) {
        handlers = new ArrayList();
        this.phaseName = phaseName;

    }

    /**
     * Method addHandler
     *
     * @param handler
     * @param index
     */
    public void addHandler(Handler handler, int index) {
        log.info("Handler " + handler.getName() + "Added to place " + 1
                + " At the Phase " + phaseName);
        handlers.add(index, handler);
    }

    /**
     * add to next empty handler
     *
     * @param handler
     */
    public void addHandler(Handler handler) {
        log.info("Handler " + handler.getName() + " Added to the Phase "
                + phaseName);
        handlers.add(handler);
    }

    /**
     * If need to see how this works look at the stack!
     *
     * @param msgctx
     * @throws AxisFault
     */
    public void invoke(MessageContext msgctx) throws AxisFault {
        // Stack executionStack = new Stack();
        try {
            while (indexOfHandlerToExecute < handlers.size() ) {
                if(msgctx.isPaused()){
                    break;
                }else{
                    Handler handler = (Handler) handlers.get(indexOfHandlerToExecute);
                    if (handler != null) {
                        log.info("Invoke the Handler " + handler.getName()
                                + "with in the Phase " + phaseName);
                        handler.invoke(msgctx);
                        //This line should be after the invoke as if the invocation failed this handlers is takn care of and 
                        //no need to revoke agien
                        //         executionStack.push(handler);
                        indexOfHandlerToExecute++;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Phase " + phaseName + " failed with the "
                    + e.getMessage());
//            while (!executionStack.isEmpty()) {
//                Handler handler = (Handler) executionStack.pop();
//                log.info("revoke the Handler " + handler.getName()
//                                + " with in the Phase " + phaseName);
//                handler.revoke(msgctx);
//            }
            throw AxisFault.makeFault(e);
        }
    }

//    /**
//     * Method revoke
//     *
//     * @param msgctx
//     */
//    public void revoke(MessageContext msgctx) {
//        for (int i = handlers.size() - 1; i > -1; i--) {
//            Handler handler = (Handler) handlers.get(i);
//            log.info("revoke the Handler " + handler.getName()
//                            + " with in the Phase " + phaseName);
//            if (handler != null) {
//                handler.revoke(msgctx);
//            }
//        }
//    }

    /**
     * @return Returns the name.
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * @param phaseName The name to set.
     */
    public void setName(String phaseName) {
        this.phaseName = phaseName;
    }
//    public void postCondition(MessageContext msgCtx) throws AxisFault {
//        
//
//    }
//
//    /* (non-Javadoc)
//     * @see org.apache.axis.engine.Phase#preCondition(org.apache.axis.context.MessageContext)
//     */
//    public void preCondition(MessageContext msgCtx) throws AxisFault {
//        // TODO Auto-generated method stub
//
//    }
    

    public int getHandlerCount(){
        return handlers.size();
    }

}
