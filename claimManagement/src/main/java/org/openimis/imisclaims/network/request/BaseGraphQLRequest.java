package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.network.apollo.DateCustomTypeAdapter;
import org.openimis.imisclaims.network.apollo.DateTimeCustomTypeAdapter;
import org.openimis.imisclaims.network.apollo.DecimalCustomTypeAdapter;
import org.openimis.imisclaims.network.util.OkHttpUtils;
import org.openimis.imisclaims.type.CustomType;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class BaseGraphQLRequest {

    private static final long TIME_OUT_IN_MS = 120_000;
    private static final String URI = BuildConfig.API_BASE_URL + "api/graphql";

    private static final ApolloClient apolloClient = ApolloClient.builder()
            .okHttpClient(OkHttpUtils.getDefaultOkHttpClient())
            .serverUrl(URI)
            .addCustomTypeAdapter(CustomType.DATE, new DateCustomTypeAdapter())
            .addCustomTypeAdapter(CustomType.DATETIME, new DateTimeCustomTypeAdapter())
            .addCustomTypeAdapter(CustomType.DECIMAL, new DecimalCustomTypeAdapter())
            .build();

    @NonNull
    @WorkerThread
    protected <T extends Operation.Data> Response<T> makeSynchronous(Query<T, ?, ?> query) throws Exception {
        Semaphore semaphore = new Semaphore(0);
        final Exception[] exceptions = new Exception[1];
        final Response<T>[] responses = new Response[1];
        apolloClient.query(query).enqueue(new ApolloCall.Callback() {
            @Override
            public void onResponse(@NonNull Response response) {
                responses[0] = response;
                semaphore.release();
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                exceptions[0] = e;
                semaphore.release();
            }
        });
        if (!semaphore.tryAcquire(TIME_OUT_IN_MS, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Call couldn't finish within " + TIME_OUT_IN_MS + "ms");
        }
        Exception exception = exceptions[0];
        if (exception != null) {
            throw exception;
        }
        return responses[0];
    }
}
