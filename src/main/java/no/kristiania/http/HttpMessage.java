package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {
    private String startLine;
    private Map<String, String> headers = new HashMap<>();

    public HttpMessage(String startLine) {
        this.startLine = startLine;
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

    public String readBody(Socket socket) throws IOException {
        int contentLength = Integer.parseInt(getHeader("Content-Length"));
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            body.append((char)socket.getInputStream().read());
        }
        return body.toString();
    }

    public static HttpMessage read(Socket socket) throws IOException {
        HttpMessage message = new HttpMessage(readLine(socket));
        message.readHeaders(socket);
        return message;
    }

    public void readHeaders(Socket socket) throws IOException {
        Map<String, String> headers = new HashMap<>();
        //After the response line, the response had zero or more responsive headers
        String headerLine;
        while(!(headerLine = HttpMessage.readLine(socket)).isEmpty()) {
            // Each header consists of name: value
            int colonPos = headerLine.indexOf(':');
            String headerName = headerLine.substring(0, colonPos);
            // Spaces at the beginning and end of the header value should be ignored
            String headerValue = headerLine.substring(colonPos+1).trim();

            setHeader(headerName, headerValue);

        }
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void write(Socket socket) throws IOException {
        writeLine(socket, startLine);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            writeLine(socket, header.getKey() + ": " + header.getValue());
        }
        writeLine(socket, "");
    }

    private void writeLine(Socket socket, String startLine) throws IOException {
        socket.getOutputStream().write((startLine + "\r\n").getBytes());
    }

    public String getStartLine() {
        return startLine;
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }
}