#include "HTTPChannel.hpp"

HTTPChannel::HTTPChannel()
{
	memset( &m_URL, 0, sizeof( URL));

	m_LastError = "No Errors";

	m_Sock = INVALID_SOCKET;

	m_bUseProxy = false;
    m_strProxyHost = "";
    m_uiProxyPort = 0;

#ifdef WIN32
	m_lTimeoutSeconds = 10;
#else
	m_lTimeoutSeconds = 0;
#endif

	if( !StartSockets())
	{
		throw HTTPTransportException( SERVER_TRANSPORT_CHANNEL_INIT_ERROR);
	}
}

HTTPChannel::~HTTPChannel()
{
// If the socket value is not invalid, then close the socket before
// deleting the Channel object.
	if( m_Sock != INVALID_SOCKET)
	{
		CloseChannel();
	}

	StopSockets();
}

const char * HTTPChannel::getURL()
{
// Return the URL currently assicated with the channel object.
    return m_URL.getURL();
}

void HTTPChannel::setURL( const char * cpURL)
{
// Set the Channel URL to the new value.
    m_URL.setURL( cpURL);
}

URL & HTTPChannel::getURLObject()
{
    return m_URL;
}

bool HTTPChannel::open() throw (HTTPTransportException&)
{
	bool	bSuccess = (bool) AXIS_FAIL;

    if( m_Sock != INVALID_SOCKET)
	{
		CloseChannel();
	}

	if( (bSuccess = OpenChannel()) != AXIS_SUCCESS)
	{
		throw HTTPTransportException( SERVER_TRANSPORT_SOCKET_CONNECT_ERROR,
									  (char *) m_LastError.c_str());
	}

	return bSuccess;
}

bool HTTPChannel::close()
{
	if( m_Sock != INVALID_SOCKET)
	{
		CloseChannel();
	}

	m_Sock = INVALID_SOCKET;

	return AXIS_SUCCESS;
}

const std::string & HTTPChannel::GetLastErrorMsg()
{
	return m_LastError;
}

const IChannel & HTTPChannel::operator >> (const char * msg)
{
    if (INVALID_SOCKET == m_Sock)
    {
// Error - Reading cannot be done without having a open socket Input
//         streaming error on undefined channel; please open the
//		   channel first

		m_LastError = "No open socket to read from.";

		throw HTTPTransportException( SERVER_TRANSPORT_INVALID_SOCKET,
									  (char *) m_LastError.c_str());
    }

    int		nByteRecv = 0;
    char	buf[BUF_SIZE];
	int		iBufSize = BUF_SIZE - 1;

    //assume timeout not set; set default tatus to OK
    int iTimeoutStatus = 1;

    //check if timeout set
    if( m_lTimeoutSeconds)
	{
        iTimeoutStatus = applyTimeout();
	}

    // Handle timeout outcome
    if( iTimeoutStatus < 0)
    {
		// Error
		m_LastError = "Channel error while waiting for timeout";

        // Select SOCKET_ERROR. Channel error while waiting for timeout
        throw HTTPTransportException( SERVER_TRANSPORT_TIMEOUT_EXCEPTION, 
									  (char *) m_LastError.c_str());
    }

    if( iTimeoutStatus == 0)
    {
		// Timeout expired - select timeout expired.
        // Channel error connection timeout before receving
		m_LastError = "Channel error: connection timed out before receving";

        throw HTTPTransportException( SERVER_TRANSPORT_TIMEOUT_EXPIRED, 
									  (char *) m_LastError.c_str());
    }

    // Either timeout was not set or data available before timeout; so read

    if( (nByteRecv = recv( m_Sock, (char *) &buf, iBufSize, 0)) == SOCKET_ERROR)
    {
		// Recv SOCKET_ERROR, Channel error while getting data
		m_LastError = "Channel error while getting data";

		/*CloseChannel();*/

		throw HTTPTransportException( SERVER_TRANSPORT_INPUT_STREAMING_ERROR, 
									  (char *) m_LastError.c_str());
    }

    if( nByteRecv)
    {
		/* printf("nByteRecv:%d\n", nByteRecv); */
		buf[nByteRecv] = '\0';
		/* got a part of the message, so add to form */
		memcpy( (void *) msg, buf, nByteRecv);
    }

	return *this;
}

const IChannel & HTTPChannel::operator << (const char* msg)
{
// Check that the Tx/Rx sockets are valid (this will have been done if the
// application has called the open method first.
    if( INVALID_SOCKET == m_Sock)
    {
// Error - Writing cannot be done without having a open socket to
//         remote end.  Throw an exception.

		m_LastError = "No valid socket open";

		throw HTTPTransportException( SERVER_TRANSPORT_INVALID_SOCKET,
									  (char *) m_LastError.c_str());
    }

    int size = strlen( msg);
	int nByteSent;

    if( (nByteSent = send( m_Sock, msg, size, MSG_DONTROUTE)) == SOCKET_ERROR)
    {
// Output streaming error while writing data.  Close the channel and
// throw an exception.
		CloseChannel();

		m_LastError = "Output streaming error while writing data";

		throw HTTPTransportException( SERVER_TRANSPORT_OUTPUT_STREAMING_ERROR,
									  (char *) m_LastError.c_str());
    }

	return *this;
}

