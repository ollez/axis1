/*
 * Created on 10-Nov-2005 TODO To change the template for this generated file go
 * to Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author hawkeye TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
// We cache the entire output file in an array of char[]
public class Response
{
    private static final char CR             ='\r';
    private static final char LF             ='\n';
    public static final String CRLF          =""+CR+LF;
    public static final String DOUBLE_CRLF    =CRLF+CRLF;
    private static final String CONTENT_LENGTH ="Content-Length:";

    private char[]              message;
    private boolean             chunked=false;

    // whether the message has a "Connection: Close" http header in it.
    private boolean             hasCloseConnectionHeader=false;

    public boolean hasCloseConnectionHeader( )
    {
        return hasCloseConnectionHeader;
    }

    /**
     * Because the repositories we use accomodate the dos2unix conversion we
     * have a problem. On unix the response files have the crlf converted to
     * just cr. this method returns the crlf ! The HTTP header and chunk sizes
     * need to be delimited with CR LF's. The 'server response' expected files
     * used to mimic the response of a server when performing unit tests also
     * need to have the correct delimiting character combinations in the fake
     * response messages (generated by the monitor). Problems arise because the
     * expected response files have been captured on different operating systems
     * (Unix and Windows) and then edited using a variety of editors that
     * interpret the CR character differently. Thus the file may be stored with
     * or without the CR characters. Also, the souce control application can be
     * set to convert a single LF character into a CR LF combination. The
     * outcome of all this character manipulation is that LF's may or may not
     * have the necessary associated CR for it to be recognised by the HTTP
     * transport. It is not just sufficient to look for every LF and to prefix
     * it with a CR because the context of that LF must be taken into
     * consideration. For example, if a LF is found in the HTTP header then it
     * definitely needs a CR prefix. This also applies when the message is
     * chunked and the chunk size must also be delimited by the CR LF sequence.
     * But, when a LF appears inside a chunk of data it should remain unchanged
     * unless the chunk size is different from the actual data size between
     * chunks, in which case the CR has probably been stripped out and needs to
     * be replaced. Below is a low level design of what should work in all
     * instances... Is the message chunked? NO: Convert all single LF sequences
     * to CR+LF. NB: Because the value associated with the 'Content-Length'
     * label is not used correctly, if the size of the file grows larger than
     * the Content-Length value, no action is taken. Exit. YES: Convert all
     * single LF sequences to CR+LF in the HTTP header. Set Index = location of
     * the first 'chunk' value. Read the chunk size. While the chunk size > 0
     * Convert any single LF sequences to CR+LF that surround the chunk value.
     * Is the character at offset Index + chunk size equal to CR or LF? NO:
     * Count up the number of unaccompanied LF's in the chunk. Is the difference
     * between the expected and actual chunk data length double the number of
     * unaccompanied LF's? NO: Error - Irreconcilable differences between
     * expected and actual chunk data sizes. Exit. YES: Convert all single LF
     * sequences to CR+LF in the chunk data. Continue. Index = Index + chunk
     * size. Read the new chunk size. End of while. Exit.
     */
    public Response(String message)
    {
        this.message=message.toCharArray( );
        // parse the message to see if it has a Connection: Close in it
        if (message.toLowerCase( ).indexOf("connection: close")>-1)
            hasCloseConnectionHeader=true;
       
        // Find out if the message is chunked
        deriveIsChunked( );
        
        // emulate None OS machines when you want to test out the CRLF method
        //emulateNoneWinmachines();

        // check and correct CRLF's
        CheckAndCorrectCRLF( );
    }

    /**
     *  This method is used when we are testing out this class.
     * It converts ALL CRLF's into LF (as it might be on a none win machine)
     */
    private void emulateNoneWinmachines( )
    {
        String msgString = new String(getMessage()); 
        Pattern pattern=Pattern.compile(CRLF);
        Matcher matcher=pattern.matcher(msgString);
        
        if(matcher.find())
           msgString = matcher.replaceAll(""+LF);
        message = msgString.toCharArray();
    }

    private void deriveIsChunked( )
    {
        // simply look for the content length string
        chunked=new String(message).indexOf(CONTENT_LENGTH)==-1;
    }

    public char[] getMessage( )
    {
        return message;
    }

    public String toString( )
    {
        return new String(message);
    }

    /**
     * On platforms other than windows the CRLF's can get "helpfully" converted
     * So, check the last two digits for CRLF - if they are LFLF then this is
     * wrong and we will convert it to be correct. e.g. 0a 0a converts to 0d0a
     * 0d0a
     */
    private void CheckAndCorrectCRLF( )
    {

        String modifiedResponse="";
        if (chunked)
        {
            System.out.println("Information - Response file is chunked.");
            
            String messageString= new String(getMessage());
            modifiedResponse = correctHTTPHeaderSection(messageString);
            modifiedResponse +=(correctChunkedData(getPostHTTPHeaderData(messageString)));
            message = modifiedResponse.toCharArray();
        }
        else
        {
            if (System.getProperty("os.name").toLowerCase( ).startsWith("windows"))
                System.out.println("Windows operating system - not converting crlf's");
            else
            {
                String request=new String(getMessage( ));
                modifiedResponse = correctHTTPHeaderSection(request);
                message=modifiedResponse.toCharArray();
            }
        }
//
//        // Irrespective of platform ensure that all responses end with \r\n\r\n
//        String request=new String(getMessage( ));
//        // this pattern looks for any chars then any non whitespace followed
//        // directly by the EOF
//        Pattern pattern=Pattern.compile("(.*)(\\s)*$");
//        Matcher matcher=pattern.matcher(request);
//        StringBuffer stringBuffer=new StringBuffer("");
//        // We should only find one occurence (if any) of the sequence
//        if (matcher.find( ))
//        {
////            System.out.println("MATCHED");
//            // and replace them with the proper sentence !
//            matcher.appendReplacement(stringBuffer, "$1\r\n\r\n");
//        }
//        else
//        {
////            System.out.println("NOT MATCHED");
//            // We did not find the sequence so just tack on the grouping we
//            // need.
//            stringBuffer.append(request);
//            stringBuffer.append("\r\n\r\n");
//
//        }
//        // Now put it back into the message
//        message=stringBuffer.toString( ).toCharArray( );
    }

        /**
     * @param message2
     * @return
     */
    private String getPostHTTPHeaderData(String response)
    {
        Pattern pattern=Pattern.compile( "(^HTTP.*["+CRLF+"]*)(.*: .*["+CRLF+"]*)*(.*)");
        Matcher matcher = pattern.matcher(response);
        
        if(!matcher.find())
            System.err.println( "ERROR: Response message does not contain a correctly formatted HTTP header:" +response);
        
        return matcher.replaceAll("$3");
    }

