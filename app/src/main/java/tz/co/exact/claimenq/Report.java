package tz.co.exact.claimenq;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class Report extends AppCompatActivity {
    TextView accepted_count;
    TextView rejected_count;
    TextView pending_count;


    String AcceptedFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/AcceptedClaims/";
    String RejectedFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/RejectedClaims/";
    String PendingFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
    String TrashFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Trash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        accepted_count = findViewById(R.id.valueAccepted);
        rejected_count = findViewById(R.id.valueRejected);
        pending_count = findViewById(R.id.valuePending);


        File acceptedClaims = new File(AcceptedFolder);
        File rejectedClaims = new File(RejectedFolder);
        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

        int countAccepted = 0;
        int countRejected = 0;
        if(acceptedClaims.listFiles().length > 0){
            for(int i = 0; i< acceptedClaims.listFiles().length; i++){
                String fname = acceptedClaims.listFiles()[i].getName();
                String str;
                try{
                    str = fname.substring(0,6);
                }catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                if(str.equals("Claim_")){
                    countAccepted++;
                }
            }
        }else {
            countAccepted = 0;
        }

        if(rejectedClaims.listFiles().length > 0){
            for(int i = 0; i< rejectedClaims.listFiles().length; i++){
                String fname = rejectedClaims.listFiles()[i].getName();
                String str;
                try{
                    str = fname.substring(0,6);
                }catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                if(str.equals("Claim_")){
                    countRejected++;
                }
            }
        }else {
            countRejected = 0;
        }
        //Pending & Trash
        int count_pending = 0;
        int count_trash = 0;
        if(pendingFolder.listFiles().length > 0){
            for(int i = 0; i< pendingFolder.listFiles().length; i++){
                String fname = pendingFolder.listFiles()[i].getName();
                String str;
                try{
                    str = fname.substring(0,6);
                }catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                if(str.equals("Claim_")){
                    count_pending++;
                }
            }
        }else {
            count_pending = 0;
        }

        if(trashFolder.listFiles().length > 0){
            for(int i = 0; i< trashFolder.listFiles().length; i++){
                String fname = trashFolder.listFiles()[i].getName();
                String str;
                try{
                    str = fname.substring(0,6);
                }catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                if(str.equals("Claim_")){
                    count_trash++;
                }
            }
        }else {
            count_trash = 0;
        }

        int total_pending = count_trash + count_pending;

        accepted_count.setText(String.valueOf(countAccepted));
        rejected_count.setText(String.valueOf(countRejected));
        pending_count.setText(String.valueOf(total_pending));

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
