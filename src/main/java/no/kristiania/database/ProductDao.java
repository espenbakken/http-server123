package no.kristiania.database;


import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.Statement;

public class ProductDao {
    //data source is used for connecting to the actual data source
    private final DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Product product) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO products (product_name, age, last_name, email) values (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                //getter and setter method
                statement.setString(1, product.getName());
                statement.setDouble(2, product.getAge());
                statement.setString(3, product.getLastName());
                statement.setString(4, product.getEmail());
                statement.executeUpdate();
                //setting the keys to id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    product.setId(generatedKeys.getLong("id"));
                }
            }
        }
    }

    public Product retrieve(Long id) throws SQLException {
        //connecting with a specific database and it gives information about the tables
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE id = ?")) {
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
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("product_name"));
        product.setLastName(rs.getString("last_name"));
        product.setEmail(rs.getString("email"));
        product.setAge(rs.getDouble("age"));
        return product;
    }

    public List<Product> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM products")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Product> products = new ArrayList<>();
                    while (rs.next()) {
                        products.add(mapRowToProduct(rs));
                    }
                    return products;
                }
            }
        }
    }


    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiashop");
        dataSource.setUser("kristianiashop");
        dataSource.setPassword("sdlkgnslkawat");

        ProductDao productDao = new ProductDao(dataSource);

        System.out.println("Please enter product name:");
        Scanner scanner = new Scanner(System.in);

        Product product = new Product();
        product.setName(scanner.nextLine());

        productDao.insert(product);
        System.out.println(productDao.list());
    }
}
//Git test
