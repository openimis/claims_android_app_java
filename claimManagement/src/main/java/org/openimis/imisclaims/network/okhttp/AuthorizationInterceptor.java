package org.openimis.imisclaims.network.okhttp;

import androidx.annotation.NonNull;

import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.repository.LoginRepository;

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
        LoginRepository loginRepository = global.getLoginRepository();
        String token = loginRepository.getToken();
        if (token != null) {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Authorization", "bearer " + token.trim());
            Response response = chain.proceed(builder.build());
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                loginRepository.saveToken(null, null);
            }
            return response;
        }
        return chain.proceed(chain.request());
    }
}
