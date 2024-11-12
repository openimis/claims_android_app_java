package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imisclaims.GetInsureeIdQuery;
import org.openimis.imisclaims.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;


public class GetInsureeIdGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetInsureeIdQuery.Node get(
            @NonNull String chfId
    ) throws Exception {
        List<GetInsureeIdQuery.Edge> edges = makeSynchronous(new GetInsureeIdQuery(
                Input.fromNullable(chfId)
        )).getData().insurees().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "Insuree with id '" + chfId + "' was not found",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return edges.get(0).node();
    }
}