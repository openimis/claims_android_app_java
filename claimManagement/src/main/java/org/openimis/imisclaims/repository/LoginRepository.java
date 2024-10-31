package org.openimis.imisclaims.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.Token;
import org.openimis.imisclaims.util.StringUtils;

import java.util.Date;

public class LoginRepository {
    private static final String PREFS_NAME = "LoginRepository";
    private static final String HAS_MIGRATED = "has_migrated";
    private static final String FHIR_TOKEN = "fhir_token";
    private static final String FHIR_VALIDITY = "fhir_validity";

    private final SharedPreferences prefs;

    public LoginRepository(@NonNull Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(HAS_MIGRATED, false)) {
            migrateOldTokens();
        }
    }

    @SuppressWarnings("deprecation")
    private void migrateOldTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        Token token = new Token();
        if (token.isTokenValidJWT()) {
            editor.putString(FHIR_TOKEN, token.getTokenText());
            Date validity = token.getValidity();
            if (validity != null) {
                editor.putLong(FHIR_VALIDITY, validity.getTime());
            }
        }
        editor.putBoolean(HAS_MIGRATED, true);
        editor.apply();
    }
    /**
     * Logic taken from [Token.java]
     */
    @Nullable
    public String getToken() {
        String token = prefs.getString(FHIR_TOKEN, null);
        if (token == null) {
            return null;
        }

        int indexOfFirstDot = token.indexOf('.');
        if (indexOfFirstDot == -1) {
            return null;
        }

        String tokenHeader = token.substring(0, indexOfFirstDot);
        try {
            JSONObject headerObject = new JSONObject(new String(Base64.decode(tokenHeader, Base64.DEFAULT)));
            if (!"JWT".equals(headerObject.getString("typ"))) {
                return null;
            }
        } catch (JSONException e) {
            return null;
        }

        Date expiryDate = getValidity();
        if (expiryDate == null) {
            return null;
        }

        Date now = new Date();
        if (now.after(expiryDate)) {
            return null;
        }
        return token;
    }

    @Nullable
    private Date getValidity() {
        long validTo = prefs.getLong(FHIR_VALIDITY, -1);
        if (validTo == -1) {
            return null;
        }
        return new Date(validTo);
    }

    public void saveToken(
            @Nullable String token, @Nullable Date validity
    ) {
        SharedPreferences.Editor editor = prefs.edit();
        if (StringUtils.isEmpty(token)) {
            editor.remove(FHIR_TOKEN);
        } else {
            editor.putString(FHIR_TOKEN, token);
        }
        if (validity == null) {
            editor.remove(FHIR_VALIDITY);
        } else {
            editor.putLong(FHIR_VALIDITY, validity.getTime());
        }
        editor.apply();
    }
}
