package org.openimis.imisclaims;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.network.exception.HttpException;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.tools.StorageManager;
import org.openimis.imisclaims.util.StreamUtils;
import org.openimis.imisclaims.util.UriUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SynchronizeActivity extends ImisActivity {
    private static final String LOG_TAG = "SYNCACTIVITY";
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_EXPORT_XML_FILE = 2;
    ArrayList<String> broadcastList;

    TextView tvUploadClaims, tvZipClaims;
    RelativeLayout uploadClaims, zipClaims, importMasterData, downloadMasterData, checkUpdate;

    ProgressDialog pd;
    Uri exportUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        broadcastList = new ArrayList<>();
        broadcastList.add(SynchronizeService.ACTION_CLAIM_COUNT_RESULT);
        broadcastList.add(SynchronizeService.ACTION_SYNC_SUCCESS);
        broadcastList.add(SynchronizeService.ACTION_SYNC_ERROR);
        broadcastList.add(SynchronizeService.ACTION_EXPORT_SUCCESS);
        broadcastList.add(SynchronizeService.ACTION_EXPORT_ERROR);
        broadcastList.add(MasterDataService.ACTION_IMPORT_ERROR);
        broadcastList.add(MasterDataService.ACTION_IMPORT_SUCCESS);
        broadcastList.add(MasterDataService.ACTION_DOWNLOAD_ERROR);
        broadcastList.add(MasterDataService.ACTION_DOWNLOAD_SUCCESS);


        tvUploadClaims = findViewById(R.id.tvUploadClaims);
        tvZipClaims = findViewById(R.id.tvZipClaims);

        uploadClaims = findViewById(R.id.upload_claims);
        zipClaims = findViewById(R.id.zip_claims);
        importMasterData = findViewById(R.id.importMasterData);
        downloadMasterData = findViewById(R.id.downloadMasterData);
        checkUpdate = findViewById(R.id.checkUpdate);

        uploadClaims.setOnClickListener(view -> doLoggedIn(this::confirmUploadClaims));
        zipClaims.setOnClickListener(view -> confirmXMLCreation());

        importMasterData.setOnClickListener(view -> requestPickDatabase());
        downloadMasterData.setOnClickListener(view -> {
        }); //TODO Not yet implemented
        downloadMasterData.setVisibility(View.GONE);
        checkUpdate.setOnClickListener(view -> CheckUpdate());

    }

    @Override
    public void onResume() {
        super.onResume();
        SynchronizeService.getClaimCount(this);
    }

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        String action = intent.getAction();
        String errorMessage;

        switch (action) {
            case SynchronizeService.ACTION_CLAIM_COUNT_RESULT:
                tvUploadClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ENTERED, 0)));
                tvZipClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ENTERED, 0)));
                break;
            case SynchronizeService.ACTION_EXPORT_SUCCESS:
                exportUri = Uri.parse(intent.getStringExtra(SynchronizeService.EXTRA_EXPORT_URI));
                showDialog(getResources().getString(R.string.XmlExportCreated),
                        (dialog, which) -> StorageManager.of(this).requestCreateFile(
                                REQUEST_EXPORT_XML_FILE,
                                "application/octet-stream",
                                UriUtils.getDisplayName(this, exportUri)));
                break;
            case SynchronizeService.ACTION_SYNC_SUCCESS:
                try {
                    JSONArray result = new JSONArray(intent.getStringExtra(SynchronizeService.EXTRA_CLAIM_RESPONSE));
                    int resultLength = result.length();
                    if (resultLength > 0) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < resultLength; i++) {
                            String message = result.getString(i);
                            builder.append(message).append("\n");
                            Log.i(LOG_TAG, message);
                        }
                        showDialog(builder.toString());
                    } else {
                        showDialog(getResources().getString(R.string.BulkUpload));
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error while processing claim response", e);
                }
                break;
            case SynchronizeService.ACTION_EXPORT_ERROR:
            case SynchronizeService.ACTION_SYNC_ERROR:
                errorMessage = intent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE);
                showDialog(errorMessage);
                break;
            case MasterDataService.ACTION_IMPORT_SUCCESS:
                showDialog(getResources().getString(R.string.importMasterDataSuccess));
                break;
            case MasterDataService.ACTION_IMPORT_ERROR:
                errorMessage = intent.getStringExtra(MasterDataService.EXTRA_ERROR_MESSAGE);
                showDialog(errorMessage);
                break;
        }

        if (pd != null && pd.isShowing()) pd.dismiss();

        if (!SynchronizeService.ACTION_CLAIM_COUNT_RESULT.equals(action)) {
            SynchronizeService.getClaimCount(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedFile = data.getData();
            pd = ProgressDialog.show(this, "", getResources().getString(R.string.Processing));
            MasterDataService.importMasterData(this, selectedFile);
        } else if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            showToast(R.string.importMasterDataCanceled);
        } else if (requestCode == REQUEST_EXPORT_XML_FILE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri outputFileUri = data.getData();
                try (InputStream is = getContentResolver().openInputStream(exportUri);
                     OutputStream os = getContentResolver().openOutputStream(outputFileUri)) {
                    StreamUtils.bufferedStreamCopy(is, os);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Copying XML export failed", e);
                }
            } else {
                showDialog(getResources().getString(R.string.XmlExportRetry),
                        (dialog, which) -> StorageManager.of(this).requestCreateFile(
                                REQUEST_EXPORT_XML_FILE,
                                "application/octet-stream",
                                UriUtils.getDisplayName(this, exportUri)));
            }
        }
    }

    @Override
    protected ArrayList<String> getBroadcastList() {
        return broadcastList;
    }

    public void confirmXMLCreation() {
        showDialog(getResources().getString(R.string.AreYouSure), (dialogInterface, i) -> exportClaims(), (dialog, id) -> dialog.cancel());
    }

    public void confirmUploadClaims() {
        showDialog(getResources().getString(R.string.AreYouSure), (dialogInterface, i) -> uploadClaims(), (dialog, id) -> dialog.cancel());
    }

    public void requestPickDatabase() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void uploadClaims() {
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Processing));
        SynchronizeService.uploadClaims(this);
    }

    public void exportClaims() {
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Processing));
        SynchronizeService.exportClaims(this);
    }

    public void CheckUpdate(){
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.Checking_For_Updates);
            pd = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);

            Thread thread = new Thread(() -> {
                try {
                    //get current release
                    String currentVersion = BuildConfig.VERSION_NAME; //
                    boolean updateAvailable = false;

                    //get all github releases
                    URL url = new URL("https://api.github.com/repos/openimis/claims_android_app_java/releases");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    connection.setReadTimeout(60_000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();

                    //get latest version
                    JSONArray jsonarray = new JSONArray(response.toString());
                    String lastVersion = "";
                    String tag_name = "";
                    for (int i = 0; i < jsonarray.length(); i++){
                        JSONObject releaseObj = jsonarray.getJSONObject(i);
                        if(releaseObj.getString("tag_name").equals(getResources().getString(R.string.release_tag))){
                            tag_name = releaseObj.getString("tag_name");
                            String releaseName = releaseObj.getString("name");
                            if(!releaseName.equals(currentVersion)){
                                lastVersion = releaseName;
                                updateAvailable = true;
                            }
                        }
                    }

                    //print result
                    boolean finalUpdateAvailable = updateAvailable;
                    String finalLastVersion = lastVersion;
                    String finalTagName = tag_name;
                    runOnUiThread(() -> {
                        pd.dismiss();
                        if (finalUpdateAvailable) {
                            new AlertDialog.Builder(this)
                                    .setTitle(getResources().getString(R.string.updateAvailable))
                                    .setMessage(getResources().getString(R.string.newVersion) + " " + finalLastVersion )
                                    .setPositiveButton(getResources().getString(R.string.download), (dialog, which) -> downloadUpdate(finalLastVersion, finalTagName))
                                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                                    .show();
                        } else {
                            Toast.makeText(this,
                                    getResources().getString(R.string.haveLastVersion) + " " + currentVersion,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (HttpException e){
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(this,
                                getResources().getString(R.string.Error),
                                Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
            thread.start();
        } else {
           showDialog(getResources().getString(R.string.CheckInternet));
        }
    }

    public void downloadUpdate(String lastVersion, String tagName) {
        try {
            String apkUrl = "https://github.com/openimis/claims_android_app_java/releases/download/" + tagName + "/claimManagement-"+ BuildConfig.FLAVOR + "-debug.apk";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl))
                    .setTitle(getResources().getString(R.string.claimUpdate))
                    .setDescription(getResources().getString(R.string.getVersion) + lastVersion)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "claimManagement-"+ BuildConfig.FLAVOR + "-debug.apk")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            Toast.makeText(this, getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.downloadUpdateFail), Toast.LENGTH_SHORT).show();
            Log.e("DownloadUpdate", "Erreur: ", e);
        }
    }
}
