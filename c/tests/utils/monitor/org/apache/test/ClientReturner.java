// Copyright 2003-2004 The Apache Software Foundation.
// (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//        http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.test;

import java.io.*;
import java.net.*;

/**
 * TestClientThread is a child thread of TestClientListener and handles all
 * communication between the original requestor and the TCPMonitor class.
 * 
 * @author Andrew Perry, hawkeye
 * @since 1.0
 * @see TestClientListener
 */

public class ClientReturner extends Thread
{
    boolean                  continueToRun        =true;
    private static int       number               =0;

    // the response from the server
    protected BufferedReader serverResponseStream =null;

    // the writer back to the client
    protected BufferedWriter streamToClient       =null;

    private static final int READ_BUFFER_SIZE     =4091; // 4k

    protected ClientReturner(Socket clientSocket) throws IOException
    {
        number++;
        streamToClient=new BufferedWriter(new OutputStreamWriter(clientSocket
                .getOutputStream( )));
    }

    /**
     * @param clientSocket the socket to the client
     * @param serviceSocket the socket to the server (service)
     * @throws IOException
     */
    public ClientReturner(Socket clientSocket, Socket serviceSocket)
            throws IOException
    {
        this(clientSocket);
        //        System.out.println( "ClientReturner(): entry");
        // create the reader from the server
        serverResponseStream=new BufferedReader(new InputStreamReader(
                serviceSocket.getInputStream( )));

    }

    /**
     * Reads the request from the client and if of a valid format will extract
     * the test ID and required data and call the TestSingleton class to set or
     * get the information. It is assumed that all requests are UTF Strings.
     * <p>
     * If the incoming request does not contain a test ID, or is not of a
     * recognised format then the socket will be closed and this object will
     * finish.
     * </p>
     */
    public void run( )
    {
        //        System.out.println( "ClientReturner#run("+number+"): entry");
        int bytesRead=0;
        char[] readBuffer=new char[READ_BUFFER_SIZE];

        try
        {
            while (continueToRun)
            {
                bytesRead=serverResponseStream.read(readBuffer, 0,
                        READ_BUFFER_SIZE);
                //                System.out.println( "Clientreturner got some bytes from the
                // server "+bytesRead);
                if (bytesRead!=-1)
                {
                    /*System.out.println("ClientReturner#run("+number
                            +"): Writing to client: "
                            +new String(readBuffer, 0, bytesRead));
                            */
                    try
                    {
                        streamToClient.write(readBuffer, 0, bytesRead);
                        streamToClient.flush( );
                    }
                    catch (IOException exception)
                    {
                        System.err
                                .println("IOException when writing server response back to client");
                        exception.printStackTrace(System.err);

                    }
                    // System.out.println("ClientReturner#run(): flushed");
                    TCPMonitor.getInstance( ).writeResponse(readBuffer,
                            bytesRead);
                    System.out.println("About to go around again");
                }
                else
                {
                    //                    System.out.println( "ClientFileReturner#run():
                    // bytesRead==-1
                    // "+continueToRun);

                    //continueToRun=false;
                }
            }
        }
        catch (IOException exception)
        {
            System.err
                    .println("IOException when reading in response from server ");
            exception.printStackTrace(System.err);
        }
    }
    //        System.out.println( "ClientReturner#run(): exit");

}

