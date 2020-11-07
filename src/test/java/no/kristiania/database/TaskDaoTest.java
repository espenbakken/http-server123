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
    void shouldListAllTasks() throws SQLException {
        MemberTask task1 = exampleTask();
        MemberTask task2 = exampleTask();
        taskDao.insert(task1);
        taskDao.insert(task2);
        assertThat(taskDao.list())
                .extracting(MemberTask::getName)
                .contains(task1.getName(), task2.getName());
    }


    @Test
    void shouldRetrieveAllTaskProperties() throws SQLException {
        taskDao.insert(exampleTask());
        taskDao.insert(exampleTask());
        MemberTask task = exampleTask();
        taskDao.insert(task);
        assertThat(task).hasNoNullFieldsOrProperties();

        assertThat(taskDao.retrieve(task.getId()))
                .usingRecursiveComparison()
                .isEqualTo(task);
    }

    @Test
    void shouldReturnTasksAsOptions() throws SQLException {
        MemberTaskOptionsController controller = new MemberTaskOptionsController(taskDao);
        MemberTask memberTask = exampleTask();
        taskDao.insert(memberTask);

        assertThat(controller.getBody(null))
                .contains("<option value=" + memberTask.getId() + ">" + memberTask.getName() + "</option>");
    }

    @Test
    void shouldSelectCurrentTasksInOptions() throws SQLException {
        MemberTaskOptionsController controller = new MemberTaskOptionsController(taskDao);
        MemberTask memberTask = exampleTask();
        taskDao.insert(memberTask);

        assertThat(controller.getBody(memberTask.getId()))
                .contains("<option value=" + memberTask.getId() + " selected='selected'>" + memberTask.getName() + "</option>");
    }

    public static MemberTask exampleTask() {
        MemberTask task = new MemberTask();
        task.setName(exampleTaskName());
        return task;
    }

    private static String exampleTaskName() {
        String[] options = {"Fruit", "Candy", "Non-food", "Dairy"};
        return options[random.nextInt(options.length)];
    }
}
