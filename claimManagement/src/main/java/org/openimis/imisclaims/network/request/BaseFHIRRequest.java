package org.openimis.imisclaims.network.request;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.network.util.OkHttpUtils;

import java.util.Map;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public abstract class BaseFHIRRequest {

    protected static final String BASE_URL = BuildConfig.API_BASE_URL + "api/api_fhir_r4/";

    @NonNull
    private final String endpoint;
    @NonNull
    protected final OkHttpClient okHttpClient = OkHttpUtils.getDefaultOkHttpClient();

    protected BaseFHIRRequest(@NonNull String endpoint) {
        this.endpoint = endpoint;
    }

    @NonNull
    protected Request.Builder getRequestBuilder(@Nullable Map<String, String> queryParameters) {
        Request.Builder builder = new Request.Builder();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + endpoint + "/")).newBuilder();
        if (queryParameters != null) {
            for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.url(urlBuilder.build())
                .addHeader("Content-Type", "application/json");
    }
}
