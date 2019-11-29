import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class PublishSynonyms
{
    //static final String endPoint = "https://privacyoracle.cognitiveservices.azure.com";
    static final String endPoint = "https://privacyoracle.cognitiveservices.azure.com/qnamaker/v4.0/alterations";
    static final String subscriptionKey = "ffb86703a6614408b657e219d4e4368d";
    private static String buildPayload(List<List<String>> syngroups)
    {
        JsonObject inputPayload = new JsonObject();
        JsonArray wordAlterationsList = new JsonArray();
        for(List<String> syngroup: syngroups)
        {
            JsonArray alterationsList = new JsonArray();
            for(String syn: syngroup) { alterationsList.add(syn); }
            if(alterationsList.size()==0) continue;
            JsonObject syngroupjson = new JsonObject();
            syngroupjson.add("alterations", alterationsList);
            wordAlterationsList.add(syngroupjson);
        }
        inputPayload.add("wordAlterations", wordAlterationsList);
        return new Gson().toJson(inputPayload);
    }

    public static void publish(List<List<String>> syngroups) throws Exception
    {
        final String text = buildPayload(syngroups);
        System.out.println("Payload for alterations: " + text);
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder(endPoint);


            URI uri = builder.build();
            HttpPut request = new HttpPut(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            StringEntity reqEntity = new StringEntity(text);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                System.out.println(EntityUtils.toString(entity));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
