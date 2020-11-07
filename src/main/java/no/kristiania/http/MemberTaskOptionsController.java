package no.kristiania.http;

import no.kristiania.database.MemberTask;
import no.kristiania.database.MemberTaskDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class MemberTaskOptionsController implements HttpController{
    private MemberTaskDao taskDao;

    public MemberTaskOptionsController(MemberTaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        String requestTarget = request.getStartLine().split(" ")[1];
        int questionPos = requestTarget.indexOf('?');
        Integer taskId = null;
        if (questionPos != -1){
            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            taskId = Integer.valueOf(queryString.getParameter("taskId"));
        }
        HttpMessage response = new HttpMessage(getBody(taskId));
        response.write(clientSocket);
    }

    public String getBody(Integer taskId) throws SQLException {
        String body = "";
        List<MemberTask> list = taskDao.list();
        for (int i = 0, listSize = list.size(); i < listSize; i++){
            MemberTask task = list.get(i);
            if (taskId != null && taskId.equals(task.getId())){
                body += "<option value=" + task.getId() + " selected='selected'>" + task.getName() + "</option>";
            } else {
                body += "<option value=" + task.getId() + ">" + task.getName() + "</option>";
            }
        }
        return body;
    }
}
