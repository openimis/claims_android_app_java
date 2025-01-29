package org.openimis.imisclaims;

import android.os.Bundle;
import android.widget.TextView;

public class About extends ImisActivity {
    TextView appVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appVersion = findViewById(R.id.tvAppVersion);
        appVersion.setText(BuildConfig.VERSION_NAME);

        if (actionBar != null) {
            actionBar.setTitle("");
        }
    }
}
