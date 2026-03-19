package org.openimis.imisclaims.network.request;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetHealthFacilitiesQuery;
public class GetHealthfacilitiesGraphQLRequest extends BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public GetHealthFacilitiesQuery.HealthFacilities get(@NonNull int page) throws Exception {
        return makeSynchronous(new GetHealthFacilitiesQuery(Input.fromNullable(page))).getData().healthFacilities();
    }
}