/**
 * @param string the response message - HTTPheaders
 * @return correctly formatted (as best we can) http headers.
 */
private String correctChunkedData(String request)
{
    // A chunk Is defined by 
    // <hexnumber><potentially CR><LF><any data><potentially CR><LF><hexnumber>
    StringBuffer theCorrectedResponse = new StringBuffer();
    String theChunkStartPatternREGEX = "(\\p{XDigit}+)("+CR+"?"+LF+")";
    String theNextChunkSizeREGEX ="("+CR+"?"+LF+")"+theChunkStartPatternREGEX;
    
    Pattern chunkStartPattern=Pattern.compile(theChunkStartPatternREGEX);
    Pattern theNextChunkSizePattern = Pattern.compile(theNextChunkSizeREGEX);
    
    Matcher chunkStartMatcher = chunkStartPattern.matcher(request);
    Matcher theNextChunkSizeMatcher = theNextChunkSizePattern.matcher(request);    
    
    int chunkStart = 0;
    int chunkEnd=0;
    int chunkSize = 0;
    int nextChunkSize = -1;
    int nextChunkStart= -1;
    int nextChunkEnd= -1;
    
    String nextChunkSizeString="";
    while(chunkStartMatcher.find() && nextChunkSize!=0)
    {
        String chunkSizeString=""; 
        if(nextChunkSize==-1)
        {
            // means this is the first time round the loop so work it out
            chunkSizeString = request.substring(chunkStartMatcher.start(), chunkStartMatcher.end());
            chunkSize = Integer.parseInt(chunkSizeString.trim(), 16);
            chunkStart = chunkStartMatcher.end();
            // ensure the string has the correct crlf on it
            chunkSizeString = chunkSizeString.trim()+CRLF;
        }
        else
        {
            // we've already been round the loop and got the chunkSize
            chunkSizeString = nextChunkSizeString;
            chunkSize = nextChunkSize;
            chunkStart = nextChunkStart;
            // ensure the string has the correct crlf on it
            // this one came with a different parsing algorithm so add on crlf to *both* ends !
            chunkSizeString = CRLF+chunkSizeString.trim()+CRLF;
        }
//        System.out.println( "chunkString = "+chunkSizeString.trim());
//        System.out.println( "Chunk size = "+chunkSize);
//        System.out.println( "Chunked Start="+chunkStart);
//        System.out.println( "chunkend = "+chunkEnd);
        
        

        String chunkFromSize=null;
        if(chunkStart+chunkSize>request.length())
        {
            System.err.println( "ChunksSize String is wrong");
            System.err.println( "chunksStart = "+chunkStart);
            System.err.println( "ChunkSize = "+chunkSize);
            System.err.println( "request.length = "+request.length());
            // We can work this out though it's probably the last or only chunk so just guess !
            // do this later on so we don't duplicate work> 
            // chunkFromSize is null so we can see later on that we need to guess
        }
        else
            chunkFromSize = request.substring(chunkStart, chunkStart+chunkSize);
        
        String chunk=""; 
        int endOfThisChunk;
        // Now find the end of the chunk (and while we do that we actually find the next chunk size too !
        
        if(!theNextChunkSizeMatcher.find())
            endOfThisChunk = request.length()-1;
        else
        {
            chunkEnd = theNextChunkSizeMatcher.start();
            
            // get the nextchunkSize while we're here!
            nextChunkSizeString = request.substring(theNextChunkSizeMatcher.start(), theNextChunkSizeMatcher.end());
            nextChunkStart = theNextChunkSizeMatcher.end();
            nextChunkSize = Integer.parseInt(nextChunkSizeString.trim(), 16);
            
            endOfThisChunk = theNextChunkSizeMatcher.start();
        
        }
        chunk = request.substring(chunkStart, endOfThisChunk);
        if(chunkFromSize==null)
        {
            // we need to set this value now we have probably worked out what it is
            // this stops us from stopping but hopefully we won't fail too !
            chunkFromSize = chunk;
            chunkSize=chunk.length();
            System.err.println( "Guessing the size is "+chunkFromSize.length());
            // we now also have to take into account the chunkSize and it's \r\n
            // test it again to see whether we made it right or not
            // make sure we set the chunksize string
            if(!chunkSizeString.startsWith(""+CR))
            {
                // Then it's the beginning of the message
                chunkSizeString = ""+Integer.toHexString(chunk.length())+CRLF;
            }
            else
                chunkSizeString = CRLF+Integer.toHexString(chunk.length())+CRLF;
        }
        
        // Check that the chunk size really is the same size as it should be.
        if(chunk.length() !=chunkSize)
        {
            System.out.println( "Warning: Chunksize is not correct");
            System.out.println( "chunk.length = "+chunk.length());
            System.out.println( "ChunkSize = "+chunkSize);
            System.out.println( "chunkString "+chunkSizeString);
            // then we might be on a unix system and lost the CR of a CRLF combo
            if (System.getProperty("os.name").toLowerCase( ).startsWith(
            "windows"))
            {
                System.out.println( "Warning: Chunksize is not correct. On windows so not attempting to fix");
            }
            else
            {
                System.out.println( "Warning: Chunksize is not correct. Attempting to correct with additional \\r\\n");
            	String newChunk = chunk.replaceAll(""+LF, CRLF);
            	if(newChunk.length()+chunkSizeString.trim().length()+2 !=chunkSize)
            	{
            	    System.err.println( "Attempt to correct the chunksize failed");
            	}
            }
        }
        theCorrectedResponse.append(chunkSizeString);
        // append the chunk to the rest of the correct messages
        theCorrectedResponse.append(chunk);
        
    }
    // append the last chunk on 
    // sometimes the chunk already has a \n on it so we need to remove it
    if(theCorrectedResponse.charAt(theCorrectedResponse.length()-1) == LF)
    {
        theCorrectedResponse.deleteCharAt(theCorrectedResponse.length()-1);
    }
    theCorrectedResponse.append(CRLF+"0"+DOUBLE_CRLF);
    return theCorrectedResponse.toString();
}

