package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetHealthFacilityQuery;
import org.openimis.imisclaims.network.request.GetHealthfacilityGraphQLRequest;

import java.util.List;

public class CheckHealthFacility {
    @NonNull
    private final GetHealthfacilityGraphQLRequest request;

    public CheckHealthFacility(){this(new GetHealthfacilityGraphQLRequest());}

    public CheckHealthFacility(@NonNull GetHealthfacilityGraphQLRequest request){
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public boolean execute(String HfCode) throws Exception {
        List<GetHealthFacilityQuery.Edge> response = request.get(HfCode);
        return !response.isEmpty();
    }
}
