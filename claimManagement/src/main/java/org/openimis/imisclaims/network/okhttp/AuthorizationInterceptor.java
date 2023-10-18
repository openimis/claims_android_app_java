package org.openimis.imisclaims.network.okhttp;

import android.view.textclassifier.TextLinks;

import androidx.annotation.NonNull;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.Token;

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

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Token token = global.getJWTToken();
        if (token != null && token.isTokenValidJWT()) {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Authorization", "bearer " + token.getTokenText().trim());
            Response response = chain.proceed(builder.build());
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                global.getJWTToken().clearToken();
            }
            return response;
        }
        return chain.proceed(chain.request());
    }
}
