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

package org.apache.axis.soap;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;

public class SOAPFaultTextTest extends SOAPFaultReasonTestCase {
    protected SOAPFaultText soap11FaultText;
    protected SOAPFaultText soap12FaultText;
    protected SOAPFaultText soap11FaultTextWithParser;
    protected SOAPFaultText soap12FaultTextWithParser;

    public SOAPFaultTextTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11FaultText = soap11Factory.createSOAPFaultText(soap11FaultReason);
        soap12FaultText = soap12Factory.createSOAPFaultText(soap12FaultReason);
        soap11FaultTextWithParser = soap11FaultReasonWithParser.getSOAPText();
        soap12FaultTextWithParser = soap12FaultReasonWithParser.getSOAPText();
    }

    //SOAP 1.1 Fault Text Test (Programaticaly Created)
    public void testSOAP11SetLang() {
        soap11FaultText.setLang("en");
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setLang method, Lang attribute value mismatch", soap11FaultText.getLang().equals("en"));
        OMAttribute langAttribute = (OMAttribute) soap11FaultText.getAttributes().next();
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setLang method, Lang attribute local name mismaatch", langAttribute.getLocalName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME));
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setLang method, Lang attribute namespace prefix mismatch", langAttribute.getNamespace().getPrefix().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setLang method, Lang attribute namespace uri mismatch", langAttribute.getNamespace().getName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI));
    }

    public void testSOAP11GetLang() {


        assertNull("SOAP 1.1 Fault Text Test : - After creating SOAPFaultText, it has a Lnag attribute", soap11FaultText.getLang());

        soap11FaultText.setLang("en");
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setLang method, Lang attribute value mismatch", soap11FaultText.getLang().equals("en"));
    }

    public void testSOAP11SetText() {
        soap11FaultText.setText("This is only a test");
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setText method, getText method return incorrect string", soap11FaultText.getText().equals("This is only a test"));
    }

    public void testSOAP11GetText() {
        assertTrue("SOAP 1.1 Fault Text Test : - After creating SOAPFaultText, it has a text", soap11FaultText.getText().equals(""));
        soap11FaultText.setText("This is only a test");
        assertTrue("SOAP 1.1 Fault Text Test : - After calling setText method, getText method return incorrect string", soap11FaultText.getText().equals("This is only a test"));
    }

    //SOAP 1.2 Fault Text Test (Programaticaly Created)
    public void testSOAP12SetLang() {
        soap12FaultText.setLang("en");
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setLang method, Lang attribute value mismatch", soap12FaultText.getLang().equals("en"));
        OMAttribute langAttribute = (OMAttribute) soap12FaultText.getAttributes().next();
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setLang method, Lang attribute local name mismaatch", langAttribute.getLocalName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME));
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setLang method, Lang attribute namespace prefix mismatch", langAttribute.getNamespace().getPrefix().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setLang method, Lang attribute namespace uri mismatch", langAttribute.getNamespace().getName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI));
    }

    public void testSOAP12GetLang() {

        assertNull("SOAP 1.2 Fault Text Test : - After creating SOAPFaultText, it has a Lnag attribute", soap12FaultText.getLang());

        soap12FaultText.setLang("en");
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setLang method, Lang attribute value mismatch", soap12FaultText.getLang().equals("en"));
    }

    public void testSOAP12SetText() {
        soap12FaultText.setText("This is only a test");
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setText method, getText method return incorrect string", soap12FaultText.getText().equals("This is only a test"));
    }

    public void testSOAP12GetText() {
        assertTrue("SOAP 1.2 Fault Text Test : - After creating SOAPFaultText, it has a text", soap12FaultText.getText().equals(""));
        soap12FaultText.setText("This is only a test");
        assertTrue("SOAP 1.2 Fault Text Test : - After calling setText method, getText method return incorrect string", soap12FaultText.getText().equals("This is only a test"));
    }

    //SOAP 1.1 Fault Text Test (With Parser)
    public void testSOAP11GetTextWithParser() {
        assertTrue("SOAP 1.1 Fault Text Test With Parser : - getText method returns incorrect string", soap11FaultTextWithParser.getText().equals("Sender Timeout"));
    }

    //SOAP 1.2 Fault Text Test (With Parser)
    public void testSOAP12GetLangWithParser() {
        assertTrue("SOAP 1.2 Fault Text Test With Parser : - getLang method returns incorrect string", soap12FaultTextWithParser.getLang().equals("en"));
        OMAttribute langAttribute = (OMAttribute) soap12FaultTextWithParser.getAttributes().next();
        assertTrue("SOAP 1.2 Fault Text Test With Parser : - Lang attribute local name mismaatch", langAttribute.getLocalName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME));
        assertTrue("SOAP 1.2 Fault Text Test With Parser : - Lang attribute namespace prefix mismatch", langAttribute.getNamespace().getPrefix().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        assertTrue("SOAP 1.2 Fault Text Test With Parser : - Lang attribute namespace uri mismatch", langAttribute.getNamespace().getName().equals(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI));
    }

    public void testSOAP12GetTextWithParser() {
        assertTrue("SOAP 1.2 Fault Text Test With Parser : - getText method returns incorrect string", soap12FaultTextWithParser.getText().equals("Sender Timeout"));
    }
}
