package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateMemberController implements HttpController{
    private MemberDao memberDao;

    public UpdateMemberController(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        HttpMessage response = handle(request);
        response.write(clientSocket);
    }

    public HttpMessage handle(HttpMessage request) throws SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Integer memberId = Integer.valueOf(requestParameter.getParameter("memberId"));
        Integer taskId = Integer.valueOf(requestParameter.getParameter("taskId"));
        Member member = memberDao.retrieve(memberId);
        member.settaskId(taskId);

        memberDao.update(member);

        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 Redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/index.html");

        return redirect;
    }


}
