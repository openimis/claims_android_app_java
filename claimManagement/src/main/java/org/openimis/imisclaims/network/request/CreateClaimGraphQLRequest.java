package org.openimis.imisclaims.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.apollographql.apollo.api.internal.QueryDocumentMinifier;
import org.json.JSONObject;
import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.Token;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.domain.entity.PendingClaimGQL;
import org.openimis.imisclaims.domain.entity.SubServiceItem;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.usecase.PostNewClaims;
import org.openimis.imisclaims.util.DateUtils;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateClaimGraphQLRequest extends BaseGraphQLRequest{

    private static final String URI = BuildConfig.API_BASE_URL + "api/graphql";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    protected Global global;
    private final Token token = Global.getGlobal().getJWTToken();

    @WorkerThread
    @NonNull
    public PostNewClaims.Result create(
            @NonNull PendingClaimGQL claim,
            @NonNull int hfId,
            @NonNull int adminId,
            @NonNull int insureeId,
            @NonNull int diagnosisId,
            @NonNull int referFromId,
            @NonNull int icd1Id,
            @NonNull int icd2Id,
            @NonNull int icd3Id,
            @NonNull int icd4Id
    ) throws Exception{

        String claimServices = "";
        if(claim.getServices().isEmpty()){
            claimServices = "[]";
        }else{
            claimServices = "[";
            for(PendingClaimGQL.Service service: claim.getServices()){
                String subServices = "";
                String subItems = "";

                if(service.getSubServices() != null && service.getSubServices().isEmpty()){
                    subServices = "[]";
                }else{
                    subServices = "[";
                    for (PendingClaimGQL.SubServiceItem subService: service.getSubServices()){
                        String subObj = "{"
                                + " subServiceCode: \"" + subService.getCode() + "\""
                                + " qtyAsked: \"" + subService.getQty() + "\""
                                + " priceAsked: \"" + subService.getPrice() + "\""
                                + " qtyProvided: \"" + subService.getQty() + "\""
                                + "}";
                        subServices = subServices + subObj;
                    }
                    subServices = subServices + "]";
                }

                if(service.getSubItems() != null && service.getSubItems().isEmpty()){
                    subItems = "[]";
                }else{
                    subItems = "[";
                    for(PendingClaimGQL.SubServiceItem subItem: service.getSubItems()){
                        String subObj = "{"
                                + " subItemCode: \"" + subItem.getCode() + "\""
                                + " qtyAsked: \"" + subItem.getQty() + "\""
                                + " priceAsked: \"" + subItem.getPrice() + "\""
                                + " qtyProvided: \"" + subItem.getQty() + "\""
                                + "}";
                        subItems = subItems + subObj;
                    }
                    subItems = subItems + "]";
                }

                String obj = "{"
                        + " serviceId: " + Integer.valueOf(service.getId())
                        + " priceAsked: \"" + service.getPrice() + "\""
                        + " qtyProvided: \"" + service.getQuantity() + "\""
                        + " serviceServiceSet: " + subServices
                        + " serviceItemSet: " + subItems
                        + " status: 1"
                        + "}";
                claimServices = claimServices + obj;
            }
            claimServices = claimServices + "]";
        }

        String claimItems = "";
        if(claim.getMedications().isEmpty()){
            claimItems = "[]";
        }else{
            claimItems = "[";
            for(PendingClaimGQL.Medication item: claim.getMedications()){
                String obj = "{"
                        + " itemId: " + Integer.valueOf(item.getId())
                        + " priceAsked: \"" + item.getPrice() + "\""
                        + " qtyProvided: \"" + item.getQuantity() + "\""
                        + " status: 1"
                        + "}";
                claimItems = claimItems + obj;
            }
            claimItems = claimItems + "]";
        }

        boolean preAuthorization = false;
        if(claim.getPreAuthorization() != null && claim.getPreAuthorization().equals("1")){
            preAuthorization = true;
        }

        String referFrom = "";
        if(referFromId != 0){
            referFrom = " referFromId: " + referFromId;
        }

        String icd1 = "";
        if(icd1Id != 0){
            icd1 = "icd1Id: "+ icd1Id;
        }

        String icd2 = "";
        if(icd2Id != 0){
            icd2 = "icd2Id: "+ icd2Id;
        }

        String icd3 = "";
        if(icd3Id != 0){
            icd3 = "icd3Id: "+ icd3Id;
        }

        String icd4 = "";
        if(icd4Id != 0){
            icd4 = "icd4Id: "+ icd4Id;
        }

        String QUERY_DOCUMENT = QueryDocumentMinifier.minify(
                "mutation {"
                        + "  createClaim(input: {"
                        + " code: \"" + claim.getClaimCode() + "\""
                        + " insureeId: " + insureeId
                        + " adminId: " + adminId
                        + " dateFrom: \"" + DateUtils.toDateString(claim.getStartDate()) + "\""
                        + " dateTo: \"" + DateUtils.toDateString(claim.getEndDate()) + "\""
                        + " icdId: " + diagnosisId
                        + icd1
                        + icd2
                        + icd3
                        + icd4
                        + " guaranteeId: \"" + claim.getGuaranteeNumber() + "\""
                        + " dateClaimed: \"" + DateUtils.toDateString(claim.getClaimDate()) + "\""
                        + referFrom
                        + " referralCode: \"" + claim.getReferralCode() + "\""
                        + " patientCondition: \"" + claim.getPatientCondition() + "\""
                        + " preAuthorization: " + preAuthorization
                        + " healthFacilityId: " + hfId
                        + " visitType: \"" + claim.getVisitType() +"\""
                        + " services: " + claimServices
                        + " items: " + claimItems
                        + "}){"
                        + "    clientMutationId"
                        + "    internalId"
                        + "  }"
                        + "}"
        );

        JSONObject json = new JSONObject();
        json.put("query", QUERY_DOCUMENT);


        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        OkHttpClient httpClient = builder.build();

        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(URI)
                .addHeader("Authorization","bearer " + token.getTokenText().trim())
                .post(body)
                .build();


        Response response = httpClient.newCall(request).execute();
        int responseCode = response.code();

        Log.i("HTTP_POST", URI + " - " + responseCode);
        Log.i("Claim", QUERY_DOCUMENT);

        String responsePhrase = response.body().string();
        Log.i("RESPONSE", String.format("response: %d %s", responseCode, responsePhrase));



        return (new PostNewClaims.Result(
                claim.getClaimCode(),
                response.code() == 200 ? PostNewClaims.Result.Status.SUCCESS: PostNewClaims.Result.Status.REJECTED,
                response.message()
        ));
    }
}