package org.openimis.imisclaims;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

import java.io.File;

public class Report extends ImisActivity {
    TextView accepted_count;
    TextView rejected_count;
    TextView pending_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        global = (Global) getApplicationContext();

        accepted_count = findViewById(R.id.valueAccepted);
        rejected_count = findViewById(R.id.valueRejected);
        pending_count = findViewById(R.id.valuePending);

        String AcceptedFolder = global.getSubdirectory("AcceptedClaims");
        String RejectedFolder = global.getSubdirectory("RejectedClaims");
        String PendingFolder = global.getSubdirectory("PendingClaims");
        String TrashFolder = global.getSubdirectory("Trash");

        File acceptedClaims = new File(AcceptedFolder);
        File rejectedClaims = new File(RejectedFolder);
        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

        int countAccepted = 0;
        int countRejected = 0;
        if (acceptedClaims.listFiles().length > 0) {
            for (int i = 0; i < acceptedClaims.listFiles().length; i++) {
                String fname = acceptedClaims.listFiles()[i].getName();
                String str;
                try {
                    str = fname.substring(0, 6);
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
                if (str.equals("Claim_")) {
                    countAccepted++;
                }
            }
        } else {
            countAccepted = 0;
        }

        if (rejectedClaims.listFiles().length > 0) {
            for (int i = 0; i < rejectedClaims.listFiles().length; i++) {
                String fname = rejectedClaims.listFiles()[i].getName();
                String str;
                try {
                    str = fname.substring(0, 6);
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
                if (str.equals("Claim_")) {
                    countRejected++;
                }
            }
        } else {
            countRejected = 0;
        }
        //Pending & Trash
        int count_pending = 0;
        int count_trash = 0;
        if (pendingFolder.listFiles().length > 0) {
            for (int i = 0; i < pendingFolder.listFiles().length; i++) {
                String fname = pendingFolder.listFiles()[i].getName();
                String str;
                try {
                    str = fname.substring(0, 6);
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
                if (str.equals("Claim_")) {
                    count_pending++;
                }
            }
        } else {
            count_pending = 0;
        }

        if (trashFolder.listFiles().length > 0) {
            for (int i = 0; i < trashFolder.listFiles().length; i++) {
                String fname = trashFolder.listFiles()[i].getName();
                String str;
                try {
                    str = fname.substring(0, 6);
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
                if (str.equals("Claim_")) {
                    count_trash++;
                }
            }
        } else {
            count_trash = 0;
        }

        int total_pending = count_pending;

        accepted_count.setText(String.valueOf(countAccepted));
        rejected_count.setText(String.valueOf(countRejected));
        pending_count.setText(String.valueOf(total_pending));

    }
}
