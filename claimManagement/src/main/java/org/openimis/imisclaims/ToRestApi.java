package org.openimis.imisclaims;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

import static org.openimis.imisclaims.BuildConfig.API_BASE_URL;
import static org.openimis.imisclaims.BuildConfig.API_VERSION;

public class ToRestApi {
    private final Token token;
    private final String uri;
    private final String apiVersion;

    public ToRestApi() {
        token = Global.getGlobal().getJWTToken();
        uri = API_BASE_URL + "api/";
        apiVersion = API_VERSION;
    }

    public HttpResponse getFromRestApi(String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri + functionName);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("api-version", apiVersion);
        if (addToken) {
            httpGet.setHeader("Authorization", "bearer " + token.getTokenText().trim());
        }

        try {
            HttpResponse response = httpClient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("HTTP_GET", uri + functionName + " - " + responseCode);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(JSONObject object, String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(uri + functionName);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("api-version", apiVersion);
        if (addToken) {
            httpPost.setHeader("Authorization", "bearer " + token.getTokenText().trim());
        }

        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            HttpResponse response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("HTTP_POST", uri + functionName + " - " + responseCode);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(JSONObject object, String functionName) {
        return postToRestApi(object, functionName, false);
    }

    public HttpResponse postToRestApiToken(JSONObject object, String functionName) {
        return postToRestApi(object, functionName, true);
    }

    public String getFromRestApi(String functionName) {
        HttpResponse response = getFromRestApi(functionName, false);
        return getContent(response);
    }

    public String getFromRestApiToken(String functionName) {
        HttpResponse response = getFromRestApi(functionName, true);
        return getContent(response);

    }

    public String getContent(HttpResponse response) {
        try {
            HttpEntity respEntity = (response != null) ? response.getEntity() : null;
            return (respEntity != null) ? EntityUtils.toString(respEntity) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
