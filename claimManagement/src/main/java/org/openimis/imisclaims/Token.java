package org.openimis.imisclaims;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Token {
    public void saveTokenText(String token, String validTo) {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        File tokenFile = new File(dir, "token.txt");
        File validToFile = new File(dir, "validTo.txt");

        global.writeText(tokenFile, token);
        global.writeText(validToFile, validTo);
    }

    public String getTokenText() {
        String token = "";

        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        if (isTokenValidJWT()) {
            token = global.getFileText(dir, "token.txt");
        } else {
            clearToken();
        }

        return token;
    }

    public void clearToken() {
        saveTokenText("", "");
    }

    //How to validate JWT:
    //https://datatracker.ietf.org/doc/html/rfc7519#section-7.2
    public boolean isTokenValidJWT() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        String validTo = global.getFileText(dir, "validTo.txt");
        String token = global.getFileText(dir, "token.txt");

        if (token == null || "".equals(token))
            return false;

        int indexOfFirstDot = token.indexOf('.');
        if (indexOfFirstDot == -1)
            return false;

        String tokenHeader = token.substring(0, indexOfFirstDot);
        try {
            JSONObject headerObject = new JSONObject(new String(Base64.decode(tokenHeader, Base64.DEFAULT)));
            if (!"JWT".equals(headerObject.getString("typ"))) {
                return false;
            }
        } catch (JSONException e) {
            return false;
        }

        if (validTo == null || "".equals(validTo)) {
            return false;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.US);
            Date expiryDate = format.parse(validTo);
            Date now = new Date();

            if (now.after(expiryDate)) {
                return false;
            }
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }

        return true;
    }
}

