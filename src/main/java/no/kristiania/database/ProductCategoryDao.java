package no.kristiania.database;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryDao {
    private DataSource dataSource;

    public ProductCategoryDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ProductCategory> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM product_categories")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<ProductCategory> members = new ArrayList<>();
                    while (rs.next()) {
                        members.add(mapRowToCategory(rs));
                    }
                    return members;
                }
            }
        }
    }

    private ProductCategory mapRowToCategory(ResultSet rs) throws SQLException {
        ProductCategory category = new ProductCategory();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        return category;
    }

    public void insert(ProductCategory category) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO product_categories (name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                //getter and setter method
                statement.setString(1, category.getName());
                statement.executeUpdate();
                //setting the keys to id
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    category.setId(generatedKeys.getLong("id"));
                }
            }
        }
    }

    public ProductCategory retrieve(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM product_categories WHERE id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToCategory(rs);
                    } else {
                        return null;
                    }
                }
            }
        }
    }
}
