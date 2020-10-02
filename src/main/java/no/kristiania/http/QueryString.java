
package no.kristiania.http;

import java.util.HashMap;
import java.util.Map;

public class QueryString {
    private final Map<String, String> parameters = new HashMap<>();

    public QueryString(String queryString) {
        String[] parts = queryString.split("&");
        for (String parameter : parts) {
            int equalPos = parameter.indexOf('=');
            String parameterName = parameter.substring(0, equalPos);
            String parameterValue = parameter.substring(equalPos + 1);
            parameters.put(parameterName, parameterValue);
        }
    }

    public String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }


}
