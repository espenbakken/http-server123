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

public class ProductDao {

    private DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiashop");
        dataSource.setUser("kristianiashop");
        dataSource.setPassword("sdlkgnslkawat");

        ProductDao productDao = new ProductDao(dataSource);

        System.out.println("Please enter product name:");
        Scanner scanner = new Scanner(System.in);
        String productName = scanner.nextLine();

        productDao.insert(productName);
        for (String product : productDao.list()) {
            System.out.println(product);

        }


        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO products (product_name) VALUES (?)")) {
                statement.setString(1, productName);
                statement.executeUpdate();
            }
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from products")) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getString("product_name"));
                    }
                }
            }
        }
    }

    public void insert(String product) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO products (product_name) VALUES (?)")) {
                statement.setString(1, product);
                statement.executeUpdate();
            }
        }
    }

    public List<String> list() throws SQLException {
        List<String> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from products")) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        products.add(rs.getString("product_name"));
                    }
                }
            }
        }
        return products;
    }
}


