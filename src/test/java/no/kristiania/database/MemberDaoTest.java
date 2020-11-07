package no.kristiania.database;

import no.kristiania.http.HttpMessage;
import no.kristiania.http.MemberOptionsController;
import no.kristiania.http.UpdateMemberController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberDaoTest {

    private MemberDao memberDao;
    private static Random random = new Random();
    private MemberTaskDao taskDao;
    private MemberTask defaulttask;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        memberDao = new MemberDao(dataSource);
        taskDao = new MemberTaskDao(dataSource);

        defaulttask = TaskDaoTest.exampletask();
        taskDao.insert(defaulttask);
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
    void shouldQueryMembersBytask() throws SQLException {
        MemberTask task = TaskDaoTest.exampletask();
        taskDao.insert(task);

        MemberTask othertask = TaskDaoTest.exampletask();
        taskDao.insert(othertask);

        Member matchingMember = exampleMember();
        matchingMember.settaskId(task.getId());
        memberDao.insert(matchingMember);
        Member nonMatchingMember = exampleMember();
        nonMatchingMember.settaskId(othertask.getId());
        memberDao.insert(nonMatchingMember);

        assertThat(memberDao.queryMembersBytaskId(task.getId()))
                .extracting(Member::getId)
                .contains(matchingMember.getId())
                .doesNotContain(nonMatchingMember.getId());
    }

    @Test
    void shouldSaveAndRetrieveAllMemberProperties() throws SQLException{
        memberDao.insert(exampleMember());
        memberDao.insert(exampleMember());
        Member member = exampleMember();
        memberDao.insert(member);
        assertThat(member).hasNoNullFieldsOrProperties();
        assertThat(memberDao.retrieve(member.getId()))
                .usingRecursiveComparison()
                .isEqualTo(member);
    }

    @Test
    void shouldReturnMembersAsOptions() throws SQLException {
        MemberOptionsController controller = new MemberOptionsController(memberDao);
        Member member = exampleMember();
        memberDao.insert(member);

        assertThat(controller.getBody())
                .contains("<option value=" + member.getId() + ">" + member.getName() + "</option>");
    }

    @Test
    void shouldUpdateExistingmemberWithNewtask() throws IOException, SQLException {
        UpdateMemberController controller = new UpdateMemberController(memberDao);

        Member member = exampleMember();
        memberDao.insert(member);

        MemberTask task = TaskDaoTest.exampletask();
        taskDao.insert(task);

        String body = "memberId=" + member.getId() + "&taskId=" + task.getId();

        HttpMessage response = controller.handle(new HttpMessage(body));

        assertThat(memberDao.retrieve(member.getId()).gettaskId())
                .isEqualTo(task.getId());
        assertThat(response.getStartLine())
                .isEqualTo("HTTP/1.1 302 Redirect");
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("http://localhost:8080/index.html");
    }

    private Member exampleMember() {
        Member member = new Member();
        member.setName(exampleMemberName());
        member.setAge((int) (10.50 + random.nextInt(20)));
        member.settaskId(defaulttask.getId());
        member.setLastName(exampleMemberName());
        member.setEmail(exampleMemberName());
        return member;
    }


    private static String exampleMemberName() {
        String[] options = {"Apples", "Bananas", "Coconuts", "Dates"};
        return options[random.nextInt(options.length)];
    }
}