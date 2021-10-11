package org.openimis.imisclaims;

import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;

public class SynchronizeService extends JobIntentService {
    private static final int JOB_ID = 6541259; //Random unique Job id
    private static final String LOG_TAG = "SYNCSERVICE";

    private static final String ACTION_UPLOAD_CLAIMS = "SynchronizeService.ACTION_UPLOAD_CLAIMS";
    private static final String ACTION_EXPORT_CLAIMS = "SynchronizeService.ACTION_EXPORT_CLAIMS";
    private static final String ACTION_CLAIM_COUNT = "SynchronizeService.ACTION_CLAIM_COUNT";

    public static final String ACTION_SYNC_SUCCESS = "SynchronizeService.ACTION_SYNC_SUCCESS";
    public static final String ACTION_SYNC_ERROR = "SynchronizeService.ACTION_SYNC_ERROR";
    public static final String ACTION_EXPORT_SUCCESS = "SynchronizeService.ACTION_EXPORT_SUCCESS";
    public static final String ACTION_EXPORT_ERROR = "SynchronizeService.ACTION_EXPORT_ERROR";
    public static final String ACTION_CLAIM_COUNT_RESULT = "SynchronizeService.ACTION_CLAIM_COUNT_RESULT";

    public static final String EXTRA_CLAIM_RESPONSE = "SynchronizeService.EXTRA_CLAIM_RESPONSE";
    public static final String EXTRA_ERROR_MESSAGE = "SynchronizeService.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_CLAIM_COUNT_PENDING = "SynchronizeService.EXTRA_CLAIM_COUNT_PENDING";
    public static final String EXTRA_CLAIM_COUNT_PENDING_XML = "SynchronizeService.EXTRA_CLAIM_COUNT_PENDING_XML";
    public static final String EXTRA_CLAIM_COUNT_ACCEPTED = "SynchronizeService.EXTRA_CLAIM_COUNT_ACCEPTED";
    public static final String EXTRA_CLAIM_COUNT_REJECTED = "SynchronizeService.EXTRA_CLAIM_COUNT_REJECTED";

    private static final String claimJsonPrefix = "ClaimJSON_";
    private static final String claimXmlPrefix = "Claim_";

    private static final String claimResponseLine = "[%s] %s";

    public static class ClaimResponse {
        public static final int Success = 2001;
        public static final int InvalidHFCode = 2002;
        public static final int DuplicateClaimCode = 2003;
        public static final int InvalidInsuranceNumber = 2004;
        public static final int EndDateIsBeforeStartDate = 2005;
        public static final int InvalidICDCode = 2006;
        public static final int InvalidItem = 2007;
        public static final int InvalidService = 2008;
        public static final int InvalidClaimAdmin = 2009;
        public static final int Rejected = 2010;
        public static final int UnexpectedException = 2999;
    }

    Global global;
    String lastAction;
    ToRestApi toRestApi;

    @Override
    public void onCreate() {
        super.onCreate();
        global = (Global) getApplicationContext();
        toRestApi = new ToRestApi();

    }

