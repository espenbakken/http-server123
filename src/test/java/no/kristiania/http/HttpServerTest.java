package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp(){
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
    }

    @Test
    void shouldReturnSuccesfulErrorCode() throws IOException {
        new HttpServer(10001, dataSource);
        HttpClient client = new HttpClient("localhost", 10001, "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccesfulErrorCode() throws IOException {
        new HttpServer(10002, dataSource);
        HttpClient client = new HttpClient("localhost", 10002, "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnHttpHeaders() throws IOException {
        new HttpServer(10003, dataSource);
        HttpClient client = new HttpClient("localhost", 10003, "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException {
        new HttpServer(10020, dataSource);
        HttpClient client = new HttpClient("localhost", 10020, "/echo?body=Hello");
        assertEquals("Hello", client.getResponseBody());
    }

    @Test
    void shouldReturnFileContent() throws IOException{
        new HttpServer(10005, dataSource);
        File documentRoot = new File("target/test-classes");

        String fileContent = "Hello " + new Date();
        Files.writeString(new File(documentRoot, "index.html").toPath(), fileContent);
        HttpClient client = new HttpClient("localhost", 10005, "/index.html");
        assertEquals(fileContent, client.getResponseBody());
    }

    @Test
    void shouldReturn404onMissingFile() throws IOException {
        new HttpServer(10006, dataSource);

        HttpClient client = new HttpClient("localhost", 10006, "/missingFile");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException{
        new HttpServer(10007, dataSource);
        File documentRoot = new File("target");

        Files.writeString(new File(documentRoot, "plain.txt").toPath(), "Plain text");
        HttpClient client = new HttpClient("localhost", 10007, "/plain.txt");
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldPostNewMember() throws IOException, SQLException {
        HttpServer server = new HttpServer(10008, dataSource);
        HttpClient client = new HttpClient("localhost", 10008, "/api/newMember", "POST", "memberName=apples&age=10");
        assertEquals(200, client.getStatusCode());
        assertThat(server.getMemberNames())
                .extracting(member -> member.getName())
                .contains("apples");
    }

    @Test
    void shouldReturnExistingMembers() throws IOException, SQLException {
        new HttpServer(10009, dataSource);
        MemberDao memberDao = new MemberDao(dataSource);
        Member member = new Member();
        member.setName("Espen");
        member.setLastName("Bakken");
        member.setAge(20);
        member.setEmail("test@gmail.com");
        memberDao.insert(member);
        HttpClient client = new HttpClient("localhost", 10009, "/api/members");
        assertThat(client.getResponseBody()).contains("<li>Espen Bakken<br>test@gmail.com</li>");
    }
}