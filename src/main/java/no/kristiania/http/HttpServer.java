package no.kristiania.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HttpServer {

    private File documentRoot;
    private List<String> memberNames = new ArrayList<>();

    public HttpServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    handleRequest(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        //The first line of the incoming request is called the request line
        String requestLine = HttpMessage.readLine(clientSocket);
        System.out.println(requestLine);

        // The requestLine consists of a verb (GET, POST), a request target and HTTP version
        String requestMethod = requestLine.split(" ")[0];
        String requestTarget = requestLine.split(" ")[1];

        //Here we deal with POST /addMember
        if (requestMethod.equals("POST")) {
            HttpMessage requestMessage = new HttpMessage(requestLine);
            requestMessage.readHeaders(clientSocket);
            QueryString requestForm = new QueryString(requestMessage.readBody(clientSocket));
            memberNames.add(requestForm.getParameter("memberName"));

            HttpMessage responseMessage = new HttpMessage("HTTP/1.1 200 OK\r\n" +
                    "Content-Length: 62 \r\n" +
                    "\r\n" +
                    "Member added to project \r\n " +
                    "Please go back to see all members");
            responseMessage.write(clientSocket);
            return;
        }

        String statusCode = null;
        String body = null;
        // The request target can have a query string separated by ?
        // For example /echo?status=404
        int questionPos = requestTarget.indexOf('?');

        if (requestTarget.equals("/api/members")) {
            body ="<ul>";
            for (String memberName : getMemberNames()) {
                body += "<li>" + memberName + "</li>";
            }
            body += "</ul>";
        } else if (questionPos != -1) {
            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            if (queryString.getParameter("status") != null){
                statusCode = queryString.getParameter("status");
            }
            if (queryString.getParameter("body") != null) {
                body = queryString.getParameter("body");
            }
        } else if (!requestTarget.equals("/echo")){
            File targetFile = new File(documentRoot, requestTarget);

            if (!targetFile.exists()){
                writeResponse(clientSocket, "404", requestTarget + " not found");
                return;
            }

            HttpMessage responseMessage = new HttpMessage("HTTP/1.1 200 OK");
            responseMessage.setHeader("Content-Length", String.valueOf(targetFile.length()));
            responseMessage.setHeader("Content-type", "text/html");

            if (targetFile.getName().endsWith(".txt")) {
                responseMessage.setHeader("Content-Type", "text/plain");
            }

            else if (targetFile.getName().endsWith(".css")) {
                responseMessage.setHeader("Content-Type", "text/css");
            }
            responseMessage.write(clientSocket);

            try(FileInputStream inputStream = new FileInputStream(targetFile)) {
                inputStream.transferTo(clientSocket.getOutputStream());
            }
        }
        if (statusCode == null) statusCode = "200";
        if (body == null) body = "Hello <strong>World</strong>!";

        writeResponse(clientSocket, statusCode, body);
    }

    private void writeResponse(Socket clientSocket, String statusCode, String body) throws IOException {
        HttpMessage responseMessage = new HttpMessage("HTTP/1.1 " + statusCode + " OK");
        responseMessage.setHeader("Content-Length", String.valueOf(body.length()));
        responseMessage.setHeader("Content-Type", "text/plain");
        responseMessage.write(clientSocket);
        clientSocket.getOutputStream().write(body.getBytes());
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(8080);
        server.setDocumentRoot(new File("src/main/resources"));
    }

    public void setDocumentRoot(File documentRoot) {
        this.documentRoot = documentRoot;
    }

    public <E> List<String> getMemberNames() {
        return memberNames;
    }
}
