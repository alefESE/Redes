package redes;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyCache {
    /** Hash de cache (HOST, caches) */
    private static HashMap<String, HttpResponse> cache = new HashMap();
    /** Port for the proxy */
    private static int port;
    /** Socket for client connections */
    private static ServerSocket socket;

    /** Create the ProxyCache object and the socket */
    public static void init(int p) {
	port = p;
	try {
	    socket = new ServerSocket(port);
	} catch (IOException e) {
	    System.out.println("Error creating socket: " + e);
	    System.exit(-1);
	}
    }

    public static void handle(Socket client) {
	Socket server = null;
	HttpRequest request = null;
	HttpResponse response = null;

	/* Process request. If there are any exceptions, then simply
	 * return and end this request. This unfortunately means the
	 * client will hang for a while, until it timeouts. */

	/* Read request */
	try {
	    BufferedReader fromClient = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));//preenchido
	    request = new HttpRequest(fromClient);//preenchido
	} catch (IOException e) {
	    System.out.println("Error reading request from client: " + e);
	    return;
	}
        /* Cache hit! */
        if((response = cache.get(request.URI)) != null) {
            System.out.println("Cache Hit!");
            try {
                DataOutputStream toClient =
                        new DataOutputStream(client.getOutputStream());
                toClient.write(response.toString().getBytes());
                toClient.write(response.body);
                client.close();
            } catch (IOException ex) {
                System.out.println("Error writing response to client: " + ex);
            }
        } else { /* Cache miss! */
            System.out.println("Cache miss!");
            /* Send request to server */
            try {
                /* Open socket and write request to socket */
                server = new Socket(InetAddress.getByName(request.getHost())
                        , request.getPort());//preenchido
                DataOutputStream toServer = 
                        new DataOutputStream(server.getOutputStream());//preenchido
                toServer.write(request.toString().getBytes());//preechido
                if(request.method.equals("POST"))//se for post tem corpo
                    toServer.write(request.body);
            } catch (UnknownHostException e) {
                System.out.println("Unknown host: " + request.getHost());
                System.out.println(e);
                return;
            } catch (IOException e) {
                System.out.println("Error writing request to server: " + e);
                return;
            }
            /* Read response and forward it to client */
            try {
                DataInputStream fromServer = 
                        new DataInputStream(server.getInputStream());//preenchido
                response = new HttpResponse(fromServer);
                DataOutputStream toClient = 
                        new DataOutputStream(client.getOutputStream());//prenchido
                //primeiro headers
                toClient.write(response.toString().getBytes());//preenchido
                //depois corpo(se houver)
                toClient.write(response.body);//preenchido
                /* Write response to client. First headers, then body */
                client.close();
                server.close();
                /* Insert object into the cache */
                cache.put(request.URI, response);
            } catch (IOException e) {
                System.out.println("Error writing response to client: " + e);
            }
        }
    }


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	int myPort = 0;
	
	try {
	    myPort = Integer.parseInt(args[0]);
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Need port number as argument");
	    System.exit(-1);
	} catch (NumberFormatException e) {
	    System.out.println("Please give port number as integer.");
	    System.exit(-1);
	}
	
	init(myPort);

	/** Main loop. Listen for incoming connections and spawn a new
	 * thread for handling them */
	Socket client = null;
	
	while (true) {
	    try {
		client = socket.accept();//preenchido
		handle(client);
	    } catch (IOException e) {
		System.out.println("Error reading request from client: " + e);
		/* Definitely cannot continue processing this request,
		 * so skip to next iteration of while loop. */
		continue;
	    }
	}

    }
}
