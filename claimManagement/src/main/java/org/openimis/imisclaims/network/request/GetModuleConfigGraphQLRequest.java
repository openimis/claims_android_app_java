package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetConfigsQuery;

import java.util.List;

public class GetModuleConfigGraphQLRequest extends  BaseGraphQLRequest{

    @NonNull
    @WorkerThread
    public List<GetConfigsQuery.ModuleConfiguration> get(String layer) throws Exception {
        return makeSynchronous(new GetConfigsQuery(Input.fromNullable(layer))).getData().moduleConfigurations();
    }
}
