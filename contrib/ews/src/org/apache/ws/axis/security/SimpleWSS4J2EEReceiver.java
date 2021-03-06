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
package org.apache.ws.axis.security;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.geronimo.ews.ws4j2ee.context.security.impl.SecurityContext4J2EEImpl;
import org.apache.ws.axis.security.WSDoAllConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.message.token.UsernameToken;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.util.Iterator;

/**
 * This is an Axis handler that can be used to retrieve the credentials
 * available in the <code>UsernameToken</code> element.
 * This is a simple security handler that can provide only that service.
 * Therefore this handler can be used for testing perposes and
 * other simple works.
 * This can be configured to do the authentication at Axis or at J2EE Server.
 * For that you have to set the <code>WSS4J2EEConstants.AUTH_AT_AXIS</code>
 * to true.
 *
 * @author Rajith Priyanga (rpriyanga@yahoo.com)
 * @date May 29, 2004
 */
public class SimpleWSS4J2EEReceiver extends BasicHandler {

    MessageContext cntxt = null;

    boolean doAuthentication = false;

    /**
     * Retrieve the username-password information and perform a verification.
     *
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext msgCntxt) throws AxisFault {
        this.cntxt = msgCntxt;
        doAuthentication = false;
        try {
            //Get the SOAP header.
            Message m = msgCntxt.getCurrentMessage();
            SOAPHeader sh = m.getSOAPPart().getEnvelope().getHeader();
			
            //Retrieve the action property.
            String action = null;
            if ((action = (String) getOption(WSDoAllConstants.ACTION)) == null)
                action = (String) cntxt.getProperty(WSDoAllConstants.ACTION);
            if (action == null) {
                return;
            }
            String[] actions = action.split(" ");
            if (actions == null)
                return;
            boolean utAction = false;		
			
            //Check whether UsernameToken action property is available. Otherwise no more processing.
            for (int i = 0; i < actions.length; i++) {
                utAction = actions[i].equalsIgnoreCase(WSDoAllConstants.USERNAME_TOKEN);
                if (utAction)
                    break;
            }
            if (!utAction)
                return;		
			
            //Get all the headers.
            Iterator headers = sh.getChildElements();
            SOAPHeaderElement headerElem = null;
            if (headers == null) {
                throw AxisFault.makeFault(new Exception("No Security Headers found"));
            }
            //Find the security header.			
            while (headers.hasNext()) {
                headerElem = (SOAPHeaderElement) headers.next();
                if (headerElem.getLocalName().equals(WSConstants.WSSE_LN)
                        && headerElem.getNamespaceURI().equals(WSConstants.WSSE_NS)) {
                    //headerElem.setMustUnderstand(false);					
                    break;
                }
            }
			
            //Decides whether to do authentication at Axis or not.
            if (cntxt.containsProperty(WSS4J2EEConstants.AUTH_AT_AXIS)) {
                String check = (String) cntxt.getProperty(WSS4J2EEConstants.AUTH_AT_AXIS);
                if (check != null && check.equalsIgnoreCase("true"))
                    this.doAuthentication = true;
            }			
			
            //Hand over the security header to process it's UsernameToken.			
            processUsernameToken(headerElem);
            headerElem.detachNode();
        } catch (Exception ex) {
            throw AxisFault.makeFault(ex);
        }
    }

    /**
     * Processes the UsernameToken element of the security header.
     * It populates the SecurityContext4J2EE property of the MessageContext too.
     *
     * @param secHeader SOAP Security Header.
     * @throws Exception
     */
    private void processUsernameToken(SOAPHeaderElement secHeader) throws Exception {
        SOAPFactory sf = SOAPFactory.newInstance();
        Name utName = sf.createName(WSConstants.USERNAME_TOKEN_LN, WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS);
        Iterator toks = secHeader.getChildElements(utName);
        if (toks == null) {
            throw new Exception("No Security tokens found!");
        }
		
        //Get the UsernameToken element
        SOAPElement utElem = null;
        if (toks.hasNext()) {
            utElem = (SOAPElement) toks.next();
        } else {
            throw new Exception("No UsernameToken found!");
        }
        Name unName = sf.createName(WSConstants.USERNAME_LN, WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS);
        Name pwdName = sf.createName(WSConstants.PASSWORD_LN, WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS);
		
        //Get the user name
        String username = ((SOAPElement) (utElem.getChildElements(unName).next())).getValue();
		
        //Get the password element
        SOAPElement pwdElem = (SOAPElement) utElem.getChildElements(pwdName).next();
		
        //Get the password type 
        String pwdType = pwdElem.getAttributeValue(sf.createName(WSConstants.PASSWORD_TYPE_ATTR, WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS));//,  WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS));
		
        //Get the password
        String pwd = pwdElem.getValue();
		
        //If the password type is not speciied take it as PASSWORD_TEXT type.
        if (pwdType == null)
            pwdType = WSConstants.PASSWORD_TEXT;
        if (pwdType.equalsIgnoreCase(WSConstants.PASSWORD_TEXT)) {
            ///////////// This part can be removed. . /////////////////
            if (doAuthentication) {
                if (!veryfyPWD(username, pwd)) {
                    throw new Exception("Password Verification failed!");
                }
            }
            ///////////////////////////////////////////////////////////
            this.populateSecurityContext4J2EE(username, pwd, pwdType, null, null);
            //this.Authenticate4J2EE();
        } else if (pwdType.equalsIgnoreCase(WSConstants.PASSWORD_DIGEST)) {
            Name nonceName = sf.createName(WSConstants.NONCE_LN, WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS);
            Name createdName = sf.createName(WSConstants.CREATED_LN, WSConstants.WSU_PREFIX, WSConstants.WSU_NS);
            Iterator elems = utElem.getChildElements(nonceName);
            String nonce = this.extractNonce(elems);
            elems = utElem.getChildElements(createdName);
            String created = this.extractCreated(elems);  
            ///////////// This part can be removed. . /////////////////
            if (doAuthentication) {
                if (!veryfyPWD(username, pwd, nonce, created)) {
                    throw new Exception("Password Verification failed!");
                }
            }
            ///////////////////////////////////////////////////////////
            this.populateSecurityContext4J2EE(username, pwd, pwdType, nonce, created);
        } else {
            throw new Exception("Unsupported Password Type!");
        }
    }

