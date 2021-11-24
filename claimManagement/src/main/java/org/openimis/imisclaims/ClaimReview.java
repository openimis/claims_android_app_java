package org.openimis.imisclaims;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

public class ClaimReview extends ImisActivity {

    public String claims = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                selectedFragment = new ReviewFragment();
                break;
            case R.id.navigation_dashboard:
                selectedFragment = new ItemsFragment();
                break;
            case R.id.navigation_notifications:
                selectedFragment = new ServicesFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, selectedFragment).commit();
        return true;
    };

    private View.OnClickListener restoreButtonListener = v -> restoreClaim(v);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_review);

        actionBar.setTitle(getResources().getString(R.string.ClaimReview));

        Intent intent = getIntent();
        claims = intent.getStringExtra("claims");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button restoreButton = (Button) findViewById(R.id.restore_button);
        restoreButton.setOnClickListener(restoreButtonListener);

        Fragment selectedFragment = new ReviewFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, selectedFragment).commit();
    }

    public void restoreClaim(View view) {
        Intent intent = new Intent(this, ClaimActivity.class);
        intent.putExtra("claims", claims);
        startActivity(intent);
    }

}
