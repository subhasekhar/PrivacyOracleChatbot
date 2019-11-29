import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class LUIS
{
    static final String appId = "acd7e09e-259e-47de-ac1a-16c763e43f30";
    static final String version = "0.1";
    static final String subscriptionKey = "12d23ac95af1478984b44d5f77ec122f";

    public static void createIntent(final String intent)
    {
        HttpClient httpclient = HttpClients.createDefault();
        try
        {
            URIBuilder builder = new URIBuilder(
                    "https://westus.api.cognitive.microsoft.com/luis/api/v2.0/apps/" + appId + "/versions/" + version + "/intents");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body
            final String body = "{\"name\": \"" + intent + "\"}";
            //System.out.println(body);
            StringEntity reqEntity = new StringEntity(body);
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
    public static void createEntity(final String entityName)
    {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder(
                    "https://westus.api.cognitive.microsoft.com/luis/api/v2.0/apps/" + appId + "/versions/" + version + "/entities");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            final String body = "{\"name\": \"" + entityName + "\"}";
            StringEntity reqEntity = new StringEntity(body);
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

    public static void addUtterances(List<String> utterances, final String intent)
    {
        JsonArray payloadJson = new JsonArray();
        for(String utterance: utterances)
        {
            JsonObject currentPayload = new JsonObject();
            currentPayload.addProperty("text", utterance);
            currentPayload.addProperty("intentName", intent);
            payloadJson.add(currentPayload);
        }
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder(
                    "https://westus.api.cognitive.microsoft.com/luis/api/v2.0/apps/"+appId+"/versions/"+version+"/examples");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            StringEntity reqEntity = new StringEntity(new Gson().toJson(payloadJson));
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

    public static void train()
    {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/luis/api/v2.0/apps/"+appId+"/versions/"+version+"/train");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            StringEntity reqEntity = new StringEntity("");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                System.out.println(EntityUtils.toString(entity));
            }
            System.out.println("Waiting for 60sec for the LUIS model to finish training");
            Thread.sleep(60000);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void publish()
    {
        HttpClient httpclient = HttpClients.createDefault();
        try
        {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/luis/api/v2.0/apps/"+appId+"/publish");
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            final String body = "{\"versionId\": \"0.1\",\"isStaging\": false,\"directVersionPublish\": false}";
            StringEntity reqEntity = new StringEntity(body);
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
/*    public static void main(String[] args)
    {
        List<String> utts = new LinkedList<>();
        utts.add("hi hello how are you"); utts.add("ippudu how are you antav. tarvata who are you antav");
        createIntent("kufla");
        createEntity("kaikachori");
        addUtterances(utts, "kufla");
        train();
        publish();
    }*/
}
