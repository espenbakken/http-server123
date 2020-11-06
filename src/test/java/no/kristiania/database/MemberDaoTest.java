package no.kristiania.database;

import no.kristiania.http.HttpMessage;
import no.kristiania.http.ProductOptionsController;
import no.kristiania.http.UpdateProductController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.PaintEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberDaoTest {

    private MemberDao memberDao;
    private static Random random = new Random();
    private ProductCategoryDao categoryDao;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        memberDao = new MemberDao(dataSource);
        categoryDao = new ProductCategoryDao(dataSource);
    }

    @Test
    void shouldListInsertedMembers() throws SQLException {
        Member member1 = exampleMember();
        Member member2 = exampleMember();
        memberDao.insert(member1);
        memberDao.insert(member2);
        assertThat(memberDao.list())
                .extracting(Member::getName)
                .contains(member1.getName(), member2.getName());
    }

    @Test
    void shouldQueryMembersByCategory() throws SQLException {
        ProductCategory category = CategoryDaoTest.exampleCategory();
        categoryDao.insert(category);

        ProductCategory otherCategory = CategoryDaoTest.exampleCategory();
        categoryDao.insert(otherCategory);

        Member matchingMember = exampleMember();
        matchingMember.setCategoryId(category.getId());
        memberDao.insert(matchingMember);
        Member nonMatchingMember = exampleMember();
        nonMatchingMember.setCategoryId(otherCategory.getId());
        memberDao.insert(nonMatchingMember);

        assertThat(memberDao.queryMembersByCategoryId(category.getId()))
                .extracting(Member::getId)
                .contains(matchingMember.getId())
                .doesNotContain(nonMatchingMember.getId());
    }

    @Test
    void shouldRetrieveAllMemberProperties() throws SQLException{
        memberDao.insert(exampleMember());
        memberDao.insert(exampleMember());
        Member member = exampleMember();
        memberDao.insert(member);
        assertThat(member).hasNoNullFieldsOrPropertiesExcept("categoryId");
        assertThat(memberDao.retrieve(member.getId()))
                .usingRecursiveComparison()
                .isEqualTo(member);
    }

    @Test
    void shouldReturnMembersAsOptions() throws SQLException {
        ProductOptionsController controller = new ProductOptionsController(memberDao);
        Member member = MemberDaoTest.exampleMember();
        memberDao.insert(member);

        assertThat(controller.getBody())
                .contains("<option value=" + member.getId() + ">" + member.getName() + "</option>");
    }

    @Test
    void shouldUpdateExistingProductWithNewCategory() throws IOException, SQLException {
        UpdateProductController controller = new UpdateProductController(memberDao);

        Member member = exampleMember();
        memberDao.insert(member);

        ProductCategory category = CategoryDaoTest.exampleCategory();
        categoryDao.insert(category);

        String body = "memberId=" + member.getId() + "&categoryId=" + category.getId();

        HttpMessage response = controller.handle(new HttpMessage(body));

        assertThat(memberDao.retrieve(member.getId()).getCategoryId())
                .isEqualTo(category.getId());
        assertThat(response.getStartLine())
                .isEqualTo("HTTP/1.1 302 Redirect");
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("http://localhost:8080/index.html");
    }

    public static Member exampleMember() {
        Member member = new Member();
        member.setName(exampleMemberName());
        member.setAge((int) (10.50 + random.nextInt(20)));
        member.setLastName(exampleMemberName());
        member.setEmail(exampleMemberName());
        return member;
    }


    private static String exampleMemberName() {
        String[] options = {"Apples", "Bananas", "Coconuts", "Dates"};
        return options[random.nextInt(options.length)];
    }
}