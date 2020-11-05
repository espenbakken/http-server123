package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ProductOptionsController implements HttpController {
    private MemberDao memberDao;

    public ProductOptionsController(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        String body = getBody();
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public String getBody() throws SQLException {
        String body = "";
        for (Member member : memberDao.list()) {
            body += "<option value=" + member.getId() + ">" + member.getName() + "</option>";
        }


        return body;
    }
}
