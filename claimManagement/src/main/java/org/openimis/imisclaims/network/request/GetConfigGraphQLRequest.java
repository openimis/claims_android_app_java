package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetConfigsQuery;

import java.util.List;

public class GetConfigGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public List<GetConfigsQuery.ModuleConfiguration> get() throws Exception {
        return makeSynchronous(new GetConfigsQuery()).getData().moduleConfigurations();
    }
}