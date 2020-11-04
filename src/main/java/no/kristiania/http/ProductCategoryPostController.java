package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.ProductCategory;
import no.kristiania.database.ProductCategoryDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ProductCategoryPostController implements ControllerMcControllerface{
    private ProductCategoryDao productCategoryDao;

    public ProductCategoryPostController(ProductCategoryDao productCategoryDao) {

        this.productCategoryDao = productCategoryDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        ProductCategory category = new ProductCategory();
        category.setName(requestParameter.getParameter("categoryName"));
        productCategoryDao.insert(category);

        String body = "Gruppemedlem er lagt til i databasen!";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;
        clientSocket.getOutputStream().write(response.getBytes());
    }
}
