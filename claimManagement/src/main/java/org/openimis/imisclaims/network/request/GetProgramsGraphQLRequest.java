package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import org.openimis.imisclaims.GetProgramsQuery;

import java.util.List;

public class GetProgramsGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public List<GetProgramsQuery.Edge> get() throws Exception {
        return makeSynchronous(new GetProgramsQuery(Input.fromNullable(10))).getData().program().edges();
    }

}