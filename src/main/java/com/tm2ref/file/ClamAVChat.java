package com.tm2ref.file;


import com.tm2ref.service.LogService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import java.io.InputStream;

        /**
         * Connects to a clamd daemon, chats with it to pass it the email data and
         * a response of virus or not in it.
         * @author Jean-Francois POUX
         */
        public class ClamAVChat
        {
            private static final boolean DEBUG = false;

            private String host;
            private int port;
            private byte[] toScan = null;

            private InputStream iss = null;

            private Socket chat = null;
            private Socket data = null;
            private String virus = "";

            private int connectionTimeout = 90;

            public ClamAVChat(String host, int port, byte[] toScan, int connectionTimeout)
            {
                this.host = host;

                this.port = port;

                this.toScan = toScan;

                if( connectionTimeout > 0 )
                    this.connectionTimeout = connectionTimeout;
            }

            public ClamAVChat(String host, int port, InputStream strm, int connectionTimeout)
            {
                this.host = host;

                this.port = port;

                this.iss = strm;

                if( connectionTimeout > 0 )
                    this.connectionTimeout = connectionTimeout;
            }



            /**
             * check for viruses
             * @return true if none found, false if found
             */
            public boolean doScan()
            {
                chat = new Socket();

                SocketAddress sockaddr = new InetSocketAddress(host, port);

                try
                {
                    chat.setSoTimeout(connectionTimeout * 1000);
                }

                catch (SocketException e1)
                {
                    LogService.logIt( e1 , "ClamAVChat.doScan() Socket Exception Setting Timeout 1.  Returning TRUE since apparently ClamAV is having problems."  );
                    return true;
                }

                String responseValue = "";

                try
                {
                    //First, try to connect to the clamd
                    chat.connect(sockaddr);

                    byte[] b = { 'S', 'T', 'R', 'E', 'A', 'M', '\n' };

                    chat.getOutputStream().write(b); // Write the initialisation command

                    // Now, read byte per byte until we find a LF.
                    byte[] rec = new byte[1];

                    while (true)
                    {
                        chat.getInputStream().read(rec);

                        if (rec[0] == '\n')
                            break;

                        responseValue += new String(rec);
                    }

                    if( DEBUG )
                        LogService.logIt( "ClamAVChat.doScan() response: " + responseValue);

                    // In the response value, there's an integer. It's the TCP port that the clamd has allocated for us for data stream.
                    int dataPort = -1;

                    if (responseValue.contains(" "))
                        dataPort = Integer.parseInt(responseValue.split(" ")[1]);

                    // Now, we connect to the data port obtained before.
                    data = new Socket();

                    SocketAddress sockaddrData = new InetSocketAddress(host, dataPort);

                    try
                    {
                        data.setSoTimeout(connectionTimeout * 1000); // we leave 1m30 before closing connection is clamd does not issue a response.
                    }

                    catch (SocketException e1)
                    {
                        LogService.logIt( e1 , "ClamAVChat.doScan() Socket Exception Setting Timeout 2.  Returning TRUE since apparently ClamAV is having problems." );
                        return true;
                    }

                    try
                    {
                        data.connect(sockaddrData);

                        if( iss != null )
                        {
                            int n;

                            byte[] buffer = new byte[1024];

                            while((n = iss.read(buffer)) > -1)
                            {
                                 data.getOutputStream().write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
                            }
                        }

                        else
                        {
                            data.getOutputStream().write(toScan); // We write to the data stream the content of the mail
                        }

                        data.close(); // Then close the stream, so that clamd knows it's the end of the stream.
                    }
                    catch (IOException e2)
                    {
                        LogService.logIt( "ClamAVChat.doScan() connecting via socket. Returning TRUE since apparently ClamAV is having problems. " + e2.toString() );
                        return true;
                    }

                    // Now that's we have send the body of the mail to clamd, we wait for the response on the chat stream.
                    responseValue = "";

                    while (true)
                    {
                        try
                        {
                            chat.getInputStream().read(rec);
                        }

                        catch (IOException e3)
                        {
                            LogService.logIt( e3 , "ClamAVChat.doScan() getting inputStream.  Returning TRUE since apparently ClamAV is having problems. " );
                            return true;
                            // break;
                        }

                        if (rec[0] == '\n')
                            break;

                        responseValue += new String(rec);
                    }

                    if( DEBUG )
                        LogService.logIt( "ClamAVChat.doScan() response: " + responseValue);

                }

                catch (IOException e)
                {
                    LogService.logIt( e , "ClamAVChat.doScan() IO Error. Returning TRUE since ClamAV is apparently not available. : " + e.getMessage());                    
                    return true;
                }

                finally
                {
                    if (data != null)
                    {
                        try
                        {
                            data.close();
                        } catch (IOException e3)
                        {
                            LogService.logIt( e3 , "ClamAVChat.doScan() Finally block, closing data " );
                        }
                    }

                    if (chat != null)
                    {
                        try
                        {
                            chat.close();
                        }

                        catch (IOException e3)
                        {
                            LogService.logIt( e3 , "ClamAVChat.doScan() Finally block, closing chat " );
                        }
                    }
                }

                //if (responseValue == null)
                //{
                //    if( DEBUG )
                //        LogService.logIt( "ERROR: ClamAVChat.doScan() response is null. THERE MUST HAVE BEEN AN ERROR IN DOING THE VIRUS SCAN!!! Passing anyway...");

                //    return true;
                //}

                if (responseValue.contains("ERROR"))
                {
                    if( DEBUG )
                        LogService.logIt( "ClamAVChat.doScan() response is erroneous (" + responseValue
                                           + "). Passing anyway...");
                }

                if (responseValue.equals("stream: OK")) // clamd writes this if the stream we sent does not contains viruses.
                    return true;

                virus = responseValue; // Else there is an error, the response contains the name of the identified virus

                return false;

            }

            public String getVirus()
            {
                return virus;
            }
        }
