package no.kristiania.http;

import no.kristiania.database.Product;
import no.kristiania.database.ProductDao;
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
import java.util.Properties;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final ProductDao productDao;

    public HttpServer(int port, DataSource dataSource) throws IOException {
        productDao = new ProductDao(dataSource);
        ServerSocket serverSocket = new ServerSocket(port);

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
            QueryString requestParameter = new QueryString(request.getBody());

            Product product = new Product();
            product.setName(requestParameter.getParameter("productName"));
            product.setLastName(requestParameter.getParameter("lastName"));
            product.setEmail(requestParameter.getParameter("email"));
            productDao.insert(product);
            String body = "Gruppemedlem er lagt til i databasen!";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Connection: close\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body;
            clientSocket.getOutputStream().write(response.getBytes());
        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(clientSocket, requestTarget, questionPos);
            } else if (requestPath.equals("/api/members")) {
                handleGetmembers(clientSocket);
            } else {
                handleFileRequest(clientSocket, requestPath);
            }
        }
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

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + buffer.toByteArray().length + "\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "\r\n";
            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.getOutputStream().write(buffer.toByteArray());
        }
    }

    private void handleGetmembers(Socket clientSocket) throws IOException, SQLException {
        String body = "<ul>";


        for (Product product : productDao.list()) {

            body += "<li>" + product.getName() + " " + product.getLastName() + "<br>" +
                product.getEmail() + "</li>";
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

    public List<Product> getProductNames() throws SQLException{
        return productDao.list();
    }
}
