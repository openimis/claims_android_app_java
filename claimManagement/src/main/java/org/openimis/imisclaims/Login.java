package org.openimis.imisclaims;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class Login {

    Token token;

    public Login() {
        token = Global.getGlobal().getJWTToken();
    }

    // Login to API and get Token JWT
    public boolean LoginToken(final String Username, final String Password) {
        ToRestApi rest = new ToRestApi();

        JSONObject object = new JSONObject();
        try {
            object.put("UserName", Username);
            object.put("Password", Password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String functionName = "login";

        HttpResponse response;
        String content;

        response = rest.postToRestApi(object, functionName);
        content = rest.getContent(response);

        if (response != null && response.getStatusLine().getStatusCode() == 200 && content != null) {
            JSONObject ob;
            String jwt = "";
            String validTo = "";
            try {
                ob = new JSONObject(content);
                jwt = ob.getString("access_token");
                validTo = ob.getString("expires_on");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            token.saveTokenText(jwt, validTo);

            return true;
        }

        return false;
    }
}
