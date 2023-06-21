package org.openimis.imisclaims.claimlisting;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.openimis.imisclaims.ClaimActivity;
import org.openimis.imisclaims.ImisActivity;
import org.openimis.imisclaims.R;

public class ClaimListingActivity extends ImisActivity {
    private ViewPager claimsView;
    private TabLayout claimCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_claim_listing);

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
        } else {
            onBackPressed();
        }
        return true;
    }
}
