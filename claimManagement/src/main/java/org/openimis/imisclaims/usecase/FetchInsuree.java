package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetInsureeIdQuery;
import org.openimis.imisclaims.network.request.GetInsureeIdGraphQLRequest;

public class FetchInsuree {

    @NonNull
    private final GetInsureeIdGraphQLRequest request;

    public FetchInsuree() {
        this(new GetInsureeIdGraphQLRequest());
    }

    public FetchInsuree(@NonNull GetInsureeIdGraphQLRequest request) {
        this.request = request;
    }

    @NonNull
    @WorkerThread
    public String execute(@NonNull String chfId) throws Exception {
        GetInsureeIdQuery.Node node = request.get(chfId);
        byte[] bytes = node.id().getBytes();
        String id = new String(org.apache.commons.codec.binary.Base64.decodeBase64(bytes)).split(":")[1];
        return id;
    }
}