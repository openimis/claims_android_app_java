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

    public static final String EXTRA_ERROR_MESSAGE = "SynchronizeService.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_CLAIM_COUNT_PENDING = "SynchronizeService.EXTRA_CLAIM_COUNT_PENDING";
    public static final String EXTRA_CLAIM_COUNT_PENDING_XML = "SynchronizeService.EXTRA_CLAIM_COUNT_PENDING_XML";
    public static final String EXTRA_CLAIM_COUNT_ACCEPTED = "SynchronizeService.EXTRA_CLAIM_COUNT_ACCEPTED";
    public static final String EXTRA_CLAIM_COUNT_REJECTED = "SynchronizeService.EXTRA_CLAIM_COUNT_REJECTED";

    private static final String claimJsonPrefix = "ClaimJSON_";
    private static final String claimXmlPrefix = "Claim_";

    private static class ClaimUploadResult {
        public static final int CLAIM_REJECTED = 0;
        public static final int CLAIM_ACCEPTED = 1;
        public static final int CLAIM_UPLOAD_ERROR = 2;
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
        enqueueWork(context,SynchronizeService.class,JOB_ID,intent);
    }

    public static void exportClaims(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_EXPORT_CLAIMS);
        enqueueWork(context,SynchronizeService.class,JOB_ID,intent);
    }

    public static void getClaimCount(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_CLAIM_COUNT);
        enqueueWork(context,SynchronizeService.class,JOB_ID,intent);
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
        String errorMessage;

        File pendingDirectory = new File(global.getMainDirectory());
        ArrayList<File> jsonClaims = new ArrayList<>(Arrays.asList(getListOfFilesPrefix(pendingDirectory, claimJsonPrefix)));

        if (jsonClaims.size() < 1) {
            broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.NoClaim));
        } else if (!global.isNetworkAvailable()) {
            broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.CheckInternet));
        } else {
            boolean errorOccurred = false;
            for (File claimFile : jsonClaims) {
                String claim = global.getFileText(claimFile);

                JSONObject obj = null;

                try {
                    JSONObject jo = new JSONObject(claim);
                    JSONObject jobj = jo.getJSONObject("Claim");

                    JSONObject datailsObj = jobj.getJSONObject("Details");
                    JSONArray itemsArray = jobj.getJSONArray("Items");
                    JSONArray servicesArray = jobj.getJSONArray("Services");

                    JSONArray itemsArrayRes = new JSONArray();
                    for (int k = 0; k < itemsArray.length(); k++) {
                        itemsArrayRes.put(itemsArray.getJSONObject(k).getJSONObject("Item"));
                    }

                    JSONArray servicesArrayRes = new JSONArray();
                    for (int k = 0; k < servicesArray.length(); k++) {
                        servicesArrayRes.put(servicesArray.getJSONObject(k).getJSONObject("Service"));
                    }

                    obj = new JSONObject();
                    obj.put("details", datailsObj);
                    obj.put("items", itemsArrayRes);
                    obj.put("services", servicesArrayRes);

                    HttpResponse response = toRestApi.postToRestApiToken(obj, "claim");

                    if (response == null) {
                        errorMessage = getResources().getString(R.string.CheckInternet);
                    } else {
                        errorMessage = getErrorMessage(response.getStatusLine().getStatusCode());
                    }

                    if (!"".equals(errorMessage)) {
                        broadcastError(ACTION_SYNC_ERROR, errorMessage);
                        errorOccurred = true;
                        break;
                    } else {
                        String content = toRestApi.getContent(response);

                        if (content != null && !"".equals(content)) {
                            int resInt = Integer.parseInt(content);

                            switch (resInt) {
                                case ClaimUploadResult.CLAIM_ACCEPTED: {
                                    MoveFileToSubdirectory(claimFile, "AcceptedClaims");
                                    File xmlFile = new File(claimFile.getAbsolutePath().replace(claimJsonPrefix, claimXmlPrefix).replace(".txt", ".xml"));
                                    if (xmlFile.exists()) {
                                        MoveFileToSubdirectory(xmlFile, "AcceptedClaims");
                                    }
                                    break;
                                }
                                case ClaimUploadResult.CLAIM_REJECTED: {
                                    MoveFileToSubdirectory(claimFile, "RejectedClaims");
                                    File xmlFile = new File(claimFile.getAbsolutePath().replace(claimJsonPrefix, claimXmlPrefix).replace(".txt", ".xml"));
                                    if (xmlFile.exists()) {
                                        MoveFileToSubdirectory(xmlFile, "RejectedClaims");
                                    }
                                    break;
                                }
                                default: {
                                    broadcastError(ACTION_SYNC_ERROR, getResources().getString(R.string.ErrorOccurred));
                                    errorOccurred = true;
                                }
                            }
                            if (errorOccurred) break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    broadcastError(ACTION_SYNC_ERROR, e.getMessage());
                    errorOccurred = true;
                    break;
                }
            }
            if (!errorOccurred) {
                broadcastSuccess(ACTION_SYNC_SUCCESS);
            }
        }
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
                MoveFileToSubdirectory(f, "Trash");
            }

            for (File f : jsonClaims) {
                MoveFileToSubdirectory(f, "Trash");
            }

            broadcastSuccess(ACTION_EXPORT_SUCCESS);
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
        String errorMessage;
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            errorMessage = getResources().getString(R.string.has_no_rights);
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else if (responseCode >= 400) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else {
            errorMessage = "";
        }
        return errorMessage;
    }

    private File[] getListOfFilesPrefix(File directory, String prefix) {
        FilenameFilter filter = (dir, filename) -> filename.startsWith(prefix);
        return directory.listFiles(filter);
    }

    private int getFilesCountPrefix(File directory, String prefix) {
        File[] listOfFiles = getListOfFilesPrefix(directory, prefix);
        return listOfFiles != null ? listOfFiles.length : 0;
    }

    private void MoveFileToSubdirectory(File file, String subdirectory) {
        file.renameTo(new File(global.getSubdirectory(subdirectory), file.getName()));
    }

    private void broadcastSuccess(String action) {
        Intent successIntent = new Intent(action);
        sendBroadcast(successIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s",lastAction,action));
    }

    private void broadcastError(String action, String errorMessage) {
        Intent errorIntent = new Intent(action);
        errorIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
        Log.i(LOG_TAG,String.format("%s finished with %s, error message: %s",lastAction,action,errorMessage));
    }

    private void broadcastClaimCount(int pending, int accepted, int rejected, int pendingXml) {
        Intent resultIntent = new Intent(ACTION_CLAIM_COUNT_RESULT);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_PENDING, pending);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_ACCEPTED, accepted);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_REJECTED, rejected);
        resultIntent.putExtra(EXTRA_CLAIM_COUNT_PENDING_XML, pendingXml);
        sendBroadcast(resultIntent);
        Log.i(LOG_TAG,String.format("%s finished with %s, result:  p: %d,a: %d,r: %d,pxml: %d",lastAction,ACTION_CLAIM_COUNT_RESULT,pending,accepted,rejected,pendingXml));
    }
}