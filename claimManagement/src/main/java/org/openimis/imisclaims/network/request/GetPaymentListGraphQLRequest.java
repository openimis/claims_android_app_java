package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetClaimsQuery;
import org.openimis.imisclaims.GetPaymentListQuery;

import java.util.Date;

public class GetPaymentListGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetPaymentListQuery.Node get(
            @NonNull String claimAdministratorCode
    ) throws Exception {
        return makeSynchronous(new GetPaymentListQuery(
                Input.optional(claimAdministratorCode)
        )).getData().claimAdmins().edges().get(0).node();
    }
}
