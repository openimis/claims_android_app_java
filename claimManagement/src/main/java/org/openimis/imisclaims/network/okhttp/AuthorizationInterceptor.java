package org.openimis.imisclaims.network.okhttp;

import android.view.textclassifier.TextLinks;

import androidx.annotation.NonNull;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.Token;
import org.openimis.imisclaims.tools.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {

    @NonNull
    private final Global global;

    public AuthorizationInterceptor(@NonNull Global global) {
        this.global = global;
    }
    private static final String USER_AGENT = "mobile_app";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Token token = global.getJWTToken();
        String csrfToken = global.getCsrfToken();
        if (token != null && token.isTokenValidJWT()) {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Authorization", "bearer " + token.getTokenText().trim());
            builder.addHeader("User-Agent", USER_AGENT);
            if(!csrfToken.isEmpty()){
                builder.addHeader("X-Csrftoken", csrfToken);
            }
            Response response = chain.proceed(builder.build());
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                global.getJWTToken().clearToken();
            }
            return response;
        }
        return chain.proceed(chain.request());
    }
}
