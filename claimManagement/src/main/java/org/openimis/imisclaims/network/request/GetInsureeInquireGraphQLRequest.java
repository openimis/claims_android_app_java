package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;


import org.openimis.imisclaims.GetInsureeInquireQuery;


public class GetInsureeInquireGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetInsureeInquireQuery.Node get(
            @NonNull String chfId
    ) throws Exception {
        return makeSynchronous(new GetInsureeInquireQuery(
                Input.fromNullable(chfId)
        )).getData().insurees().edges().get(0).node();
    }
}
