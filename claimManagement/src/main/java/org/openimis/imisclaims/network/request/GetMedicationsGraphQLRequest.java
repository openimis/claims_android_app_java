package org.openimis.imisclaims.network.request;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.apollographql.apollo.api.Input;
import org.openimis.imisclaims.GetMedicationsQuery;

import java.util.Date;
public class GetMedicationsGraphQLRequest extends  BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public GetMedicationsQuery.MedicalItemsStr get(int page, String pricelistUuid, Date date) throws Exception {
        return makeSynchronous(new GetMedicationsQuery(Input.fromNullable(page), Input.fromNullable(pricelistUuid), Input.fromNullable(date))).getData().medicalItemsStr();
    }
}