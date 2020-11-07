package no.kristiania.database;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberTaskDao extends AbstractDao<MemberTask>{

    public MemberTaskDao(DataSource dataSource) { super(dataSource); }

    public void insert(MemberTask task) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO member_tasks (name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                //getter and setter method
                statement.setString(1, task.getName());
                statement.executeUpdate();
                //setting the keys to id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    task.setId(generatedKeys.getInt("id"));
                }
            }
        }
    }

    public MemberTask retrieve(Integer id) throws SQLException {
        return retrieve(id, "SELECT * FROM member_tasks WHERE id = ?");
    }

    public List<MemberTask> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM member_tasks")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<MemberTask> members = new ArrayList<>();
                    while (rs.next()) {
                        members.add(mapRow(rs));
                    }
                    return members;
                }
            }
        }
    }

    @Override
    protected MemberTask mapRow(ResultSet rs) throws SQLException {
        MemberTask task = new MemberTask();
        task.setId(rs.getInt("id"));
        task.setName(rs.getString("name"));
        return task;
    }
}
