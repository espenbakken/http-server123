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
    //data source is used for connecting to the actual data source
    private final DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Product projectMembers) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO products (member_name, id) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                //getter and setter method
                statement.setString(1, projectMembers.getName());
                statement.setDouble(2, projectMembers.getId());
                statement.executeUpdate();

                //setting the keys to id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    projectMembers.setId(generatedKeys.getLong("id"));
                }
            }
        }
    }

    public Product retrieve(Long id) throws SQLException {
        //connecting with a specific database and it gives information about the tables
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM product WHERE id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToProduct(rs);
                    } else {
                        return null;
                    }
                }
            }
        }
    }
    //creating a column of row in which the data will display/stored
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product projectMembers = new Product();
        projectMembers.setId(rs.getLong("id"));
        projectMembers.setName(rs.getString("member_name"));
        return projectMembers;
    }

    public List<Product> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM projectMembers")) {
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
        //to access the specified data, this properties and filereader is created
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //should never commit the file that contains sensitive data
        //therefore it is stored somewherelse

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.username"));
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
