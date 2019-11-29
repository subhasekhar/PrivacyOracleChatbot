import java.io.*;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

public class Main
{
    static final String subscriptionKey = "ffb86703a6614408b657e219d4e4368d";
    static final String host = "https://privacyoracle.cognitiveservices.azure.com";
    static final String service = "/qnamaker/v4.0";
    static final int SYNONYM_COUNT = 3;
    static TextAnalytics textAnalytics = new TextAnalytics();
    static List<String> urls = new LinkedList<>();
    static String name = new String();
    static final String recordPath = "C:\\MS\\ncsu\\SUBJECTS\\thirdsem\\privacy\\PrivacyOracle\\PrivacyOracle\\src\\records.json";

    public static Response Get (URL url) throws Exception{
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);
        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return new Response (connection.getHeaderFields(), response.toString());
    }

    private static String createKB(final String name, final LinkedList<String> qnaStrings)
    {
        CreateKB createKB = new CreateKB(subscriptionKey, host, service,
                "/knowledgebases/create", name, qnaStrings);
        Response createResponse = createKB.create();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> fields = new Gson().fromJson(createResponse.Response, type);
        String resourceLocation = fields.get ("resourceLocation");
        return resourceLocation.substring(resourceLocation.lastIndexOf("/") + 1);
    }

    private static String parse(final String url) throws Exception
    {
        System.out.println("Parsing URL: " + url);
        ProcessBuilder processBuilder = new ProcessBuilder("python",
                "C:\\MS\\ncsu\\SUBJECTS\\thirdsem\\privacy\\PrivacyOracle\\PrivacyOracle\\src\\parser.py",url);
        Process parserRunProcess = processBuilder.start();
        BufferedReader parserOutputBuffer = new BufferedReader(new InputStreamReader(parserRunProcess.getInputStream()));
        String line;
        String qna = "";
        while((line = parserOutputBuffer.readLine()) != null)
            qna += line;
        //String qna = parserOutputBuffer.readLine();
        System.out.println("QnA found from " + url + "\n" + qna);
        parserRunProcess.destroy();
        return qna;
    }

    private static void askInputs()
    {
        System.out.println("Enter a valid, unique name of domain/org for which privacy policies need to be fetched. " +
                "This input will be used as Knowledge Base name, LUIS Intent name(with word 'Intent' at the end) and LUIS entity name");
        Scanner scanner = new Scanner(System.in);
        name = scanner.nextLine();

        System.out.println("Enter comma separated list of urls");
        String allurlsString = scanner.nextLine();
        allurlsString = allurlsString.replace(" ", "");
        String[] allurls = allurlsString.split(",");
        urls.addAll(Arrays.asList(allurls));

    }

    private static List<String> getQuestions(List<String> qnaStrings)
    {
        List<String> questions = new LinkedList<>();
        for(String qna: qnaStrings) {
            JsonObject jsonObject = new JsonParser().parse(qna).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String question = entry.getKey();
                question = question.replace("\"", "");
                questions.add(question);
            }
        }
        return questions;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("---- Welcome to Privacy Oracle builder ----");

        askInputs();

        LinkedList<String> qnaStrings = new LinkedList<>();
        for(int i=0;i<urls.size();i++)
        {
            final String qnaString = parse(urls.get(i));
            qnaStrings.add(qnaString);
        }
        //create KB
        final String kbid = createKB(name, qnaStrings);
        System.out.println("Knowledge Base created with kbid: " + kbid);

        //obtain keywords
        List<String> keywords = new LinkedList<>();
        for(String qnaString: qnaStrings)
        {
            keywords.addAll(textAnalytics.obtainKeywords(qnaString));
        }
        System.out.println(keywords);

        //fetch and publish synonyms
        fetchandpublishSynonyms(keywords);

        //publish KB
        publishKB(kbid);

        final String intent = name + "Intent";
        setupLUIS(intent, name, getQuestions(qnaStrings));

        createRecordEntry(name, intent, kbid);
    }

    static void createRecordEntry(final String name, final String intent, final String kbid) throws Exception
    {
        //read from file
        File file = new File(recordPath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String fileContentAsString = "";
        String line = "";
        while ((line = br.readLine()) != null){ fileContentAsString +=  line;}
        JsonObject fileContentAsJson = new JsonParser().parse(fileContentAsString).getAsJsonObject();
        JsonArray names = fileContentAsJson.getAsJsonArray("names");
        JsonArray records = fileContentAsJson.getAsJsonArray("records");

        //create new entry
        JsonObject newEntry = new JsonObject();
        newEntry.addProperty("name", name);
        newEntry.addProperty("intent", intent);
        newEntry.addProperty("kbid", kbid);
        records.add(newEntry);
        names.add(name);

        //create and write to new json
        JsonObject outputJson = new JsonObject();
        outputJson.add("names", names);
        outputJson.add("records", records);

        //write to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(recordPath));
        writer.write(new Gson().toJson(fileContentAsJson));
        writer.close();
    }

    private static void setupLUIS(final String intent, final String entity, List<String> utterances)
    {
        LUIS.createIntent(intent);
        LUIS.createEntity(entity);
        LUIS.addUtterances(utterances, intent);
        LUIS.train();
        LUIS.publish();
    }

    private static void fetchandpublishSynonyms(final List<String> keywords) throws Exception
    {
        List<List<String>> syngroups = new LinkedList<>();
        for(String keyword: keywords)
        {
            syngroups.add(Synonym.getTopSynonyms(keyword, SYNONYM_COUNT));
        }
        PublishSynonyms.publish(syngroups);
    }

    private static void publishKB(final String kbid) throws Exception
    {
        // Create http client
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost request = new HttpPost(host + service + "/knowledgebases/" + kbid);

        // set authorization
        request.setHeader("Ocp-Apim-Subscription-Key",subscriptionKey);

        // Send request to Azure service, get response
        HttpResponse response = httpclient.execute(request);

        // No returned content, 204 == success
        System.out.println(response.getStatusLine().getStatusCode());
    }
}