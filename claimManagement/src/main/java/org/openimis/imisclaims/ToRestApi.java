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
    public HttpResponse postToRestApi(final JSONObject object, final String functionName) {
        setProperUri(functionName);
        HttpResponse response = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    //Post with Token
    public HttpResponse postToRestApiToken(final JSONObject object, final String functionName) {
        setProperUri(functionName);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Post without Token, returned object
    public String postObjectToRestApiObjectToken(final JSONObject object, final String functionName) {
        setProperUri(functionName);
        final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
    }

    // Get without Token
    public String getFromRestApi(final String functionName) {
        setProperUri(functionName);
        final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
    }

    // Get with Token, returned object
    public String getObjectFromRestApiToken(final String functionName) {
        setProperUri(functionName);
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Authorization", "bearer "+tokenl.getTokenText());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            if (uri.endsWith("rest/api/")) {
                uri = uri.substring(0, uri.length() - 9);
                uri = uri + "rest-chf/api/";
            }
        }
    }
}
