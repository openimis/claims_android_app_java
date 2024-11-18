package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetAdminIdQuery;
import org.openimis.imisclaims.GetDiagnoseIdQuery;
import org.openimis.imisclaims.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;

public class GetDiagnosesGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetDiagnoseIdQuery.Node get(
            @NonNull String code
    ) throws Exception {
        List<GetDiagnoseIdQuery.Edge> edges = makeSynchronous(new GetDiagnoseIdQuery(
                Input.fromNullable(code)
        )).getData().diagnoses().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "Insuree with id '" + code + "' was not found",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return edges.get(0).node();
    }
}