    public static void uploadClaims(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPLOAD_CLAIMS);
        enqueueWork(context, SynchronizeService.class, JOB_ID, intent);
    }

    public static void exportClaims(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_EXPORT_CLAIMS);
        enqueueWork(context, SynchronizeService.class, JOB_ID, intent);
    }

    public static void getClaimCount(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_CLAIM_COUNT);
        enqueueWork(context, SynchronizeService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        lastAction = intent.getAction();
        if (ACTION_UPLOAD_CLAIMS.equals(lastAction)) {
            handleUploadClaims();
        } else if (ACTION_EXPORT_CLAIMS.equals(lastAction)) {
            handleExportClaims();
        } else if (ACTION_CLAIM_COUNT.equals(lastAction)) {
            handleGetClaimCount();
        }
    }

    private void handleUploadClaims() {
        if (!global.isNetworkAvailable()) {
            broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.CheckInternet));
            return;
        }

        File pendingDirectory = new File(global.getMainDirectory());
        ArrayList<File> jsonClaims = new ArrayList<>(Arrays.asList(getListOfFilesPrefix(pendingDirectory, claimJsonPrefix)));
        JSONArray claims = loadClaims(jsonClaims);

        if (claims.length() < 1) {
            broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.NoClaim));
            return;
        }

        HttpResponse response = toRestApi.postToRestApiToken(claims, "claim");
        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            String errorMessage = getErrorMessage(statusCode);

            if (errorMessage != null) {
                broadcastError(ACTION_SYNC_ERROR, errorMessage);
                return;
            }

            try {
                String responseContent = toRestApi.getContent(response);
                JSONArray claimResponseArray = new JSONArray(responseContent);
                JSONArray claimStatus = processClaimResponse(claimResponseArray);
                broadcastSyncSuccess(claimStatus);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error while processing claim response", e);
                broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.ErrorOccurred));
            }
        }
    }

    private JSONArray processClaimResponse(JSONArray claimResponseArray) throws JSONException {
        JSONArray result = new JSONArray();

        for (int i = 0; i < claimResponseArray.length(); i++) {
            JSONObject claimResponse = claimResponseArray.getJSONObject(i);
            String claimCode = claimResponse.getString("claimCode");
            int claimResponseCode = claimResponse.getInt("response");

            if (claimResponseCode == ClaimResponse.Success) {
                moveClaimToSubdirectory(claimCode, "AcceptedClaims");
            } else if (claimResponseCode == ClaimResponse.Rejected) {
                moveClaimToSubdirectory(claimCode, "RejectedClaims");
            } else {
                result.put(String.format(claimResponseLine, claimCode, claimResponse.getString("message")));
            }
        }
        return result;
    }

    private void handleExportClaims() {
        File pendingDirectory = new File(global.getMainDirectory());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        String dateForZip = format.format(calendar.getTime());

        String zipFilePath = global.getMainDirectory() + "/Claims" + "_" + global.getOfficerCode() + "_" + dateForZip + ".zip";
        String password = global.getRarPwd();

        ArrayList<File> xmlClaims = new ArrayList<>(Arrays.asList(getListOfFilesPrefix(pendingDirectory, claimXmlPrefix)));
        ArrayList<File> jsonClaims = new ArrayList<>(Arrays.asList(getListOfFilesPrefix(pendingDirectory, claimJsonPrefix)));

        if (xmlClaims.size() > 0) {
            Compressor.zip(xmlClaims, zipFilePath, password);

            for (File f : xmlClaims) {
                moveFileToSubdirectory(f, "Trash");
            }

            for (File f : jsonClaims) {
                moveFileToSubdirectory(f, "Trash");
            }

            broadcastExportSuccess();
        } else {
            broadcastError(ACTION_EXPORT_ERROR, getResources().getString(R.string.NoClaim));
        }
    }

    private void handleGetClaimCount() {
        File pendingDirectory = new File(global.getMainDirectory());
        File acceptedDirectory = new File(global.getSubdirectory("AcceptedClaims"));
        File rejectedDirectory = new File(global.getSubdirectory("RejectedClaims"));

        int pendingCount = getFilesCountPrefix(pendingDirectory, claimJsonPrefix);
        int pendingXMLCount = getFilesCountPrefix(pendingDirectory, claimXmlPrefix);
        int acceptedCount = getFilesCountPrefix(acceptedDirectory, claimJsonPrefix);
        int rejectedCount = getFilesCountPrefix(rejectedDirectory, claimJsonPrefix);

        broadcastClaimCount(pendingCount, acceptedCount, rejectedCount, pendingXMLCount);
    }

    private String getErrorMessage(int responseCode) {
        String errorMessage = null;
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            errorMessage = getResources().getString(R.string.has_no_rights);
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else if (responseCode >= 400) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        }
        return errorMessage;
    }

    private void moveClaimToSubdirectory(String claimCode, String subDirectory) {
        File pendingDirectory = new File(global.getMainDirectory());
        File[] files = getListOfFilesForClaim(pendingDirectory, claimCode);
        for (File f : files) {
            moveFileToSubdirectory(f, subDirectory);
        }
    }

    private File[] getListOfFilesPrefix(File directory, String prefix) {
        FilenameFilter filter = (dir, filename) -> filename.startsWith(prefix);
        return directory.listFiles(filter);
    }

    private File[] getListOfFilesForClaim(File directory, String claimCode) {
        String regex = ".+_.+_" + claimCode + "_";
        return getListOfFilesMatching(directory, regex);
    }

    private File[] getListOfFilesMatching(File directory, String regex) {
        FilenameFilter filter = (dir, filename) -> filename.matches(regex);
        return directory.listFiles(filter);
    }

    private int getFilesCountPrefix(File directory, String prefix) {
        File[] listOfFiles = getListOfFilesPrefix(directory, prefix);
        return listOfFiles != null ? listOfFiles.length : 0;
    }

    private void moveFileToSubdirectory(File file, String subdirectory) {
        if (!file.renameTo(new File(global.getSubdirectory(subdirectory), file.getName()))) {
            Log.e(LOG_TAG, String.format("Moving a file to %s failed: %s", subdirectory, file.getAbsolutePath()));
        }
    }

    private JSONArray loadClaims(List<File> files) {
        try {
            JSONArray claims = new JSONArray();
            for (File claimFile : files) {
                String claim = global.getFileText(claimFile);
                JSONObject claimObject = new JSONObject(claim);
                claims.put(claimObject);
            }
            return claims;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while loading claims", e);
            return new JSONArray();
        }
    }

    private void broadcastSyncSuccess(JSONArray claimResponse) {
        Intent successIntent = new Intent(ACTION_SYNC_SUCCESS);
        successIntent.putExtra(EXTRA_CLAIM_RESPONSE, claimResponse.toString());
        sendBroadcast(successIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s, messages count: %d", lastAction, ACTION_SYNC_SUCCESS, claimResponse.length()));
    }


    private void broadcastExportSuccess() {
        Intent successIntent = new Intent(ACTION_EXPORT_SUCCESS);
        sendBroadcast(successIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s", lastAction, ACTION_EXPORT_SUCCESS));
    }

    private void broadcastError(String action, String errorMessage) {
        Intent errorIntent = new Intent(action);
        errorIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s, error message: %s", lastAction, action, errorMessage));
    }

    private void broadcastClaimCount(int pending, int accepted, int rejected, int pendingXml) {
        Intent resultIntent = new Intent(ACTION_CLAIM_COUNT_RESULT);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_PENDING, pending);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_ACCEPTED, accepted);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_REJECTED, rejected);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_PENDING_XML, pendingXml);
        sendBroadcast(resultIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s, result:  p: %d,a: %d,r: %d,pxml: %d", lastAction, ACTION_CLAIM_COUNT_RESULT, pending, accepted, rejected, pendingXml));
    }
}