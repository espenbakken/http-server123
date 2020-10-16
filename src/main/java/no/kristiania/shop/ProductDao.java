package no.kristiania.shop;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDao {
    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/kristianiashop");
        dataSource.setUser("kristianiashop");
        dataSource.setPassword("sdlkgnslkawat");

        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from products");
        ResultSet rs = statement.executeQuery();
        while(rs.next()) {
            String productName = rs.getString("product_name");
            System.out.println(productName);
        }
    }
}
