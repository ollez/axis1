package test.utils;

import junit.framework.*;
import java.io.*;
import org.apache.axis.utils.XMLUtils;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class TestXMLUtils extends TestCase
{

    public TestXMLUtils (String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestXMLUtils.class);
    }

    public void setup() {
    }
    
    public void testInit()
    {
        DocumentBuilderFactory dbf = XMLUtils.init();
        assert(dbf instanceof DocumentBuilderFactory);
    }

    public void testNewDocumentNoArgConstructor()
    {
        Document doc = XMLUtils.newDocument();
        assert(doc instanceof org.w3c.dom.Document);
    }
    
    public void testNewDocumentInputSource()
    {
        Reader reader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(reader);
        Document doc = XMLUtils.newDocument(inputsrc);
        assert(doc instanceof org.w3c.dom.Document);
    }

    public void testNewDocumentInputStream()
    {
        InputStream iostream = (InputStream)this.getTestXml("inputstream");
        InputSource inputsrc = new InputSource(iostream);
        Document doc = XMLUtils.newDocument(inputsrc);
        assert(doc instanceof org.w3c.dom.Document);
    }
   
    /* This test will fail unless you are connected to the Web, so just skip
    * it unless you really want to test it.  When not connected to the Web you
    * will get an UnknownHostException.
    */
    /*
    public void testNewDocumentURI()
    {
        String uri = "http://java.sun.com/j2ee/dtds/web-app_2.2.dtd";
        Document doc = XMLUtils.newDocument(uri);
        assert(doc instanceof org.w3c.dom.Document);
    }
    */

    public void testDocumentToString()
    {
        Reader reader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(reader);
        Document doc = XMLUtils.newDocument(inputsrc);
        
        String xmlString = (String)this.getTestXml("string");
        String result = XMLUtils.DocumentToString(doc);
        assertEquals(xmlString, result);
    }
    
    /**
    * This test method is somewhat complex, but it solves a problem people have
    * asked me about, which is how to unit test a method that has void return
    * type but writes its output to an output stream.  So half the reason for
    * creating and using it here is as a reference point.  <i>Note</i>, the
    * XMLUtils class does not omit the XML declaration when it does
    * ElementToStream, which is different behavior from its toString() method.
    * Might mention that to te developers.
    */
    public void testElementToStream() throws IOException
    {
        /* Get the Document and one of its elements. */ 
        Reader xmlReader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(xmlReader);
        Document doc = XMLUtils.newDocument(inputsrc);
        NodeList nl = doc.getElementsByTagName("display-name");
        Element elem = (Element)nl.item(0);
        String expected = "<display-name>Apache-Axis</display-name>";
       
        /*
        * Create a PipedOutputStream to get the output from the tested method. 
        * Pass the PipedOutputStream to the ConsumerPipe's constructor, which 
        * will create a PipedInputStream in a separate thread.
        */
        PipedOutputStream out = new PipedOutputStream();
        ConsumerPipe cpipe = new ConsumerPipe(out);
        
        /*
        * Call the method under test, passing the PipedOutStream to trap the
        * results.
        */
        XMLUtils.ElementToStream(elem, out);

        /*
        * The output of the test will be piped to ConsumerPipe's PipedInputStream, which
        * is used to read the bytes of the stream into an array.  It then creates a
        * String for comparison from that byte array.
        */
        out.flush();
        String result = cpipe.getResult();
        //don't forget to close this end of the pipe (ConsumerPipe closes the other end).
        out.close();
        
        /* 
        * Unlike toString(), the XML declaration is not ommitted so we check to
        * make sure it's there.
        */
        assert(result.startsWith("<?xml version=")); 
        assert(result.endsWith(expected));
    }
    
    /**
    * For explanation of the methodology used to test this method, see notes in
    * previous test method.
    */ 
    public void testDocumentToStream() throws IOException
    {
        Reader reader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(reader);
        Document doc = XMLUtils.newDocument(inputsrc);
        
        PipedOutputStream out = new PipedOutputStream();
        ConsumerPipe cpipe = new ConsumerPipe(out);
       
        XMLUtils.DocumentToStream(doc, out);
        out.flush();
        String result = cpipe.getResult();
        out.close();
        
        String expected = (String)this.getTestXml("string");
        assertEquals(expected, result);
    }

    public void testElementToString()
    {
        Reader reader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(reader);
        Document doc = XMLUtils.newDocument(inputsrc);
        
        NodeList nl = doc.getElementsByTagName("display-name");
        Element elem = (Element)nl.item(0);
        String expected = "<display-name>Apache-Axis</display-name>";
        String result = XMLUtils.ElementToString(elem);
        assertEquals("display-name", elem.getTagName());
        assertEquals(expected, result);
    }
    
    public void testGetInnerXMLString()
    {
        Reader reader = (Reader)this.getTestXml("reader");
        InputSource inputsrc = new InputSource(reader);
        Document doc = XMLUtils.newDocument(inputsrc);
        
        NodeList nl = doc.getElementsByTagName("display-name");
        Element elem = (Element)nl.item(0);
        String expected = "Apache-Axis";
        String result = XMLUtils.getInnerXMLString(elem);
        assertEquals(expected, result);
    }
        
    public void testGetPrefix()
    {
        Document doc = XMLUtils.newDocument();
       
        Element elem = doc.createElement("svg"); 
        elem.setAttribute("xmlns:svg", "\"http://www.w3.org/2000/svg\""); 
        elem.setAttribute("xmlns:xlink", "\"http://www.w3.org/1999/xlink\""); 
        elem.setAttribute("xmlns:xhtml", "\"http://www.w3.org/1999/xhtml\""); 
        
        String expected = "svg";
        String result = XMLUtils.getPrefix("\"http://www.w3.org/2000/svg\"", elem);
        assertEquals(expected, result);
        expected = "xlink";
        result = XMLUtils.getPrefix("\"http://www.w3.org/1999/xlink\"", elem);
        assertEquals(expected, result);
        expected = "xhtml";
        result = XMLUtils.getPrefix("\"http://www.w3.org/1999/xhtml\"", elem);
        assertEquals(expected, result);
    }
    
    public void testGetNamespace()
    {
        Document doc = XMLUtils.newDocument();
       
        Element elem = doc.createElement("svg"); 
        elem.setAttribute("xmlns:svg", "\"http://www.w3.org/2000/svg\""); 
        
        String expected = "\"http://www.w3.org/2000/svg\"";
        String result = XMLUtils.getNamespace("svg", elem);
        assertEquals(expected, result);
    }
    
    /**
    * This is a utility method for creating XML document input sources for this
    * JUnit test class.  The returned Object should be cast to the type you
    * request via the gimme parameter.
    *
    * @param gimme A String specifying the underlying type you want the XML
    * input source returned as; one of "string", "reader", or "inputstream."
    */
    public Object getTestXml(String gimme)
    {
        StringBuffer sb = new StringBuffer();
          sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        //The System ID will cause an unknown host exception unless you are
        //connected to the Internet, so comment it out for testing.
          //.append("<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN\"\n")
          //.append("\"http://java.sun.com/j2ee/dtds/web-app_2.2.dtd\">\n")
          .append("<web-app>\n")
          .append("<display-name>Apache-Axis</display-name>\n") 
          .append("<servlet>\n") 
          .append("<servlet-name>AxisServlet</servlet-name>\n") 
          .append("<display-name>Apache-Axis Servlet</display-name>\n") 
          .append("<servlet-class>\n") 
          .append("org.apache.axis.transport.http.AxisServlet\n") 
          .append("</servlet-class>\n") 
          .append("</servlet>\n") 
          .append("<servlet-mapping>\n") 
          .append("<servlet-name>AxisServlet</servlet-name>\n") 
          .append("<url-pattern>servlet/AxisServlet</url-pattern>\n") 
          .append("<url-pattern>*.jws</url-pattern>\n") 
          .append("</servlet-mapping>\n") 
          .append("</web-app>");

        String xmlString = sb.toString();
        
        if (gimme.equals("string"))
        {
            return xmlString;
        }
        else if (gimme.equals("reader"))
        { 
            StringReader strReader = new StringReader(xmlString);
            return strReader;
        }
        else if (gimme.equals("inputstream"))
        {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(xmlString.getBytes());
            return byteStream;
        }
        else return null;
    }
}
