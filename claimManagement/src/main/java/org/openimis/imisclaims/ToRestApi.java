package org.openimis.imisclaims;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openimis.imisclaims.tools.Log;

import java.io.IOException;

import static org.openimis.imisclaims.BuildConfig.API_BASE_URL;
import static org.openimis.imisclaims.BuildConfig.API_VERSION;
import static java.lang.Math.min;

public class ToRestApi {
    private static final String LOG_TAG = "HTTP";
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
            Log.i(LOG_TAG, String.format("request: GET %s%s", uri, functionName));

            HttpResponse response = httpClient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            String responsePhrase = response.getStatusLine().getReasonPhrase();
            Log.i(LOG_TAG, String.format("response: %d %s", responseCode, responsePhrase));

            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(Object object, String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(uri + functionName);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("api-version", apiVersion);
        if (addToken) {
            httpPost.setHeader("Authorization", "bearer " + token.getTokenText().trim());
        }

        try {
            String entity = object.toString();
            StringEntity postingString = new StringEntity(entity);
            httpPost.setEntity(postingString);
            Log.i(LOG_TAG, String.format("request: POST %s%s", uri, functionName));
            Log.v(LOG_TAG, "request content: " + entity.substring(0, min(entity.length(), 1000)));

            HttpResponse response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            String responsePhrase = response.getStatusLine().getReasonPhrase();
            Log.i(LOG_TAG, String.format("response: %d %s", responseCode, responsePhrase));

            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(Object object, String functionName) {
        return postToRestApi(object, functionName, false);
    }

    public HttpResponse postToRestApiToken(Object object, String functionName) {
        return postToRestApi(object, functionName, true);
    }

    public String getFromRestApi(String functionName) {
        HttpResponse response = getFromRestApi(functionName, false);
        return getContent(response);
    }

    public HttpResponse getFromRestApiToken(String functionName) {
        return getFromRestApi(functionName, true);

    }

    public String getContent(HttpResponse response) {
        try {
            HttpEntity respEntity = (response != null) ? response.getEntity() : null;
            String content = (respEntity != null) ? EntityUtils.toString(respEntity) : null;
            Log.v(LOG_TAG, "response content: " + (content != null ? content.substring(0, min(content.length(), 1000)) : "null"));
            return content;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error when extracting response body", e);
            return null;
        }
    }


}
