package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {

    private String responseBody;
    private final HttpMessage responseMessage;

    // Constructor - det som kalles når vi sier new
    public HttpClient(final String hostname, int port, final String requestTarget) throws IOException {
        // Connect til serveren
        Socket socket = new Socket(hostname, port);

        // HTTP Request consists of request line + 0 or more request headers
        //  request line consists of verb (GET, POST, PUT) request target ("/echo?status=404"), protocol (HTTP/1.1)
        HttpMessage requestMessage = new HttpMessage("GET " + requestTarget + " HTTP/1.1");
        requestMessage.setHeader("Host", hostname);
        requestMessage.write(socket);

        // The first line in the response is called the response line or status line
        // response line consists of protocol ("HTTP/1.1") status code (200, 404, 401, 500) and status message


        responseMessage = HttpMessage.read(socket);

        int contentLength = Integer.parseInt(getResponseHeader("Content-Length"));
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            body.append((char) socket.getInputStream().read());
        }
        responseBody = body.toString();
    }
        /*
        String[] responseLineParts = responseLine.split(" ");
        responseMessage = new HttpMessage(responseLine);

        // Status code determines if it went ok (2xx) or not (4xx). In addition: 5xx: server error) 3xx
        int statusCode = Integer.parseInt(responseLineParts[1]);

        String headerLine;
        while (!(headerLine = HttpMessage.readLine(socket)).isEmpty()){
         int colonPos = headerLine.indexOf(':');
         String headerName = headerLine.substring(0, colonPos);
         String headerValue = headerLine.substring(colonPos+1).trim();

         responseMessage.setHeader(headerName, headerValue);
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put(headerName, headerValue);
        }


        // The Content-Length header tells us how many bytes in the response follow the headers
        int contentLength = Integer.parseInt(getResponseHeader("Content-Length"));
        // The next content-length bytes are called the response body
        this.responseBody = HttpMessage.readBody(socket, contentLength);
    }*/

    public static void main(String[] args) throws IOException {
        new HttpClient("urlecho.appspot.com", 80, "/echo?status=200&body=Hello%World!");
    }

    public Object getStatusCode() {
        String[] responseLineParts = responseMessage.getStartLine().split(" ");
        int statusCode = Integer.parseInt(responseLineParts[1]);
        return statusCode;
    }

    public String getResponseHeader(String headerName) {
        return responseMessage.getHeader(headerName);
    }

    public String getResponseBody() {
        return responseBody;
    }
}