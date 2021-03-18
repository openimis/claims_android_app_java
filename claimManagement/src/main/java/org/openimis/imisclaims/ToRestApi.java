package org.openimis.imisclaims;

import org.openimis.general.General;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Hiren on 14/02/2019.
 */

public class ToRestApi {
    General general = new General();
    Token tokenl = new Token();
    private String uri = general.getDomain() + "api/";

    private static final String CHF_FUNCTIONS = "GetDiagnosesServicesItems,GetClaims,Claims/Controls," +
            "Claims/GetClaimAdmins";

    //Post without Token
    public HttpResponse postToRestApi(final JSONObject object, final String functionName) throws IOException {
        setProperUri(functionName);
        HttpResponse response = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri + functionName);

        StringEntity postingString = new StringEntity(object.toString());
        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");
        response = httpClient.execute(httpPost);

        return response;
    }

    //Post with Token
    public HttpResponse postToRestApiToken(final JSONObject object, final String functionName) throws IOException {
        setProperUri(functionName);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri + functionName);

        StringEntity postingString = new StringEntity(object.toString());
        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", "bearer " + tokenl.getTokenText());

        HttpResponse response = null;
        response = httpClient.execute(httpPost);

        return response;
    }

    // Post without Token, returned object
    public String postObjectToRestApiObjectToken(final JSONObject object, final String functionName) throws IOException {
        setProperUri(functionName);
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri + functionName);

        StringEntity postingString = new StringEntity(object.toString());
        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", "bearer " + tokenl.getTokenText());

        HttpResponse response = null;
        response = httpClient.execute(httpPost);

        HttpEntity respEntity = (response!=null)?response.getEntity():null;

        content = (respEntity!=null)?EntityUtils.toString(respEntity):null;

        return content;
    }

    // Get without Token
    public String getFromRestApi(final String functionName) throws IOException {
        setProperUri(functionName);
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");

        HttpResponse response = null;
        response = httpClient.execute(httpGet);

        HttpEntity respEntity = (response!=null)?response.getEntity():null;

        content = (respEntity!=null)?EntityUtils.toString(respEntity):null;

        return content;
    }

    // Get with Token, returned object
    public String getObjectFromRestApiToken(final String functionName) throws IOException{
        setProperUri(functionName);
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri + functionName);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Authorization", "bearer " + tokenl.getTokenText());

        HttpResponse response = null;
        response = httpClient.execute(httpGet);

        HttpEntity respEntity = (response!=null)?response.getEntity():null;

        content = (respEntity!=null)?EntityUtils.toString(respEntity):null;

        return content;
    }

    private boolean isChfFunction(String functionName) {
        List<String> functions = Arrays.asList(CHF_FUNCTIONS.split(","));
        for (String function : functions) {
            if (function.equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    private void setProperUri(String functionName) {
        if (isChfFunction(functionName)) {
            uri = general.getDomainCHF() + "api/";
        }
        else {
            uri = general.getDomain() + "api/";
        }
    }
}
