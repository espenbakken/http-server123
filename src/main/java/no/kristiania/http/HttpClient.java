package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    private int statusCode;
    private  Map<String, String> responseHeaders = new HashMap<>();
    private String responseBody;
    // Constructor - det som kalles når vi sier new
    public HttpClient(final String hostname, int port, final String requestTarget) throws IOException {
        // Connect til serveren
        Socket socket = new Socket(hostname, port);

        // HTTP Request consists of request line + 0 or more request headers
        //  request line consists of verb (GET, POST, PUT) request target ("/echo?status=404"), protocol (HTTP/1.1)
        HttpMessage requestMessage = new HttpMessage("GET " + requestTarget + " HTTP/1.1");
        requestMessage.setHeader( "Host", hostname);
        requestMessage.write(socket);

        // The first line in the response is called the response line or status line
        // response line consists of protocol ("HTTP/1.1") status code (200, 404, 401, 500) and status message
        String[] responseLineParts = readLine(socket).split(" ");

        // Status code determines if it went ok (2xx) or not (4xx). In addition: 5xx: server error) 3xx
        statusCode = Integer.parseInt(responseLineParts[1]);

        //After the response line, the response had zero or more responsive headers
        String headerLine;
        while(!(headerLine = readLine(socket)).isEmpty()) {
            // Each header consists of name: value
           int colonPos = headerLine.indexOf(':');
            String headerName = headerLine.substring(0, colonPos);
            // Spaces at the beginning and end of the header value should be ignored
            String headerValue = headerLine.substring(colonPos+1).trim();

            responseHeaders.put(headerName, headerValue);

        }

        // The Content-Length header tells us how many bytes in the response follow the headers
        int contentLength = Integer.parseInt(getResponseHeader("Content-Length"));
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            body.append((char)socket.getInputStream().read());
        }
        // The next content-length bytes are called the response body
        this.responseBody = body.toString();
    }

    public static String readLine(Socket socket) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != -1){
            // Each line is terminated by CRLF (carriage return, line feed)
            // or \r\n
            if(c == '\r') {
                socket.getInputStream().read(); // Read the \n after \r
                break;
            }
            line.append((char)c);
        }
        return line.toString();
    }

    public static void main(String[] args) throws IOException {
        new HttpClient("urlecho.appspot.com", 80, "/echo?status=200&body=Hello%World!");
    }

    public Object getStatusCode() {
        return statusCode;
    }

    public String getResponseHeader(String headerName) {
        return responseHeaders.get(headerName);
    }

    public String getResponseBody() {
        return responseBody;
    }
}
