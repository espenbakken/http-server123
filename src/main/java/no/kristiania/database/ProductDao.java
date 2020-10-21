package no.kristiania.database;


import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class ProductDao {

    private final DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Product projectMembers) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO projectMembers (member_name) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, projectMembers.getName());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();

                }
            }
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product projectMembers = new Product();
        projectMembers.setName(rs.getString("member_name"));
        return projectMembers;
    }

    public List<Product> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM projectMember")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Product> projectMember = new ArrayList<>();
                    while (rs.next()) {
                        projectMember.add(mapRowToProduct(rs));
                    }
                    return projectMember;
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {

        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")){
        properties.load(fileReader);} catch (IOException e) {
            e.printStackTrace();
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.username"));
        // TODO: database passwords should never be checked in!
        dataSource.setPassword(properties.getProperty("dataSource.password"));

        ProductDao productDao = new ProductDao(dataSource);

        System.out.println("What's the name of the new projectMember");
        Scanner scanner = new Scanner(System.in);

        Product projectMember = new Product();
        projectMember.setName(scanner.nextLine());

        productDao.insert(projectMember);
        System.out.println(productDao.list());
    }

}