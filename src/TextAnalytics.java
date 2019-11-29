import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

class Document {
    public String id, language, text;

    public Document(String id, String language, String text){
        this.id = id;
        this.language = language;
        this.text = text;
    }
}

class Documents {
    public List<Document> documents;

    public Documents() {
        this.documents = new ArrayList<Document>();
    }
    public void add(String id, String language, String text) {
        this.documents.add (new Document (id, language, text));
    }
}

public class TextAnalytics
{
    static final String subscriptionKey = "fda2ba25ca444140a1cd61b83e68f10c";
    static final String endPoint = "https://privacyoracle-textanalytics.cognitiveservices.azure.com/";
    static final String path = "/text/analytics/v2.1/keyPhrases";

    private static JsonObject jsonify(final String qnaString)
    {
        return new JsonParser().parse(qnaString).getAsJsonObject();
    }

    private static String GetKeyPhrases (Documents documents) throws Exception {
        String text = new Gson().toJson(documents);
        System.out.println("Input to server: " + text);
        byte[] encoded_text = text.getBytes("UTF-8");

        URL url = new URL(endPoint+path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/json");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(encoded_text, 0, encoded_text.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private static Documents buildDocument(JsonObject jsonObject)
    {
        Documents documents = new Documents();
        int documentCounter = 0;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            if (documentCounter > 995) break;
            final String currQuestionString = entry.getKey();
            final String currAnswerString = entry.getValue().toString();
            if(currQuestionString.length() > 5000)
                documents.add(String.valueOf(documentCounter++), "en", currQuestionString.substring(0, 4999));
            else
                documents.add(String.valueOf(documentCounter++), "en", currQuestionString);
            if(currAnswerString.length() > 5000)
                documents.add(String.valueOf(documentCounter++), "en", currAnswerString.substring(0, 4999));
            else
                documents.add(String.valueOf(documentCounter++), "en", currAnswerString);
            //System.out.println(currQuestionString);
            //System.out.println(currAnswerString);
        }
        return documents;
    }

    public static String prettify(String json_text)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

/*    public List<String> obtainKeywords(final String qnaString) throws Exception
    {
        JsonObject jsonObject = jsonify(qnaString);
        Documents documents = buildDocument(jsonObject);
        String response = GetKeyPhrases(documents);
        System.out.println (prettify (response));
    }*/

    public static Set<String> buildListFromResponse(final String response)
    {
        Set<String> words = new HashSet<>();
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        JsonArray docs = jsonObject.getAsJsonArray("documents").getAsJsonArray();
        for(JsonElement doc: docs)
        {
            JsonArray curr_phrase_list = doc.getAsJsonObject().getAsJsonArray("keyPhrases");
            for(JsonElement curr_phrase: curr_phrase_list)
            {
                words.addAll(Arrays.asList(curr_phrase.toString().replace("\"", "").split(" ")));
            }
        }
        return words;
    }

    public Set<String> obtainKeywords(final String qnaString) throws Exception
    {
        //String qnaString = "{\"what is your favourite color?\":\"I am a brilliant student\"," +
          //      " \"India is the greatest country\":\"Tesla is a very innovative company in current standards\"}";
        JsonObject jsonObject = jsonify(qnaString);
        Documents documents = buildDocument(jsonObject);
        String response = GetKeyPhrases(documents);
        System.out.println (prettify (response));
        //System.out.println(buildListFromResponse(response));
        return buildListFromResponse(response);
    }
}
