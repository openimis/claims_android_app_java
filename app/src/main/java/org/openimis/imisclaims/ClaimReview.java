package org.openimis.imisclaims;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class ClaimReview extends AppCompatActivity {

    public String claims = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container,selectedFragment).commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_review);

        Intent intent = getIntent();
        claims = intent.getStringExtra("claims");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Fragment selectedFragment = new ReviewFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container,selectedFragment).commit();
    }

}
