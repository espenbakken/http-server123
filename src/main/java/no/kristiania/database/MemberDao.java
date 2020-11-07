package no.kristiania.database;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class MemberDao extends AbstractDao<Member> {

    public MemberDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(Member member) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO members (member_name, age, last_name, email, task_id) values (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                //getter and setter method
                statement.setString(1, member.getName());
                statement.setDouble(2, member.getAge());
                statement.setString(3, member.getLastName());
                statement.setString(4, member.getEmail());
                statement.setObject(5, member.gettaskId());
                statement.executeUpdate();
                //setting the keys to id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    member.setId(generatedKeys.getInt("id"));
                }
            }
        }
    }

    public void update(Member member) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE members SET task_id = ? WHERE id = ?"
            )) {
                //getter and setter method
                statement.setInt(1, member.gettaskId());
                statement.setInt(2, member.getId());
                statement.executeUpdate();
            }
        }

    }

    public Member retrieve(Integer id) throws SQLException {
        return retrieve(id, "SELECT * FROM members WHERE id = ?");
    }

    public List<Member> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM members")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Member> members = new ArrayList<>();
                    while (rs.next()) {
                        members.add(mapRow(rs));
                    }
                    return members;
                }
            }
        }
    }

    public List<Member> queryMembersBytaskId(Integer taskId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM members WHERE task_id = ?")) {
                statement.setInt(1, taskId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Member> members = new ArrayList<>();
                    while (rs.next()) {
                        members.add(mapRow(rs));
                    }
                    return members;
                }
            }
        }
    }

    //creating a column of row in which the data will display/stored
    @Override
    protected Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.settaskId((Integer)rs.getObject("task_id"));
        member.setName(rs.getString("member_name"));
        member.setLastName(rs.getString("last_name"));
        member.setEmail(rs.getString("email"));
        member.setAge(rs.getDouble("age"));
        return member;
    }



}