//    /**
//     * This method looks through a chunked data section and ensures that the
//     * chunksizes are correct. *
//     * 
//     * @param message
//     * @return the message - corrected if necessary
//     */
//    private boolean checkBlocks(String message)
//    {
//        boolean sizesCorrect=true;
//        boolean reachedEndOfAllChunks=false;
//        int indexOfEndOfChunkSize=0;
//        while (!reachedEndOfAllChunks&&sizesCorrect)
//        {
//            // Find this chunk_size value.
//            indexOfEndOfChunkSize=message.indexOf(CRLF);
//            System.out.println("indexOfChunkSize = "+indexOfEndOfChunkSize);
//            //                System.out.println( "Message ="+message);
//            System.out.println("Message.length() = "+message.length( ));
//
//            String chunkSizeString=message.substring(0, indexOfEndOfChunkSize)
//                    .trim( );
//
//            int chunkSize=Integer.parseInt(chunkSizeString, 16);
//            System.out.println("chunkSize="+chunkSize);
//            if (chunkSize!=8192)
//            {
//                System.out.println("DEBUGGIN");
//                ;
//            }
//            if (chunkSize==0)
//            {
//                reachedEndOfAllChunks=true;
//            }
//            else
//            {
//                // move the message past the size of Chunk
//                message=message.substring(chunkSizeString.length( )
//                        +CRLF.length( ));
//
//                // check that the last two chars are CRLF
//                String lastTwoChars=message.substring(chunkSize, chunkSize+2);
//                if (!lastTwoChars.equals(CRLF))
//                {
//                    System.err
//                            .println("WARNING: Actual chunksize != declared chunksize: "
//                                    +chunkSize);
//                    sizesCorrect=false;
//                }
//                else
//                {
//                    // 	move the message on to the end of this chunk
//                    message=message.substring(chunkSize+CRLF.length( ));
//                }
//            }
//
//        }
//        return sizesCorrect;
//
//    }

    /**
     * @param sResponse
     * @param iIndex
     * @param iEOL
     */
    private static String getResponseLine(String sResponse, int iIndex, int iEOL)
    {
        String sLine=sResponse.substring(iIndex, iEOL);

        if ((!sLine.endsWith(CRLF))&&(iEOL>iIndex))
        {
            sLine=sResponse.substring(iIndex, iEOL-1)+CRLF;
        }

        return sLine;
    }
    
    private String correctHTTPHeaderSection(String response)
    {
        String returnedResponse ="";
        // Process HTTP header block. At the end of each line in
        // the header block, if there is only a LF character,
        // prefix it with a CR. The end of the HTTP header block
        // is denoted by an empty line (i.e. CR LF).
        String sLine;
        int iIndex=0;
        int iEoL=response.indexOf(LF, iIndex)+1;

        do
        {
            sLine=getResponseLine(response, iIndex, iEoL);

            returnedResponse+=sLine;

            // Get next line.
            iIndex=iEoL;

            iEoL=response.indexOf(LF, iIndex)+1;
        }
        while (iEoL>0&&!sLine.equals(CRLF));
        
        if (chunked)
            return returnedResponse;
        else
            return returnedResponse + response.substring(iIndex);
    }

//    private static boolean isStringAHexNumber(String sValue)
//    {
//        boolean bOutcome=true;
//        String sValidChars="0123456789ABCDEFabcdef";
//        int iIndex=0;
//
//        while (bOutcome&&iIndex<sValue.length( ))
//        {
//            if (sValidChars.indexOf(sValue.substring(iIndex, iIndex+1))==-1)
//            {
//                bOutcome=false;
//            }
//            else
//            {
//                iIndex++;
//            }
//        }
//
//        return bOutcome;
//    }
}
