/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.axis.attachments;
import org.apache.axis.transport.http.HTTPConstants; 
import org.apache.axis.attachments.ManagedMemoryDataSource;
import javax.activation.DataHandler;
import org.apache.axis.Part;
import javax.mail.internet.MimeUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Rick Rineholt 
 */

 /** This simulates the multipart stream 
  *
  */
public class MultiPartRelatedInputStream extends java.io.FilterInputStream {
    public static final String MIME_MULTIPART_RELATED = "multipart/related";
    protected java.util.HashMap parts =  new java.util.HashMap();
    protected java.util.LinkedList orderedParts =  new java.util.LinkedList();
    protected int rootPartLength = 0;
    protected boolean closed = false; //If true the stream has been closed.
    protected boolean eos = false;  //This is set once the SOAP packet has reached the end of stream.
    protected java.io.InputStream is = null; //The orginal multipart/related stream.
    //This stream controls and manages the  boundary.
    protected org.apache.axis.attachments.BoundaryDelimitedStream boundaryDelimitedStream = null;
    protected java.io.InputStream soapStream = null; //Set the soap stream once found.
    protected java.io.InputStream soapStreamBDS= null; //Set to the boundary delimited stream assoc. with soap stream once found.
    protected byte[] boundary = null;
    protected java.io.ByteArrayInputStream cachedSOAPEnvelope = null; //Caches the soap stream if it is
              //Still open and a reference to read data in a later attachment occurs.
    protected String contentLocation= null;          
    protected String contentId= null;

    static Log log =
            LogFactory.getLog(MultiPartRelatedInputStream.class.getName());

