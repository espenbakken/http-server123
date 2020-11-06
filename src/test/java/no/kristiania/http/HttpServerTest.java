package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;
import no.kristiania.database.ProductCategory;
import no.kristiania.database.ProductCategoryDao;
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
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        server = new HttpServer(0, dataSource);
    }

    @Test
    void shouldReturnSuccesfulErrorCode() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccesfulErrorCode() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnHttpHeaders() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/echo?body=Hello");
        assertEquals("Hello", client.getResponseBody());
    }

    @Test
    void shouldReturnFileContent() throws IOException{
        File documentRoot = new File("target/test-classes");

        String fileContent = "Hello " + new Date();
        Files.writeString(new File(documentRoot, "index.html").toPath(), fileContent);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/index.html");
        assertEquals(fileContent, client.getResponseBody());
    }

    @Test
    void shouldReturn404onMissingFile() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/missingFile");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException{
        File documentRoot = new File("target");

        Files.writeString(new File(documentRoot, "plain.txt").toPath(), "Plain text");
        HttpClient client = new HttpClient("localhost", server.getPort(), "/plain.txt");
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldPostNewMember() throws IOException, SQLException {
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
    void shouldFilterMembersByCategory() throws SQLException, IOException {
        MemberDao memberDao = new MemberDao(dataSource);
        Member espen = new Member();
        espen.setName("Espen");
        espen.setLastName("Bakken");
        espen.setAge(20);
        espen.setEmail("test@gmail.com");
        memberDao.insert(espen);

        Member shazo = new Member();
        shazo.setName("Shazo");
        shazo.setLastName("Kul");
        shazo.setAge(20);
        shazo.setEmail("test123@gmail.com");
        memberDao.insert(shazo);

        ProductCategoryDao categoryDao = new ProductCategoryDao(dataSource);
        ProductCategory beers = new ProductCategory();
        beers.setName("Beer");
        categoryDao.insert(beers);

        shazo.setCategoryId(beers.getId());
        memberDao.update(shazo);

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/members?categoryId=" + beers.getId());
        assertThat(client.getResponseBody())
                .contains("<li>Shazo Kul(20)<br>test123@gmail.com</li>")
                .doesNotContain("<li>Espen Bakken(20)<br>test@gmail.com</li>");

    }

    @Test
    void shouldPostNewCategory() throws IOException, SQLException {
        String requestBody = "categoryName=candy&color=black";
        HttpClient postClient = new HttpClient("localhost", server.getPort(), "/api/newCategory", "POST", requestBody);
        assertEquals(200, postClient.getStatusCode());

        HttpClient getClient = new HttpClient("localhost", server.getPort(), "/api/categories");
        assertThat(getClient.getResponseBody()).contains("<li>candy</li>");
    }
}