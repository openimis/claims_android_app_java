package org.openimis.imisclaims;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SynchronizeActivity extends ImisActivity {
    ArrayList<String> broadcastList;

    TextView tvUploadClaims, tvZipClaims;
    RelativeLayout UploadClaims, zip_claims;

    ProgressDialog pd;

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


        tvUploadClaims = findViewById(R.id.tvUploadClaims);
        tvZipClaims = findViewById(R.id.tvZipClaims);

        UploadClaims = findViewById(R.id.upload_claims);
        zip_claims = findViewById(R.id.zip_claims);

        UploadClaims.setOnClickListener(view -> doLoggedIn(this::ConfirmUploadClaims));
        zip_claims.setOnClickListener(view -> ConfirmXMLCreation());
    }

    @Override
    public void onResume() {
        super.onResume();
        SynchronizeService.getClaimCount(this);
    }

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        String action = intent.getAction();
        if (SynchronizeService.ACTION_CLAIM_COUNT_RESULT.equals(action)) {
            tvUploadClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_PENDING, 0)));
            tvZipClaims.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_PENDING_XML, 0)));
        } else {
            if (SynchronizeService.ACTION_EXPORT_SUCCESS.equals(action)) {
                showDialog(getResources().getString(R.string.ZipXMLCreated));
            } else if (SynchronizeService.ACTION_SYNC_SUCCESS.equals(action)) {
                pd.dismiss();
                showDialog(getResources().getString(R.string.BulkUpload));
            } else if (SynchronizeService.ACTION_EXPORT_ERROR.equals(action)) {
                String errorMessage = intent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE);
                showDialog(errorMessage);
            } else if (SynchronizeService.ACTION_SYNC_ERROR.equals(action)) {
                pd.dismiss();
                String errorMessage = intent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE);
                showDialog(errorMessage);
            }
            SynchronizeService.getClaimCount(this);
        }
    }

    @Override
    protected ArrayList<String> getBroadcastList() {
        return broadcastList;
    }

    public void ConfirmXMLCreation() {
        showDialog(getResources().getString(R.string.AreYouSure), (dialogInterface, i) -> exportClaims(), (dialog, id) -> dialog.cancel());
    }

    public void ConfirmUploadClaims() {
        showDialog(getResources().getString(R.string.AreYouSure), (dialogInterface, i) -> uploadClaims(), (dialog, id) -> dialog.cancel());
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
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Uploading));
        SynchronizeService.uploadClaims(this);
    }

    public void exportClaims() {
        SynchronizeService.exportClaims(this);
    }
}
