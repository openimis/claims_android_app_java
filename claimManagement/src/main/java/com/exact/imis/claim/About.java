package com.exact.imis.claim;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.exact.imis.claim.R;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
