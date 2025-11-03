package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetHealthFacilityQuery;

import java.util.List;

public class GetHealthfacilityGraphQLRequest extends BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public List<GetHealthFacilityQuery.Edge> get (@NonNull String HfCode) throws Exception {
        return makeSynchronous(new GetHealthFacilityQuery(Input.fromNullable(HfCode))).getData().healthFacilities().edges();
    }
}
