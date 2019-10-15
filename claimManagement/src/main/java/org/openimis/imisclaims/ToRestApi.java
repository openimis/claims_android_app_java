package org.openimis.imisclaims;

import org.openimis.general.General;

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
import java.io.UnsupportedEncodingException;

/**
 * Created by Hiren on 14/02/2019.
 */

public class ToRestApi {
    General general = new General();
    Token tokenl = new Token();
    private String uri = general.getDomain() + "api/";

    //Post without Token
    public HttpResponse postToRestApi(final JSONObject object, final String functionName) {
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
}
