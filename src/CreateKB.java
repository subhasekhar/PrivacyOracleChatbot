import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

class KB {
    String name;
    Question[] qnaList;
    String[] urls;
    File[] files;
    boolean enableHierarchicalExtraction = true;
    String defaultAnswerUsedForExtraction = "Choose from below:";
}

class Question {
    Integer id;
    String answer;
    String source;
    String[] questions;
    Metadata[] metadata;
}

class Metadata {
    String name;
    String value;
}

class File {
    String fileName;
    String fileUri;
}

public class CreateKB {

    String subscriptionKey = "f612349ba6074bd0bbbad267500bb69c";
    String host = "https://privacyoracleapp.cognitiveservices.azure.com";
    String service = "/qnamaker/v4.0";
    String method = "";
    String name = "";
    LinkedList<Question> kbQuestions = new LinkedList<>();
    int questionCounter = 0;

    public CreateKB(final String subscriptionKey, final String host,
    final String service, final String method, final String name,
    final LinkedList<String> qnaStrings)
    {
      this.subscriptionKey = subscriptionKey;
      this.host = host;
      this.service = service;
      this.method = method;
      this.name = name;
      buildQnAs(qnaStrings);
    }

    private void buildQnAs(LinkedList<String> qnas)
    {
        for(String qna: qnas)
        {
            JsonObject jsonObject = new JsonParser().parse(qna).getAsJsonObject();
            for (Map.Entry<String,JsonElement> entry : jsonObject.entrySet()) {
                final String currQuestionString = entry.getKey();
                final String currAnswerString = entry.getValue().toString();
                Question currQuestionObj = new Question();
                currQuestionObj.id = questionCounter;
                currQuestionObj.questions = new String[]{currQuestionString};
                currQuestionObj.answer = currAnswerString;
                kbQuestions.add(currQuestionObj);
                questionCounter++;
            }
        }
    }



    public KB GetKB () {
        KB kb = new KB ();
        kb.name = this.name;

        Question[] qnaArray = new Question[kbQuestions.size()];
        qnaArray = kbQuestions.toArray(qnaArray);
        kb.qnaList = qnaArray;

        return kb;
    }

    public String PrettyPrint (String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    public Response Post (URL url, String content) throws Exception{
    System.out.println("j");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	//System.out.println(connection.getResponseCode());
	System.out.println("a");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", content.length() + "");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);
System.out.println("f");
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        byte[] encoded_content = content.getBytes("UTF-8");
        wr.write(encoded_content, 0, encoded_content.length);
        wr.flush();
        wr.close();
System.out.println("f");
        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
	System.out.println("a");
        return new Response (connection.getHeaderFields(), response.toString());
    }

    public Response Get (URL url) throws Exception{
        System.out.println("j");
	HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
	//System.out.println(connection.getResponseCode());
            System.out.println("a");
	    connection.setRequestMethod("GET");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
            connection.setDoOutput(true);
	    System.out.println("f");
        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
System.out.println("f");
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
	System.out.println("a");
        return new Response (connection.getHeaderFields(), response.toString());
    }

    public Response createKB (KB kb) throws Exception {
        URL url = new URL (host + service + method);
        System.out.println ("Calling " + url.toString() + ".");
        String content = new Gson().toJson(kb);
        System.out.println(content);
        return Post(url, content);
    }

    public Response GetStatus (String operation) throws Exception {
        URL url = new URL (host + service + operation);
        System.out.println ("Calling " + url.toString() + ".");
        return Get(url);
    }

    public Response create() {
        try {
            // Send the request to create the knowledge base.
            Response response = createKB (GetKB ());

            // Get operation ID
            String operation = response.Headers.get("Location").get(0);

            System.out.println (PrettyPrint (response.Response));

            // Loop until the request is completed.
            Boolean done = false;
            while (!done) {
                // Check on the status of the request.
                response = GetStatus (operation);
                System.out.println (PrettyPrint (response.Response));

                Type type = new TypeToken<Map<String, String>>(){}.getType();

                Map<String, String> fields = new Gson().fromJson(response.Response, type);

                String state = fields.get ("operationState");

                // If the request is still running, the server tells us how
                // long to wait before checking the status again.
                if (state.equals("Running") || state.equals("NotStarted")) {
                    String wait = response.Headers.get ("Retry-After").get(0);
                    System.out.println ("Waiting " + wait + " seconds...");
                    Thread.sleep (Integer.parseInt(wait) * 1000);
                }
                else {
                    done = true;
                }
            }
            return response;
        } catch (Exception e) {
            System.out.println (e);
        }
		return null;
    }
}