    /**
     * Multipart stream.
     * @param the string that holds the contentType
     * @param is the true input stream from where the source.
     */
    public MultiPartRelatedInputStream ( String contentType, java.io.InputStream is) throws org.apache.axis.AxisFault {
        super (is);
        try {
            this.is = is;
            //First find the start and boundary parameters. There are real weird rules regard what
            // can be in real headers what needs to be escaped etc  let mail parse it.
            javax.mail.internet.ContentType ct = new javax.mail.internet.ContentType(contentType);

            String boundaryStr = "--" + ct.getParameter("boundary"); //The boundary with -- add as always the case.

            String rootPartContentId = ct.getParameter("start"); //Get the root part content.

            if (rootPartContentId != null) {
                rootPartContentId = rootPartContentId.trim();
                if (rootPartContentId.startsWith("<")) rootPartContentId = rootPartContentId.substring(1);
                if (rootPartContentId.endsWith(">")) rootPartContentId = rootPartContentId.substring(0, rootPartContentId.length() - 1);
                if (!rootPartContentId.startsWith("cid:")) rootPartContentId = "cid:" + rootPartContentId;
            }

            //if start is null then the first attachment is the rootpart
            //First read the start boundary -- this is done with brute force since the servlet may swallow the crlf between headers.
            // after this we use the more efficient boundarydelimeted stream.  There should never be any data here anyway.
            byte[][] boundaryMarker = new byte[2][boundaryStr.length() + 2];

            is.read(boundaryMarker[0]);

            boundary = (boundaryStr + "\r\n").getBytes("US-ASCII");
            int current = 0;
            //This just goes brute force one byte at a time to find the first boundary.  
            // in most cases this just a crlf.
            for (boolean found = false; !found; ++current) {
                if (!(found = java.util.Arrays.equals(boundaryMarker[current & 0x1], boundary))) {
                    System.arraycopy(boundaryMarker[current & 0x1], 1, boundaryMarker[(current + 1) & 0x1], 0, boundaryMarker[0].length - 1);
                    if ( is.read(boundaryMarker[(current + 1) & 0x1], boundaryMarker[0].length - 1, 1) < 1) {
                        throw new org.apache.axis.AxisFault( "Error in MIME data stream start boundary not found expected:\"" +
                                new String(boundary) );
                    }
                }
            }

            //after the first boundary each boundary will have a cr lf at the beginning since after the data in any part there
            // is a cr lf added to put the boundary at the begining of a line.
            boundaryStr = "\r\n"  + boundaryStr;
            boundary = boundaryStr.getBytes("US-ASCII");

            //create the boundary delmited stream.
            boundaryDelimitedStream = new org.apache.axis.attachments.BoundaryDelimitedStream( is, boundary, 1024);

            //Now read through all potential streams until we have found the root part.
            String contentTransferEncoding = null;
            do {
                contentId = null;
                contentTransferEncoding = null;
                //Read this attachments headers from the stream.  
                javax.mail.internet.InternetHeaders headers = new javax.mail.internet.InternetHeaders(boundaryDelimitedStream);
                //Use java mail utility to read through the headers.
                contentId = headers.getHeader(HTTPConstants.HEADER_CONTENT_ID, null);
                //Clean up the headers and remove any < >
                if (contentId != null) {
                    contentId = contentId.trim();
                    if (contentId.startsWith("<")) contentId = contentId.substring(1);
                    if (contentId.endsWith(">")) contentId = contentId.substring(0, contentId.length() - 1);
                    contentId = contentId.trim();
                    if (!contentId.startsWith("cid:")) contentId = "cid:" + contentId; //make sure its identified as cid
                }

                contentLocation = headers.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION, null);
                if (contentLocation != null) {
                    contentLocation = contentLocation.trim();
                    if (contentLocation.startsWith("<")) contentLocation = contentLocation.substring(1);
                    if (contentLocation.endsWith(">")) contentLocation = contentLocation.substring(0, contentLocation.length() - 1);
                    contentLocation = contentLocation.trim();
                }
                contentType = headers.getHeader(HTTPConstants.HEADER_CONTENT_TYPE, null);
                if (contentType != null) contentType = contentType.trim();

                contentTransferEncoding = headers.getHeader(HTTPConstants.HEADER_CONTENT_TRANSFER_ENCODING, null);
                if(contentTransferEncoding != null ) contentTransferEncoding = contentTransferEncoding.trim();

                java.io.InputStream decodedStream=  boundaryDelimitedStream;
                if(contentTransferEncoding != null && 0 != contentTransferEncoding.length()){
                    decodedStream= MimeUtility.decode(decodedStream, contentTransferEncoding);
                }

                if (rootPartContentId != null && !rootPartContentId.equals( contentId)) { //This is a part that has come in prior to the root part. Need to buffer it up.
                    javax.activation.DataHandler dh = new javax.activation.DataHandler(new org.apache.axis.attachments.ManagedMemoryDataSource(decodedStream,
                   16 * 1024, contentType, true));

                    AttachmentPart ap= new AttachmentPart(dh);
                    if(contentId != null) 
                        ap.addMimeHeader(HTTPConstants.HEADER_CONTENT_ID, contentId); 

                    if (contentLocation != null)
                        ap.addMimeHeader(HTTPConstants.HEADER_CONTENT_LOCATION, contentLocation); 
                    

                    for( java.util.Enumeration en= headers.getNonMatchingHeaders(
                            new String[]{HTTPConstants.HEADER_CONTENT_ID,
                             HTTPConstants.HEADER_CONTENT_LOCATION,
                             HTTPConstants.HEADER_CONTENT_TYPE }); en.hasMoreElements(); ){
                             javax.mail.Header header= (javax.mail.Header) en.nextElement();
                             String name= header.getName();
                             String value= header.getValue();
                             if(name != null && value != null){
                                 name= name.trim();
                                 if(name.length() != 0)
                                     ap.addMimeHeader(name , value); 
                             }
                    }         

                    addPart(contentId, contentLocation, ap);
                    boundaryDelimitedStream = boundaryDelimitedStream.getNextStream(); //Gets the next stream.
                }

            }
            while ( null != boundaryDelimitedStream &&  rootPartContentId != null && !rootPartContentId.equals( contentId) );

            if (boundaryDelimitedStream  == null ) {
                throw new org.apache.axis.AxisFault( "Root part containing SOAP envelope not found.  contentId=" + rootPartContentId);
            }

            soapStreamBDS= boundaryDelimitedStream;
            if(contentTransferEncoding != null && 0 != contentTransferEncoding.length()){
                    soapStream = MimeUtility.decode(boundaryDelimitedStream, contentTransferEncoding);
            }else soapStream = boundaryDelimitedStream; //This should be the SOAP part

        //Read from the input stream all attachments prior to the root part.
        }
        catch (javax.mail.internet.ParseException e) {
            throw new org.apache.axis.AxisFault( "Error in parsing mime data stream " + e.getMessage());
        }
        catch ( java.io.IOException e) {
            throw new org.apache.axis.AxisFault( "Error in reading data stream " + e.getMessage());
        }
        catch ( javax.mail.MessagingException e) {
            throw new org.apache.axis.AxisFault( "Error in reading data stream " + e.getMessage());
        }
    }

    public Part getAttachmentByReference( final String[] id ) throws org.apache.axis.AxisFault {  // if CID should still have CID: prefix.  
        //First see if we have read it in yet.
        Part ret = null; 
        for(int i= id.length -1; ret== null && i > -1; --i){
             ret=(AttachmentPart) parts.get(id[i]);
        }

        if ( null == ret) {
            ret = readTillFound(id);
        }
        log.debug("getAttachmentByReference(\"" + id + "\") returns" + (ret == null? "null" : ret.toString()));
        return ret;
    }

    protected void addPart(String contentId, String locationId, AttachmentPart  ap) {
        if (contentId != null && contentId.trim().length() != 0) parts.put(contentId, ap);
        if (locationId != null && locationId.trim().length() != 0)parts.put(locationId, ap);
        orderedParts.add(ap); 
    }

    protected final static String[] READ_ALL= { " * \0 ".intern()}; //Shouldn't never match

    protected void readAll( ) throws org.apache.axis.AxisFault {
       readTillFound(READ_ALL ); 
    }

    public java.util.Collection getAttachments() throws org.apache.axis.AxisFault {
       readAll();
       return orderedParts; 
    }
    /** 
     * This will read streams in till the one that is needed is found.
     * @param The id is the stream being sought. TODO today its only handles CID. all ContentId streams
     *         should be prefixed by "cid:"
     */

    protected Part readTillFound( final String[] id) throws org.apache.axis.AxisFault {
        if (boundaryDelimitedStream == null) return null; //The whole stream has been consumed already
        Part ret = null;

        try {
            if ( soapStreamBDS == boundaryDelimitedStream ) { //Still on the SOAP stream.
                if (!eos) { //The SOAP packet has not been fully read yet. Need to store it away.

                    java.io.ByteArrayOutputStream soapdata = new java.io.ByteArrayOutputStream(1024 * 8);
                    byte[] buf = new byte[1024 * 16];
                    int byteread = 0;

                    do {
                        byteread = soapStream.read(buf);
                        if (byteread > 0) soapdata.write(buf, 0, byteread);
                    }
                    while (byteread > -1);
                    soapdata.close();
                    soapStream = new java.io.ByteArrayInputStream( soapdata.toByteArray());
                }
                boundaryDelimitedStream = boundaryDelimitedStream.getNextStream();
            }
               //Now start searching for the data.

            do {
                String contentType = null;
                String contentId = null;
                String contentTransferEncoding = null;
                String contentLocation = null;

                //Read this attachments headers from the stream.  
                javax.mail.internet.InternetHeaders headers = new javax.mail.internet.InternetHeaders(boundaryDelimitedStream);

                contentId = headers.getHeader("Content-ID", null);
                if (contentId != null) {
                    contentId = contentId.trim();
                    if (contentId.startsWith("<")) contentId = contentId.substring(1);
                    if (contentId.endsWith(">")) contentId = contentId.substring(0, contentId.length() - 1);
                    if (!contentId.startsWith("cid:")) contentId = "cid:" + contentId;
                    contentId = contentId.trim();
                }
                contentType = headers.getHeader(HTTPConstants.HEADER_CONTENT_TYPE, null);
                if (contentType != null) contentType = contentType.trim();
                contentLocation = headers.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION, null);
                if (contentLocation != null) contentLocation = contentLocation.trim();
                contentTransferEncoding = headers.getHeader(HTTPConstants.HEADER_CONTENT_TRANSFER_ENCODING , null);
                if (contentTransferEncoding != null ) contentTransferEncoding = contentTransferEncoding.trim();

                java.io.InputStream decodedStream=  boundaryDelimitedStream;
                if(contentTransferEncoding != null && 0 != contentTransferEncoding.length()){
                    decodedStream= MimeUtility.decode(decodedStream, contentTransferEncoding);
                }

                DataHandler dh= new DataHandler(new ManagedMemoryDataSource(decodedStream, 1024, contentType, true));

                AttachmentPart ap= new AttachmentPart(dh);
                if(contentId != null) 
                    ap.addMimeHeader(HTTPConstants.HEADER_CONTENT_ID, contentId); 

                if (contentLocation != null)
                    ap.addMimeHeader(HTTPConstants.HEADER_CONTENT_LOCATION, contentLocation); 
                

                for( java.util.Enumeration en= headers.getNonMatchingHeaders(
                        new String[]{HTTPConstants.HEADER_CONTENT_ID,
                         HTTPConstants.HEADER_CONTENT_LOCATION,
                         HTTPConstants.HEADER_CONTENT_TYPE }); en.hasMoreElements(); ){
                         javax.mail.Header header= (javax.mail.Header) en.nextElement();
                         String name= header.getName();
                         String value= header.getValue();
                         if(name != null && value != null){
                             name= name.trim();
                             if(name.length() != 0)
                                 ap.addMimeHeader(name , value); 
                         }
                }         

                addPart(contentId, contentLocation, ap);

                for(int i= id.length -1; ret== null && i > -1; --i){
                    if (contentId != null && id[i].equals( contentId)) { //This is the part being sought
                        ret = ap;
                    }else if(contentLocation != null && id[i].equals( contentLocation)){
                        ret = ap;
                    }
                }

                boundaryDelimitedStream = boundaryDelimitedStream.getNextStream();

            }
            while (null == ret && null != boundaryDelimitedStream);
        }
        catch (Exception e) {
            throw org.apache.axis.AxisFault.makeFault(e);
        }

        return ret;
    }

    /**
     * Return the content location. 
     * @return the Content-Location of the stream.
     *   Null if no content-location specified.
     */
    public String getContentLocation(){
        return contentLocation;
    }

    /**
     * Return the content id of the stream 
     * @return the Content-Location of the stream.
     *   Null if no content-location specified.
     */
    public String getContentId(){
        return contentId;
    }

    /**
     * Read the root stream. 
     */

    public int read(byte[] b, int off, int len) throws java.io.IOException {
        if (closed) throw new java.io.IOException("Stream closed.");
        if (eos) return -1;
        int read = soapStream.read(b, off, len);

        if (read < 0) eos = true;
        return read;
    }

    public int read(byte[] b) throws java.io.IOException {
        return read(b, 0, b.length);
    }

    public int read() throws java.io.IOException {
        if (closed) throw new java.io.IOException("Stream closed.");
        if (eos) return -1;
        int ret = soapStream.read();

        if ( ret < 0) eos = true;
        return ret;
    }

    public void close() throws java.io.IOException {
        closed = true;
        soapStream.close();
    }
}
