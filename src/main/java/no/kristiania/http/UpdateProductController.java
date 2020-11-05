package no.kristiania.http;

import no.kristiania.database.Member;
import no.kristiania.database.MemberDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateProductController implements HttpController{
    private MemberDao memberDao;

    public UpdateProductController(MemberDao memberDao) {
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
        Integer categoryId = Integer.valueOf(requestParameter.getParameter("categoryId"));
        Member member = memberDao.retrieve(memberId);
        member.setCategoryId(categoryId);

        memberDao.update(member);

        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 Redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/index.html");

        return redirect;
    }


}
