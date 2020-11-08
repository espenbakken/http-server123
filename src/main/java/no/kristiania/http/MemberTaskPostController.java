package no.kristiania.http;

import no.kristiania.database.MemberTask;
import no.kristiania.database.MemberTaskDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class MemberTaskPostController implements HttpController {
    private MemberTaskDao memberTaskDao;

    public MemberTaskPostController(MemberTaskDao memberTaskDao) {

        this.memberTaskDao = memberTaskDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        MemberTask task = new MemberTask();
        task.setName(requestParameter.getParameter("taskName"));
        memberTaskDao.insert(task);

        String response = "HTTP/1.1 302 Redirect\r\n" +
                "Connection: close\r\n" +
                "Location: http://localhost:8080/index.html\r\n" +
                "\r\n";
        clientSocket.getOutputStream().write(response.getBytes());
    }
}
