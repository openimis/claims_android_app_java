package org.openimis.imisclaims.network.request;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.apollographql.apollo.api.Input;
import org.openimis.imisclaims.GetMedicationsQuery;
import java.util.List;
public class GetMedicationsGraphQLRequest extends  BaseGraphQLRequest{
    @NonNull
    @WorkerThread
    public GetMedicationsQuery.MedicalItems get(int page) throws Exception {
        return makeSynchronous(new GetMedicationsQuery(Input.fromNullable(page))).getData().medicalItems();
    }
}