    /**
     * Extracts the nonce value from the given set of elements.
     * (It is given as a iteratorf o elements)
     *
     * @param elements
     * @return
     * @throws Exception
     */
    private String extractNonce(Iterator elements) throws Exception {
        boolean noNonce = false;
        String nonce = null;
        if (elements == null) {
            noNonce = true;
        }
        if (!noNonce && elements.hasNext()) {
            nonce = ((SOAPElement) (elements.next())).getValue();
        } else {
            noNonce = true;
        }
        if (nonce == null) {
            noNonce = true;
        }
        if (noNonce)
            throw new Exception("Nonce is not specified!");
        return nonce;
    }

    /**
     * Extracts the created value from the given set of elements.
     * (It is given as a iteratorf o elements)
     *
     * @param elements
     * @return
     * @throws Exception
     */
    private String extractCreated(Iterator elements) throws Exception {
        boolean noCreated = false;
        String created = null;
        if (elements == null)
            noCreated = true;
        if (!noCreated && elements.hasNext())
            created = ((SOAPElement) (elements.next())).getValue();
        else
            noCreated = true;
        if (created == null)
            noCreated = true;
        if (noCreated)
            throw new Exception("Created is not specified!");
        return created;
    }

    /**
     * Verifies the PASSWORD_TEXT type passwords.
     */
    private boolean veryfyPWD(String username, String password) throws Exception {
        if (password.equals(this.fetchActualPWD(username))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifies the PASSWORD_DIGEST type passwords.
     */
    private boolean veryfyPWD(String username,
                              String password,
                              String nonce,
                              String created) throws Exception {
        //TODO
        //Check whether (created > currentTime - 5 minutes).
        //Cache the nonce for the user and check it before verification.
		
        if (nonce == null || created == null) {
            throw new Exception("Nonce or Created not supplied!");
        }
        String digest = UsernameToken.doPasswordDigest(nonce, created, this.fetchActualPWD(username));
        if (password.equals(digest)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * }
     * Fetches the actual password using the CallbackHandler specified
     * in the deployment descripter.
     *
     * @param username username
     * @return the actual password of the user.
     * @throws Exception
     */
    private String fetchActualPWD(String username) throws Exception {
        WSPasswordCallback pwcb = new WSPasswordCallback(username, WSPasswordCallback.USERNAME_TOKEN);
        Callback[] cb = new Callback[1];
        cb[0] = pwcb;
        CallbackHandler cbh = (CallbackHandler) this.cntxt.getProperty(WSDoAllConstants.PW_CALLBACK_REF);
        if (cbh == null) {
            String cbhClass = (String) this.cntxt.getProperty(WSDoAllConstants.PW_CALLBACK_CLASS);
            cbh = (CallbackHandler) Class.forName(cbhClass).newInstance();
        }
        if (cbh == null) {
            throw new Exception("PasswordCallbackHandler not found!");
        }
        cbh.handle(cb);
        String pwd = ((WSPasswordCallback) (cb[0])).getPassword();
        if (pwd == null)
            throw new Exception("Password is not provided.");
        return pwd;
    }
	 
	
    /**
     * Associates a Authenticated principal with this thread this thread.
     * @throws Exception 
     */
    /*private void Authenticate4J2EE() throws Exception{
        CallbackHandler cbh = (CallbackHandler)this.cntxt.getProperty(WSDoAllConstants.PW_CALLBACK_REF);
        if(cbh == null){
            String cbhClass = (String)this.cntxt.getProperty(WSDoAllConstants.PW_CALLBACK_CLASS);			
            cbh = (CallbackHandler)Class.forName(cbhClass).newInstance();			
        }								
		
        if(cbh != null){
            javax.security.auth.login.LoginContext lc
                = new javax.security.auth.login.LoginContext("LC4" + this.cntxt.getTargetService(), cbh);
            lc.login();
        }
        else
            throw new Exception("CallbackHandler is null.");
    }*/
	
	
    /**
     * Populates the SecurityContext4J2EE property with the given
     * security information.
     *
     * @param username
     * @param password
     * @param passwordType
     * @param nonce
     * @param created
     */
    private void populateSecurityContext4J2EE(String username, String password, String passwordType, String nonce, String created) {
        SecurityContext4J2EEImpl sc4j2ee =
                (SecurityContext4J2EEImpl) this.cntxt.getProperty(WSS4J2EEConstants.SEC_CONTEXT_4J2EE);
        if (sc4j2ee == null) {
            sc4j2ee = new SecurityContext4J2EEImpl();
            this.cntxt.setProperty(WSS4J2EEConstants.SEC_CONTEXT_4J2EE, sc4j2ee);
        }
						
        //Populate the SecurityContext4J2EE with the user name token data.
        sc4j2ee.setUsername(username);
        sc4j2ee.setPassword(password.toCharArray());
        if (passwordType.equalsIgnoreCase(WSConstants.PASSWORD_DIGEST)) {
            sc4j2ee.setPasswordDigested(true);
            sc4j2ee.setNonce(nonce);
            sc4j2ee.setCreated(created);
        } else
            sc4j2ee.setPasswordDigested(false);
        PWDCallbackHandler4J2EE cbh = new PWDCallbackHandler4J2EE(username, password.toCharArray());
        sc4j2ee.setPWDCallbackHandler4J2EE(cbh);
    }

}


