package test.encoding;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.axis.Constants;

/** 
 * Test deserialization of SOAP responses
 */
public class TestDeser1999 extends TestDeser {

    public TestDeser1999(String name) {
        super(name, Constants.URI_1999_SCHEMA_XSI,
                    Constants.URI_1999_SCHEMA_XSD);
    }

    public void testMapWithNulls() throws Exception {
        HashMap m = new HashMap();
        m.put(null, new Boolean("false"));
        m.put("hi", null);
        deserialize("<result xsi:type=\"xmlsoap:Map\" " +
                    "xmlns:xmlsoap=\"http://xml.apache.org/xml-soap\"> " +
                      "<item>" +
                       "<key xsi:null=\"true\"/>" +
                       "<value xsi:type=\"xsd:boolean\">false</value>" + 
                      "</item><item>" +
                       "<key xsi:type=\"string\">hi</key>" +
                       "<value xsi:null=\"true\"/>" +
                      "</item>" +
                    "</result>",
                    m);
    }

    public void testArrayWithNullInt() throws Exception {
        ArrayList list = new ArrayList(4);
        list.add(new Integer(1));
        list.add(null);
        list.add(new Integer(3));
        deserialize("<result xsi:type=\"soapenc:Array\" " +
                            "soapenc:arrayType=\"xsd:int[3]\"> " +
                       "<item xsi:type=\"xsd:int\">1</item>" +
                       "<item xsi:null=\"true\"/>" +
                       "<item xsi:type=\"xsd:int\">3</item>" +
                    "</result>",
                    list, true);
    }
    
    public void testArrayWithNullString() throws Exception {
        ArrayList list = new ArrayList(4);
        list.add("abc");
        list.add(null);
        list.add("def");
        deserialize("<result xsi:type=\"soapenc:Array\" " +
                            "soapenc:arrayType=\"xsd:string[3]\"> " +
                       "<item xsi:type=\"xsd:string\">abc</item>" +
                       "<item xsi:null=\"true\"/>" +
                       "<item xsi:type=\"xsd:string\">def</item>" +
                    "</result>",
                    list, true);
    }
    
    public void testNullSOAPBoolean() throws Exception {
        deserialize("<result xsi:type=\"soapenc:boolean\" xsi:null=\"true\" />",
                    null);
    }
}
