package org.openimis.imisclaims;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.domain.entity.ClaimAdmin;

import java.util.HashMap;
import java.util.Map;

public class ClaimReview extends ImisActivity {
    private static final String CLAIM_EXTRA = "claims";

    public static Intent newIntent(@NonNull Context context, @NonNull Claim claim) {
        return new Intent(context, ClaimReview.class).putExtra(CLAIM_EXTRA, claim);
    }

    public Claim claim;
    private Map<Integer, Supplier<Fragment>> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_review);

        actionBar.setTitle(getResources().getString(R.string.ClaimReview));
        claim = getIntent().getParcelableExtra(CLAIM_EXTRA);

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
        String currentClaimCode = claim.getClaimNumber();
        if (!currentClaimCode.startsWith(getResources().getString(R.string.restoredClaimNoPrefix))) {
            restoreButton.setOnClickListener(this::restoreClaim);
        } else {
            restoreButton.setVisibility(View.GONE);
        }


        Fragment selectedFragment = new ReviewFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, selectedFragment).commit();
    }

    public void restoreClaim(View view) {
        startActivity(ClaimActivity.newIntent(this,claim));
    }
}
