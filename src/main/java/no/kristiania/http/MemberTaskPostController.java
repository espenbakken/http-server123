package no.kristiania.http;

import no.kristiania.database.MemberTask;
import no.kristiania.database.MemberTaskDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class MemberTaskPostController implements HttpController {
    private MemberTaskDao MemberTaskDao;

    public MemberTaskPostController(MemberTaskDao MemberTaskDao) {

        this.MemberTaskDao = MemberTaskDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        MemberTask task = new MemberTask();
        task.setName(requestParameter.getParameter("taskName"));
        MemberTaskDao.insert(task);

        String body = "Ny kategori er lagt til!";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;
        clientSocket.getOutputStream().write(response.getBytes());
    }
}
