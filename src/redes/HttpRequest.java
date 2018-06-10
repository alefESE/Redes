package redes;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {
    /** Help variables */
    final static String CRLF = "\r\n";
    final static int HTTP_PORT = 80;
    final static int MAX_OBJECT_SIZE = 100000;
    /** Store the request parameters */
    String method;
    String URI;
    String version;
    String headers = "";
    byte[] body = new byte[MAX_OBJECT_SIZE];
    /** Server and port */
    private String host;
    private int port;

    /** Create HttpRequest by reading it from the client socket */
    public HttpRequest(BufferedReader from) {
	String firstLine = "";
	try {
	    firstLine = from.readLine();
	} catch (IOException e) {
	    System.out.println("Error reading request line: " + e);
	}

	String[] tmp = firstLine.split(" ");// 'Metodo URI HTTP/versao'
	method = tmp[0];//preenchido
	URI = tmp[1];//preenchido
	version = tmp[2];//preenchido

	System.out.println("URI is: " + URI);
        
        System.out.println("Method: "+ method);
        try {
            String line = from.readLine();
            while (line.length() != 0) {
                headers += line + CRLF;
                /* We need to find host header to know which server to
                 * contact in case the request URI is not complete. */
                if (line.startsWith("Host:")) {
                    tmp = line.split(" ");
                    if (tmp[1].indexOf(':') > 0) {
                        String[] tmp2 = tmp[1].split(":");
                        host = tmp2[0];
                        port = Integer.parseInt(tmp2[1]);
                    } else {
                        host = tmp[1];
                        port = HTTP_PORT;
                    }
                }
                /* Post text/plain
                 * Handle apenas os POST de textos simples */
                if(line.startsWith("Content-Type:") || 
                        line.startsWith("Content-type")){
                    line = from.readLine();
                    if(line.equals("Content-Length:")) {
                        int length = Integer.parseInt(
                                line.substring(line.indexOf(':')));
                        int bytesRead = 0;
                        line = from.readLine();
                        while(bytesRead < length){
                                for(int i = 0; i < line.length(); i++)
                                body[i+bytesRead] = line.getBytes()[i];
                            bytesRead += line.length();
                            from.readLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from socket: " + e);
            return;
        }
	System.out.println("Host to contact is: " + host + " at port " + port);
    }

    /** Return host for which this request is intended */
    public String getHost() {
	return host;
    }

    /** Return port for server */
    public int getPort() {
	return port;
    }

    /**
     * Convert request into a string for easy re-sending.
     */
    public String toString() {
	String req = "";

	req = method + " " + URI + " " + version + CRLF;
	req += headers;
	/* This proxy does not support persistent connections */
	req += "Connection: close" + CRLF;
	req += CRLF;
	
	return req;
    }
}
