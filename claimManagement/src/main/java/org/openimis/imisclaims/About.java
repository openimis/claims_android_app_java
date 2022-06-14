package org.openimis.imisclaims;

import android.os.Bundle;

public class About extends ImisActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (actionBar != null) {
            actionBar.setTitle("");
        }
    }
}
