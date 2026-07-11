package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetHealthFacilityQuery;

import java.util.List;

public class GetHealthfacilityGraphQLRequest extends BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public List<GetHealthFacilityQuery.Edge> get(@NonNull String hfCode) throws Exception {

        GetHealthFacilityQuery.Data data =
                makeSynchronous(new GetHealthFacilityQuery(Input.fromNullable(hfCode)))
                        .getData();

        if (data == null || data.healthFacilities() == null || data.healthFacilities().edges() == null) {
            return java.util.Collections.emptyList();
        }

        return data.healthFacilities().edges();
    }
}
