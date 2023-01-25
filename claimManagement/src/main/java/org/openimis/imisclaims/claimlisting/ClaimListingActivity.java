package org.openimis.imisclaims.claimlisting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.ClaimActivity;
import org.openimis.imisclaims.ImisActivity;
import org.openimis.imisclaims.R;
import org.openimis.imisclaims.SynchronizeService;
import org.openimis.imisclaims.tools.Log;

import java.util.ArrayList;

public class ClaimListingActivity extends ImisActivity {
    private static final String LOG_TAG = "CLAIMLISTING";
    private ViewPager claimsView;
    private TabLayout claimCategories;
    private ArrayList<String> broadcastList;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_claim_listing);

        broadcastList = new ArrayList<>();
        broadcastList.add(SynchronizeService.ACTION_UPDATE_SUCCESS);
        broadcastList.add(SynchronizeService.ACTION_UPDATE_ERROR);

        claimsView = findViewById(R.id.claimsView);
        ClaimListingPageAdapter claimListingPageAdapter = new ClaimListingPageAdapter(this, getSupportFragmentManager());
        claimsView.setAdapter(claimListingPageAdapter);
        claimCategories = findViewById(R.id.claimCategories);
        claimCategories.setupWithViewPager(claimsView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_claim_listing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, ClaimActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_sync) {
            updateClaim();
        } else {
            onBackPressed();
        }
        return true;
    }

    private void updateClaim() {
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Updating));
        doLoggedIn(() -> SynchronizeService.updateClaims(this), () -> showDialog(getResources().getString(R.string.CheckInternet)));
    }

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case SynchronizeService.ACTION_UPDATE_SUCCESS:
                try {
                    JSONObject result = new JSONObject(intent.getStringExtra(SynchronizeService.EXTRA_UPDATE_RESPONSE));
                    String updateMessage = String.format("%s\n%s",
                            getResources().getString(R.string.NewClaimsStatus, result.getInt("newClaims")),
                            getResources().getString(R.string.UpdatedClaimsStatus, result.getInt("claimUpdates")));
                    showDialog(updateMessage, (d, i) -> refresh());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error while parsing claim update action result", e);
                }
                break;
            case SynchronizeService.ACTION_UPDATE_ERROR:
                String errorMessage = intent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE);
                showDialog(getResources().getString(R.string.Error), errorMessage);
                break;
        }

        if (pd != null && pd.isShowing()) pd.dismiss();
    }

    @Override
    protected ArrayList<String> getBroadcastList() {
        return broadcastList;
    }
}
