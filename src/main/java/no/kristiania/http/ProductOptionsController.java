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
        HttpMessage response = new HttpMessage(getBody());
        response.write(clientSocket);
    }

    public String getBody() throws SQLException {
        String body = "";
        for (Member member : memberDao.list()) {
            body += "<option value=" + member.getId() + ">" + member.getName() + "</option>";
        }


        return body;
    }
}
