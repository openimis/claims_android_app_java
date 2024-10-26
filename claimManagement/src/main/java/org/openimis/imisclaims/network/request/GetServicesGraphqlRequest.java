package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetServicesQuery;

public class GetServicesGraphqlRequest extends BaseGraphQLRequest{

    @NonNull
    @WorkerThread
    public GetServicesQuery.MedicalServices get(int page) throws Exception {
        return makeSynchronous(new GetServicesQuery(Input.fromNullable(page))).getData().medicalServices();
    }
}
