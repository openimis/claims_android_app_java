package org.openimis.imisclaims;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClaimReview extends ImisActivity {
    public String claimText;
    private Map<Integer, Supplier<Fragment>> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_review);

        actionBar.setTitle(getResources().getString(R.string.ClaimReview));

        Intent intent = getIntent();
        claimText = intent.getStringExtra("claims");

        fragmentMap.put(R.id.navigation_home, ReviewFragment::new);
        fragmentMap.put(R.id.navigation_dashboard, ItemsFragment::new);
        fragmentMap.put(R.id.navigation_notifications, ServicesFragment::new);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (fragmentMap.containsKey(itemId)) {
                Supplier<Fragment> sup = fragmentMap.get(itemId);
                if (sup != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, sup.get()).commit();
                    return true;
                }
                return false;
            }
            return false;
        });

        Button restoreButton = findViewById(R.id.restore_button);

        // check if claim is already restored - by comparing prefix
        try {
            JSONObject currentClaim = new JSONObject(this.claimText);
            String currentClaimCode = currentClaim.getString("claim_number");

            if (!currentClaimCode.startsWith(getResources().getString(R.string.restoredClaimNoPrefix))) {
                restoreButton.setOnClickListener(this::restoreClaim);
            } else {
                restoreButton.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Fragment selectedFragment = new ReviewFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, selectedFragment).commit();
    }

    public void restoreClaim(View view) {
        Intent intent = new Intent(this, ClaimActivity.class);
        intent.putExtra(ClaimActivity.EXTRA_CLAIM_DATA, claimText);
        startActivity(intent);
    }
}
