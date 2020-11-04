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
        HttpServer server = new HttpServer(10001, dataSource);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccesfulErrorCode() throws IOException {
        HttpServer server = new HttpServer(10002, dataSource);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnHttpHeaders() throws IOException {
        HttpServer server = new HttpServer(10003, dataSource);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException {
        HttpServer server = new HttpServer(10020, dataSource);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=Hello");
        assertEquals("Hello", client.getResponseBody());
    }

    @Test
    void shouldReturnFileContent() throws IOException{
        HttpServer server = new HttpServer(10005, dataSource);
        File documentRoot = new File("target/test-classes");

        String fileContent = "Hello " + new Date();
        Files.writeString(new File(documentRoot, "index.html").toPath(), fileContent);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/index.html");
        assertEquals(fileContent, client.getResponseBody());
    }

    @Test
    void shouldReturn404onMissingFile() throws IOException {
        HttpServer server = new HttpServer(10006, dataSource);

        HttpClient client = new HttpClient("localhost", server.getPort(), "/missingFile");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException{
        HttpServer server =  new HttpServer(10007, dataSource);
        File documentRoot = new File("target");

        Files.writeString(new File(documentRoot, "plain.txt").toPath(), "Plain text");
        HttpClient client = new HttpClient("localhost", server.getPort(), "/plain.txt");
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldPostNewMember() throws IOException, SQLException {
        HttpServer server = new HttpServer(10008, dataSource);
        String requestBody = "memberName=apples&age=10";
        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/newMember", "POST", requestBody);
        assertEquals(200, client.getStatusCode());
        assertThat(server.getMembers())
                .filteredOn(member -> member.getName().equals("apples"))
                .isNotEmpty()
                .satisfies(p -> assertThat(p.get(0).getAge()).isEqualTo(10));
               /* .extracting(member -> member.getName())
                .contains("apples");*/
    }

   @Test
    void shouldReturnExistingMembers() throws IOException, SQLException {
        HttpServer server = new HttpServer(10009, dataSource);
        MemberDao memberDao = new MemberDao(dataSource);
        Member member = new Member();
        member.setName("Espen");
        member.setLastName("Bakken");
        member.setAge(20);
        member.setEmail("test@gmail.com");
        memberDao.insert(member);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/members");
        assertThat(client.getResponseBody()).contains("<li>Espen Bakken(20)<br>test@gmail.com</li>");
    }

    @Test
    void shouldPostNewCategory() throws IOException, SQLException {
        HttpServer server = new HttpServer(10010, dataSource);
        String requestBody = "categoryName=candy&color=black";
        HttpClient postClient = new HttpClient("localhost", server.getPort(), "/api/newCategory", "POST", requestBody);
        assertEquals(200, postClient.getStatusCode());

        HttpClient getClient = new HttpClient("localhost", 10010, "/api/categories");
        assertThat(getClient.getResponseBody()).contains("<li>candy</li>");
    }
}