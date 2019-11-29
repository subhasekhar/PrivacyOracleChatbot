import java.util.List;
import java.util.Map;

public class Response {
    Map<String, List<String>> Headers;
    String Response;

    public Response(Map<String, List<String>> headers, String response) {
        this.Headers = headers;
        this.Response = response;
    }
}
