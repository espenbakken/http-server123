package no.kristiania.http;

import no.kristiania.http.HttpClient;
import no.kristiania.http.HttpServer;
import no.kristiania.http.QueryString;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    @Test
    void shouldReturnSuccesfulErrorCode() throws IOException {
        HttpServer server = new HttpServer(10001);
        HttpClient client = new HttpClient("localhost", 10001, "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccesfulErrorCode() throws IOException {
        HttpServer server = new HttpServer(10002);
        HttpClient client = new HttpClient("localhost", 10002, "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnResponseBody() throws IOException{
        new HttpServer(10004);
        HttpClient client = new HttpClient("localhost", 10004, "echo?body=HelloWorld");
        assertEquals("HelloWorld", client.getResponseBody());
    }

    @Test
    void shouldReturnHttpHeaders() throws IOException {
        new HttpServer(10003);
        HttpClient client = new HttpClient("localhost", 10003, "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnFileContent() throws IOException{
        HttpServer server = new HttpServer(10005);
        File documentRoot = new File("target");
        server.setDocumentRoot(documentRoot);
        String fileContent = "Hello " + new Date();
        Files.writeString(new File(documentRoot, "index.html").toPath(), fileContent);
        HttpClient client = new HttpClient("localhost", 10005, "/index.html");
        assertEquals(fileContent, client.getResponseBody());
    }

    @Test
    void shouldReturn404onMissingFile() throws IOException {
        HttpServer server = new HttpServer(10006);
        server.setDocumentRoot(new File("target"));
        HttpClient client = new HttpClient("localhost", 10006, "/missingFile");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException{
        HttpServer server = new HttpServer(10007);
        File documentRoot = new File("target");
        server.setDocumentRoot(documentRoot);
        Files.writeString(new File(documentRoot, "plain.txt").toPath(), "Plain text");
        HttpClient client = new HttpClient("localhost", 10007, "/plain.txt");
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldPostProduct() throws IOException{
        HttpServer server = new HttpServer(10008);
        QueryString member = new QueryString("");
        member.addParameter("memberName", "Espen");
        member.addParameter("email", "100");
        new HttpClient("localhost", 10008, "/addMember", "POST", member);
        assertEquals(List.of("Espen"), server.getMemberNames());
    }

    @Test
    void shouldDisplayExistingProducts() throws IOException{
        HttpServer server = new HttpServer(10009);
        server.getMemberNames().add("EspenBakken");
        HttpClient client = new HttpClient("localhost", 10009, "/api/members");
        assertEquals("<ul><li>EspenBakken</li></ul>", client.getResponseBody());
    }
}