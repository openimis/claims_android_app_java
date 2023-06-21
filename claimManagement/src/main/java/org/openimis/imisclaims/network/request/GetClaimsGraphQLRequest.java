package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetClaimsQuery;

import java.util.Date;

public class GetClaimsGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetClaimsQuery.Claims get(
            @Nullable String claimAdministratorCode,
            @Nullable Integer status,
            @Nullable Date visitDateFrom,
            @Nullable Date visitDateTo,
            @Nullable Date processedDateFrom,
            @Nullable Date processedDateTo
    ) throws Exception {
        return makeSynchronous(new GetClaimsQuery(
                Input.fromNullable(claimAdministratorCode),
                Input.fromNullable(status),
                Input.fromNullable(visitDateFrom),
                Input.fromNullable(visitDateTo),
                Input.fromNullable(processedDateFrom),
                Input.fromNullable(processedDateTo)
        )).getData().claims();
    }
}