void HTTPChannel::setTimeout( const long lSeconds)
{
    m_lTimeoutSeconds = lSeconds;
}

void HTTPChannel::setSocket( unsigned int uiNewSocket)
{
    m_Sock = uiNewSocket;
}

bool HTTPChannel::setTransportProperty( AXIS_TRANSPORT_INFORMATION_TYPE type, const char* value)
{
	bool	bSuccess = false;

	return bSuccess;
}

const char * HTTPChannel::getTransportProperty( AXIS_TRANSPORT_INFORMATION_TYPE type)
{
	return NULL;
}

void HTTPChannel::setProxy (const char *pcProxyHost, unsigned int uiProxyPort)
{
    m_strProxyHost = pcProxyHost;
    m_uiProxyPort = uiProxyPort;
    m_bUseProxy = true;
}

// +--------------------------------------------------------------------------+
// | Protected methods														  |
// | -----------------														  |
// +--------------------------------------------------------------------------+
bool HTTPChannel::OpenChannel()
{
	bool	bSuccess = (bool) AXIS_FAIL;

// Create the Client (Rx) side first.
#ifdef IPV6
    struct addrinfo		aiHints;
	struct addrinfo *	paiAddrInfo;
	struct addrinfo *	paiAddrInfo0;

    // hints is used after zero cleared
    memset( &aiHints, 0, sizeof( aiHints));

    aiHints.ai_family = PF_UNSPEC;
    aiHints.ai_socktype = SOCK_STREAM;

    char szPort[7];
   
    const char	*	pszHost = m_URL.getHostName();
    unsigned int	uiPort = m_URL.getPort();

    if( m_bUseProxy)
    {
        uiPort = m_uiProxyPort;
        pszHost = m_strProxyHost.c_str();
    }
 
    sprintf( szPort, "%hd", uiPort);

    if( getaddrinfo( pszHost, szPort, &aiHints, &paiAddrInfo0))
    {
        throw HTTPTransportException( SERVER_TRANSPORT_SOCKET_CREATE_ERROR);
    }

    for( paiAddrInfo = paiAddrInfo0; paiAddrInfo; paiAddrInfo = paiAddrInfo->ai_next)
    {
        m_Sock = socket( paiAddrInfo->ai_family, paiAddrInfo->ai_socktype, paiAddrInfo->ai_protocol);

        if( m_Sock < 0)
        {
            continue;
        }

        if( connect( m_Sock, paiAddrInfo->ai_addr, paiAddrInfo->ai_addrlen) < 0)
        {
            // Cannot open a channel to the remote end, shutting down the
            // channel and then throw an exception.
            CloseChannel();

            free( paiAddrInfo0);

            throw HTTPTransportException( SERVER_TRANSPORT_SOCKET_CONNECT_ERROR);
        }

        break;
    }

    // Samisa: free addrInfo0 - no longer needed
    freeaddrinfo( paiAddrInfo0);

    if( m_Sock < 0)
    {
        // Sockets error Couldn't create socket.  Close the channel and throw
        // an exception.
        CloseChannel();

        throw HTTPTransportException( SERVER_TRANSPORT_SOCKET_CREATE_ERROR);
    }

	bSuccess = AXIS_SUCCESS;

#else // IPV6 not defined
    if( (m_Sock = socket( PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
	{
		m_LastError = "Could not Create a socket.";

		return bSuccess;
	}

// If the transport was initilised, then create client and server sockets.
    sockaddr_in	clAddr;

	clAddr.sin_family = AF_INET;	// AF_INET (address family Internet).
	clAddr.sin_port = 0;			// No Specify Port required.
	clAddr.sin_addr.s_addr = INADDR_ANY;

// Attempt to bind the client to the client socket.
	if( bind( m_Sock, (struct sockaddr *) &clAddr, sizeof( clAddr)) == SOCKET_ERROR)
	{
// Error whilst binding. Cannot open a channel to the remote end,
// shutting down the channel and then throw an exception.
		CloseChannel();

		m_LastError = "Error whilst binding. Cannot open a channel to the remote end,";

		return bSuccess;
	}

// Although the above fragment makes use of the bind() API, it would be
// just as effective to skip over this call as there are no specific
// local port ID requirements for this client. The only advantage that
// bind() offers is the accessibility of the port which the system 
// chose via the .sin_port member of the cli_addr structure which will 
// be set upon success of the bind() call.

// Create the Server (Tx) side.

	sockaddr_in			svAddr;
	struct hostent *	pHostEntry = NULL;
	const char *		host = m_URL.getHostName();
	unsigned int		port = m_URL.getPort();

	if( m_bUseProxy)
	{
		port = m_uiProxyPort;
		host = m_strProxyHost.c_str();
	}

	svAddr.sin_family = AF_INET;
	svAddr.sin_port = htons( port);

// Probably this is the host-name of the server we are connecting to...
	if( (pHostEntry = gethostbyname( host)))
	{
		svAddr.sin_addr.s_addr = ((struct in_addr *) pHostEntry->h_addr)->s_addr;
	}
	else
	{
// No this is the IP address
		svAddr.sin_addr.s_addr = inet_addr( host);
	}

// Attempt to connect to the remote server.
	if( connect( m_Sock, (struct sockaddr *) &svAddr, sizeof (svAddr)) == SOCKET_ERROR)
	{
// Cannot open a channel to the remote end, shutting down the
// channel and then throw an exception.

#ifdef WIN32
// Before we do anything else get the last error message;
// I'd like to put the getting of the error message into platform specifics
// but not sure how!  I think it would be nicer to make the platform
// specifics a class and not just macros.  That way we could have e.g.
// char * Windows#getLastErrorMessage().
		long lLastError = GetLastError();
#endif // WIN32

		CloseChannel();

#ifdef WIN32
		char	szErrorBuffer[200]; 
	    LPVOID	lpErrorBuffer;

		FormatMessage( FORMAT_MESSAGE_ALLOCATE_BUFFER|FORMAT_MESSAGE_FROM_SYSTEM,
					   NULL,
					   lLastError,
					   MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT),
					   (LPTSTR) &lpErrorBuffer,
					   0,
					   NULL);

		sprintf( szErrorBuffer, 
				 "Failed to open connection to server:\n\
				 hostname='%s'\n\
				 port='%d'\n\
				 Error Message='%s'\
				 Error Code='%d'\n",                     \
				 m_URL.getHostName(), m_URL.getPort(), lpErrorBuffer, lLastError); 
 
	    LocalFree( lpErrorBuffer);

		m_LastError = szErrorBuffer;
#else // WIN32 not defined
		m_LastError = "Cannot open a channel to the remote end.";

#endif // WIN32
	    return bSuccess;
    }
	else
	{
		bSuccess = AXIS_SUCCESS;
	}

#endif // IPV6

    /* Turn off the Nagle algorithm - Patch by Steve Hardy */

    /* This is needed, because our TCP stack would otherwise wait at most
     * 200 ms before actually sending data to the server (while waiting for
     * a full packet). This limits performance to around 5 requests per
     * second, which is not acceptable. Turning off the Nagle algorithm
     * allows for much faster transmission of small packets, but may
     * degrade high-bandwidth transmissions.
     */

    int one = 1;

    setsockopt( m_Sock, IPPROTO_TCP, TCP_NODELAY, (char *)&one, sizeof(int));

    return bSuccess;
}

void HTTPChannel::CloseChannel()
{
    if( INVALID_SOCKET != m_Sock) // Check if socket already closed : AXISCPP-185
	{
#ifdef WIN32
		closesocket( m_Sock);
#else
		::close( m_Sock);
#endif
		m_Sock = INVALID_SOCKET; // fix for AXISCPP-185
	}
}

bool HTTPChannel::StartSockets()
{
	bool	bSuccess = false;
#ifdef WIN32
    WSADATA wsaData;	// Contains vendor-specific information, such as the
						// maximum number of sockets available and the maximum
						// datagram size.

// wsaData filled by Windows Sockets DLLs.
    if( WSAStartup( WS_VERSION_REQD, &wsaData))
    {
// Error - Could not setup underlying Windows socket transport mechanism.
		m_LastError = "WinSock DLL not responding.";
    }
    else
    {
// Query to see whether the available version matches what is required
		if ((LOBYTE( wsaData.wVersion) <  WS_VERSION_MAJOR()) ||
			(LOBYTE( wsaData.wVersion) == WS_VERSION_MAJOR() &&
			 HIBYTE( wsaData.wVersion) <  WS_VERSION_MINOR()))
		{
// Error - Underlying Windows socket transport version is not compatible with what is required.
			char 	szErrorBuffer[100];

			sprintf( szErrorBuffer, "Windows Sockets version %d.%d is not supported by winsock2.dll", LOBYTE( wsaData.wVersion), HIBYTE( wsaData.wVersion));

			m_LastError = szErrorBuffer;

			StopSockets();
		}
		else
		{
			bSuccess = true;
		}
    }
#else
    /* cout << "no need for linux" << endl; */
    /* other OS specific Intitialization goes here */
	bSuccess = true;
#endif

    return bSuccess;
}

void HTTPChannel::StopSockets()
{
	WSACleanup();
}

/**
 * Channel::applyTimeout()
 *
 * @return int 
 */
int HTTPChannel::applyTimeout()
{
    fd_set			set;
    struct timeval	timeout;

    // Initialize the file descriptor set.
    FD_ZERO( &set);
    FD_SET( m_Sock, &set);

    /* Initialize the timeout data structure. */
    timeout.tv_sec = m_lTimeoutSeconds;
    timeout.tv_usec = 0;

    /* select returns 0 if timeout, 1 if input available, -1 if error. */
    return select( FD_SETSIZE, &set, NULL, NULL, &timeout);
}
