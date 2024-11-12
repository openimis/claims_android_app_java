package org.openimis.imisclaims.usecase;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import org.openimis.imisclaims.GetDiagnoseIdQuery;
import org.openimis.imisclaims.network.request.GetDiagnosesGraphQLRequest;

public class FetchDiagnose {

    @NonNull
    private final GetDiagnosesGraphQLRequest request;

    public FetchDiagnose() {
        this(new GetDiagnosesGraphQLRequest());
    }

    public FetchDiagnose(@NonNull GetDiagnosesGraphQLRequest request) {
        this.request = request;
    }

    @NonNull
    @WorkerThread
    public String execute(@NonNull String code) throws Exception {
        GetDiagnoseIdQuery.Node node = request.get(code);
        byte[] bytes = node.id().getBytes();
        String id = new String(org.apache.commons.codec.binary.Base64.decodeBase64(bytes)).split(":")[1];
        return id;
    }
}
