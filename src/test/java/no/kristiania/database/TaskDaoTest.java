package no.kristiania.database;

import no.kristiania.http.MemberTaskOptionsController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskDaoTest {

    private MemberTaskDao taskDao;
    private static Random random = new Random();

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        taskDao = new MemberTaskDao(dataSource);
    }

    @Test
    void shouldListAlltasks() throws SQLException {
        MemberTask task1 = exampletask();
        MemberTask task2 = exampletask();
        taskDao.insert(task1);
        taskDao.insert(task2);
        assertThat(taskDao.list())
                .extracting(MemberTask::getName)
                .contains(task1.getName(), task2.getName());
    }


    @Test
    void shouldRetrieveAlltaskProperties() throws SQLException {
        taskDao.insert(exampletask());
        taskDao.insert(exampletask());
        MemberTask task = exampletask();
        taskDao.insert(task);
        assertThat(task).hasNoNullFieldsOrProperties();

        assertThat(taskDao.retrieve(task.getId()))
                .usingRecursiveComparison()
                .isEqualTo(task);
    }

    @Test
    void shouldReturntasksAsOptions() throws SQLException {
        MemberTaskOptionsController controller = new MemberTaskOptionsController(taskDao);
        MemberTask membertask = exampletask();
        taskDao.insert(membertask);

        assertThat(controller.getBody(null))
                .contains("<option value=" + membertask.getId() + ">" + membertask.getName() + "</option>");
    }

    @Test
    void shouldSelectCurrenttasksInOptions() throws SQLException {
        MemberTaskOptionsController controller = new MemberTaskOptionsController(taskDao);
        MemberTask membertask = exampletask();
        taskDao.insert(membertask);

        assertThat(controller.getBody(membertask.getId()))
                .contains("<option value=" + membertask.getId() + " selected='selected'>" + membertask.getName() + "</option>");
    }

    public static MemberTask exampletask() {
        MemberTask task = new MemberTask();
        task.setName(exampletaskName());
        return task;
    }

    private static String exampletaskName() {
        String[] options = {"Fruit", "Candy", "Non-food", "Dairy"};
        return options[random.nextInt(options.length)];
    }
}
