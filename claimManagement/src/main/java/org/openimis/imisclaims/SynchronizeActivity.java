package org.openimis.imisclaims;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.tools.StorageManager;
import org.openimis.imisclaims.util.StreamUtils;
import org.openimis.imisclaims.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SynchronizeActivity extends ImisActivity {
    private static final String LOG_TAG = "SYNCACTIVITY";
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_EXPORT_XML_FILE = 2;
    ArrayList<String> broadcastList;

    TextView tvUploadClaims, tvZipClaims;
    RelativeLayout uploadClaims, zipClaims, importMasterData, downloadMasterData;

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

        uploadClaims.setOnClickListener(view -> doLoggedIn(this::confirmUploadClaims));
        zipClaims.setOnClickListener(view -> confirmXMLCreation());

        importMasterData.setOnClickListener(view -> requestPickDatabase());
        downloadMasterData.setOnClickListener(view -> {
        }); //TODO Not yet implemented
        downloadMasterData.setVisibility(View.GONE);

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
                tvUploadClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_PENDING, 0)));
                tvZipClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_PENDING, 0)));
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
}
