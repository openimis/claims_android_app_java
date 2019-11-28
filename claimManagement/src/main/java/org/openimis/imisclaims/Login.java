package org.openimis.imisclaims;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Login {

    Token tokenl;

    public Login(){
        tokenl = new Token();
    }

    // Login to API and get Token JWT
    public boolean LoginToken(final String Username, final String Password) throws InterruptedException {
        ToRestApi rest = new ToRestApi();

        JSONObject object = new JSONObject();
        try {
            object.put("UserName",Username);
            object.put("Password",Password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String functionName = "login";

        HttpResponse response = rest.postToRestApi(object, functionName);

        String content = null;

        HttpEntity respEntity = response.getEntity();

        if (respEntity != null) {
            try {
                content = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(response.getStatusLine().getStatusCode() == 200){
            JSONObject ob = null;
            String jwt = null;
            try {
                ob = new JSONObject(content);
                jwt = ob.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tokenl.saveTokenText(jwt);

            return true;
        }
        return false;
    }
}
