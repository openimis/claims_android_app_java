package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetServicesQuery;

import java.util.Date;

public class GetServicesGraphQLRequest extends BaseGraphQLRequest{

    @NonNull
    @WorkerThread
    public GetServicesQuery.MedicalServicesStr get(int page, String pricelistUUID, Date date) throws Exception {
        return makeSynchronous(new GetServicesQuery(Input.fromNullable(page), Input.fromNullable(pricelistUUID), Input.fromNullable(date))).getData().medicalServicesStr();
    }
}