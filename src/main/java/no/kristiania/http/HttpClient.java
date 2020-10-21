package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    private int statusCode;
    private String responseBody;
    private Map<String, String> responseHeaders = new HashMap<>();

    // Constructor - det som kalles når vi sier new
    public HttpClient(final String hostname, int port, final String requestTarget) throws IOException {
        this(hostname, port, requestTarget, "GET", null);
    }
    //HttpClient fetches the Hostname, por, requestTarget, method and requestBody.
    public HttpClient(final String hostname, int port, final String requestTarget, final String method, String requestBody) throws IOException {
        Socket socket = new Socket(hostname, port);

        String contentLengthHeader = requestBody != null ? "Content-Length: " + requestBody.length() + "\r\n" : "";

        String request = method + " " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + hostname + "\r\n" +
                contentLengthHeader +
                "\r\n";

        socket.getOutputStream().write(request.getBytes());

        if (requestBody != null) {
            socket.getOutputStream().write(requestBody.getBytes());
        }

        HttpMessage response = new HttpMessage(socket);

        String responseLine = response.getStartLine();
        responseHeaders = response.getHeaders();
        responseBody = response.getBody();

        String[] responseLineParts = responseLine.split(" ");

        statusCode = Integer.parseInt(responseLineParts[1]);
    }
    //Prints out client.GetResponseBody
    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient("urlecho.appspot.com", 80, "/echo?status=404&Content-Type=text%2Fhtml&body=Hello+World");
        System.out.println(client.getResponseBody());
    }
    //GetStatusCode returns statusCode
    public Object getStatusCode() {
        return statusCode;
    }
    //getResponseHeader returns responseHeaders.get(headerName)
    public String getResponseHeader(String headerName) {
        return responseHeaders.get(headerName);
    }
    //getResponseBody returns responseBody
    public String getResponseBody() {
        return responseBody;
    }
}//End class