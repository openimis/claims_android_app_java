package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetControlsQuery;

import java.util.List;

public class GetControlsGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public List<GetControlsQuery.Edge> get() throws Exception {
        return makeSynchronous(new GetControlsQuery()).getData().control().edges();
    }
}
