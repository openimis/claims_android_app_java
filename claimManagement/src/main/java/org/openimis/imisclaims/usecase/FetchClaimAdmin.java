package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetAdminIdQuery;
import org.openimis.imisclaims.network.request.GetAdminIdGraphQLRequest;

public class FetchClaimAdmin {

    @NonNull
    private final GetAdminIdGraphQLRequest request;

    public FetchClaimAdmin() {
        this(new GetAdminIdGraphQLRequest());
    }

    public FetchClaimAdmin(@NonNull GetAdminIdGraphQLRequest request) {
        this.request = request;
    }

    @NonNull
    @WorkerThread
    public String execute(@NonNull String chfId) throws Exception {
        GetAdminIdQuery.Node node = request.get(chfId);
        byte[] bytes = node.id().getBytes();
        String id = new String(org.apache.commons.codec.binary.Base64.decodeBase64(bytes)).split(":")[1];
        return id;
    }
}
