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

package org.apache.axis.engine.registry;

import java.lang.reflect.Method;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.context.MessageContext;
import org.apache.axis.providers.AbstractProvider;

public class SpeakingProvider extends AbstractProvider implements Handler {
    private String message;
    public SpeakingProvider(){}


    public void invoke(MessageContext msgContext) throws AxisFault {
        System.out.println("I am Speaking Provider Running :)");
    }

    public void revoke(MessageContext msgContext) {
        System.out.println("I am Speaking Provider revoking :)");
    }
    /* (non-Javadoc)
     * @see org.apache.axis.engine.AbstractProvider#deserializeParameters(org.apache.axis.engine.MessageContext, java.lang.reflect.Method)
     */
    public Object[] deserializeParameters(
        MessageContext msgContext,
        Method method)
        throws AxisFault {
        return null;
    }

    public Object getTheImplementationObject(MessageContext msgContext)
        throws AxisFault {
        return null;
    }

    protected Object makeNewServiceObject(
        MessageContext msgContext)
        throws AxisFault {
        // TODO Auto-generated method stub
        return null;
    }

}
