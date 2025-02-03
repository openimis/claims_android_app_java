package org.openimis.imisclaims.network.request;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.apollographql.apollo.api.Input;
import org.openimis.imisclaims.GetHealthFacilityQuery;
public class GetHealthfacilitiesGraphQLRequest extends BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public GetHealthFacilityQuery.HealthFacilities get(@NonNull int page) throws Exception {
        return makeSynchronous(new GetHealthFacilityQuery(Input.fromNullable(page))).getData().healthFacilities();
    }
}