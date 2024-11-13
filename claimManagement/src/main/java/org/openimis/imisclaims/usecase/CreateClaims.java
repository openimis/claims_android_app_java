package org.openimis.imisclaims.usecase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imisclaims.SQLHandler;
import org.openimis.imisclaims.SynchronizeService;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.domain.entity.Insuree;
import org.openimis.imisclaims.domain.entity.PendingClaimGQL;
import org.openimis.imisclaims.network.exception.HttpException;
import org.openimis.imisclaims.network.request.CreateClaimGraphQLRequest;
import org.openimis.imisclaims.tools.Log;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class CreateClaims {

    @NonNull
    private final CreateClaimGraphQLRequest createClaimGraphQLRequest;

    public CreateClaims() {
        this(new CreateClaimGraphQLRequest());
    }

    public CreateClaims(
            @NonNull CreateClaimGraphQLRequest createPolicyGraphQLRequest
    ) {
        this.createClaimGraphQLRequest = createPolicyGraphQLRequest;
    }

    @WorkerThread
    public List<PostNewClaims.Result> execute(
            List<PendingClaimGQL> claims,
            Context context
    ) throws Exception {
        List<PostNewClaims.Result> results = new ArrayList<>();
        SQLHandler sqlHandler = new SQLHandler(context);
        for(PendingClaimGQL pendingClaim: claims){
            int insureeId = Integer.parseInt(new FetchInsuree().execute(pendingClaim.getChfId()));
            int adminId = Integer.parseInt(new FetchClaimAdmin().execute(pendingClaim.getClaimAdmin()));
            int icdId = Integer.parseInt(new FetchDiagnose().execute(pendingClaim.getIcdCode()));
            String hfId = sqlHandler.getHfId(pendingClaim.getHealthFacilityCode());
            String referFromId = sqlHandler.getHfId(pendingClaim.getReferalHF());
            results.add(createClaimGraphQLRequest.create(pendingClaim,Integer.parseInt(hfId),adminId,insureeId,icdId, Integer.parseInt(referFromId)));
        }
        return results;
    }
}