package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;
import no.kristiania.database.MemberTaskDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private Map<String, HttpController> controllers;

    private final MemberDao memberDao;
    private final ServerSocket serverSocket;

    public HttpServer(int port, DataSource dataSource) throws IOException {

        memberDao = new MemberDao(dataSource);
        MemberTaskDao memberTaskDao = new MemberTaskDao(dataSource);

        controllers = Map.of(
                "/api/newTask", new MemberTaskPostController(memberTaskDao),
                "/api/tasks", new MemberTaskGetController(memberTaskDao),
                "/api/taskOptions", new MemberTaskOptionsController(memberTaskDao),
                "/api/memberOptions", new MemberOptionsController(memberDao),
                "/api/updateMember", new UpdateMemberController(memberDao)
        );

        serverSocket = new ServerSocket(port);
        logger.info("Server started on port {}", serverSocket.getLocalPort());

        new Thread(() -> {
            while (true) {
                try {Socket clientSocket = serverSocket.accept();
                    handleRequest(clientSocket);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private void handleRequest(Socket clientSocket) throws IOException, SQLException {
        //The first line of the incoming request is called the request line
        HttpMessage request = new HttpMessage(clientSocket);
        String requestLine = request.getStartLine();
        System.out.println("REQUEST" + requestLine);

        // The requestLine consists of a verb (GET, POST), a request target and HTTP version
        String requestMethod = requestLine.split(" ")[0];
        String requestTarget = requestLine.split(" ")[1];

        int questionPos = requestTarget.indexOf('?');

        String requestPath = questionPos != -1 ? requestTarget.substring(0, questionPos) : requestTarget;

        //Here we deal with POST /addMember
        if (requestMethod.equals("POST")) {
            if (requestPath.equals("/api/newMember")){
                handlePostMember(clientSocket, request);
            } else{
                getController(requestPath).handle(request, clientSocket);

            }

        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(clientSocket, requestTarget, questionPos);
            } else if (requestPath.equals("/api/members")) {
                handleGetMembers(clientSocket, requestTarget, questionPos);
            } else {
                HttpController controller = controllers.get(requestPath);
                if (controller != null){
                    controller.handle(request, clientSocket);
                }else {
                    handleFileRequest(clientSocket, requestPath);
                }
            }
        }
    }

    private HttpController getController(String requestPath) {
        return controllers.get(requestPath);
    }

    private void handlePostMember(Socket clientSocket, HttpMessage request) throws SQLException, IOException {
        QueryString requestParameter = new QueryString(request.getBody());

        Member member = new Member();
        member.setName(requestParameter.getParameter("memberName"));
        member.setAge(Double.parseDouble(requestParameter.getParameter("age")));
        member.setLastName(requestParameter.getParameter("lastName"));
        member.setEmail(requestParameter.getParameter("email"));
        memberDao.insert(member);
        String body = "Gruppemedlem er lagt til i databasen!";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleFileRequest(Socket clientSocket, String requestPath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(requestPath)){
            if (inputStream == null){
                String body = requestPath + " does not exist";
                String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        body;
                clientSocket.getOutputStream().write(response.getBytes());
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);

            String contentType = "text/plain";
            if (requestPath.endsWith(".html")) {
                contentType = "text/html";
            }

            if (requestPath.endsWith(".ico")) {
                contentType = "image/png";
            }

            if (requestPath.endsWith(".css")) {
                contentType = "text/css";
            }

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + buffer.toByteArray().length + "\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "\r\n";
            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.getOutputStream().write(buffer.toByteArray());
        }
    }

    private void handleGetMembers(Socket clientSocket, String requestTarget, int questionPos) throws IOException, SQLException {
        Integer taskId = null;
        if (questionPos != -1){

            taskId = Integer.valueOf(new QueryString(requestTarget.substring(questionPos+1))
                    .getParameter("taskId"));
        }
        List<Member> members = taskId == null ? memberDao.list() : memberDao.queryMembersByTaskId(taskId);
        String body = "<ul>";
        for (Member member : members) {
            body += "<li>" + member.getName() + " " + member.getLastName() + "(" + String.format("%.0f", member.getAge()) + ")" + "<br>" +
                        member.getEmail() + "</li>";

        }
        body += "</ul>";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleEchoRequest(Socket clientSocket, String requestTarget, int questionPos) throws IOException{
        String statusCode = "200";
        String body = "Hello World";
        if (questionPos != -1){
            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            if (queryString.getParameter("status") != null){
                statusCode = queryString.getParameter("status");
            }
            if (queryString.getParameter("body") != null){
                body = queryString.getParameter("body");
            }
        }
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text-plain\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }
    
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        try(FileReader fileReader = new FileReader("pgr203.properties")){
            properties.load(fileReader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.username"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        logger.info("Using database {}", dataSource.getUrl());
        Flyway.configure().dataSource(dataSource).load().migrate();

        new HttpServer(8080, dataSource);
        logger.info("Started on http://localhost:{}/index.html", 8080);
    }

    public List<Member> getMembers() throws SQLException{
        return memberDao.list();
    }
}
