package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    private int statusCode;
    private final Map<String, String> headers = new HashMap<>();
    private String responseBody;

    public HttpClient(String hostname, int port, String requestTarget) throws IOException {
        Socket socket = new Socket(hostname, port);

        // The HTTP request consists of a request line and zero or more header lines, terminated by a blank line
        //  The request line consists of a verb (GET, POST) a request target and the HTTP version
        // For example "GET /index.html HTTP/1.1"
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                // A request header consists of name: value
                // The Host header is the same as the web browser shows in the menu bar
                "Host: " + hostname + "\r\n\r\n";
        socket.getOutputStream().write(request.getBytes());

        // The first line in the response is called the response line
        String responseLine = readLine(socket);
        // The response line consists of the HTTP version, a response code and a description
        // For example "HTTP/1.1 404 Not Found"
        String[] parts = responseLine.toString().split(" ");
        //The status line is the second word in the response line
        statusCode = Integer.parseInt(parts[1]);

        //After the response line, the response had zero or more responsive headers
        String headerLine;
        while(!(headerLine = readLine(socket)).isEmpty()) {
            // Each header consists of name: value
           int colonPos = headerLine.indexOf(':');
            String headerName = headerLine.substring(0, colonPos);
            // Spaces at the beginning and end of the header value should be ignored
            String headerValue = headerLine.substring(colonPos+1).trim();

            headers.put(headerName, headerValue);

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

    private String readLine(Socket socket) throws IOException {
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
        return headers.get(headerName);
    }

    public String getResponseBody() {
        return responseBody;
    }
}
