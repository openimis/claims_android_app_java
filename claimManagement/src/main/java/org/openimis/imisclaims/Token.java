package org.openimis.imisclaims;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Deprecated
public class Token {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.US);

    public void saveTokenText(String token, String validTo) {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        File tokenFile = new File(dir, "token.txt");
        File validToFile = new File(dir, "validTo.txt");

        global.writeText(tokenFile, token);
        global.writeText(validToFile, validTo);
    }

    public String getTokenText() {

        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");
        String token = global.getFileText(dir, "token.txt");
        if (isTokenValidJWT(token)) {
            return token;
        } else {
            clearToken();
        }

        return null;
    }

    @Nullable
    public Date getValidity() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        String validTo = global.getFileText(dir, "validTo.txt");
        if (validTo == null || "".equals(validTo)) {
            return null;
        }

        try {
            return format.parse(validTo);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearToken() {
        saveTokenText("", "");
    }

    //How to validate JWT:
    //https://datatracker.ietf.org/doc/html/rfc7519#section-7.2

    public boolean isTokenValidJWT() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");
        return isTokenValidJWT(Global.getGlobal().getFileText(dir, "token.txt"));
    }

    private boolean isTokenValidJWT(@Nullable String token) {

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
        Date expiryDate = getValidity();
        if (expiryDate == null) {
            return false;
        }

        Date now = new Date();
        return !now.after(expiryDate);
    }
}

