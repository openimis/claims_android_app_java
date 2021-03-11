package org.openimis.imisclaims;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.openimis.general.General;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String Language;
    General _General = new General();
    Global global = new Global();
    Token tokenl;
    SQLHandler sql;
    SQLiteDatabase db;
    ToRestApi toRestApi;
    TextView progressBarinsideText;
    boolean isUserLogged;

    TextView accepted_count;
    TextView rejected_count;
    TextView pending_count;

    TextView AdminName;

    final static String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
    String AcceptedFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/AcceptedClaims/";
    String RejectedFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/RejectedClaims/";
    String PendingFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
    String TrashFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Trash";

    final String VersionField = "AppVersionEnquire";
    NotificationManager mNotificationManager;
    final int SIMPLE_NOTIFICATION_ID = 1;
    Vibrator vibrator;

    ProgressDialog pd;

    Menu menu;

    public static final String PREFS_NAME = "CMPref";
    //final CharSequence[] lang = {"English","Swahili"};
    final CharSequence[] lang = {"English","Francais"};


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermision();
        toRestApi = new ToRestApi();
        tokenl = new Token();

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        new Thread() {
            public void run() {
                CheckForUpdates();
            }

        }.start();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        makeTrashFolder();

        accepted_count = findViewById(R.id.accepted_count);
        rejected_count = findViewById(R.id.rejected_count);
        pending_count = findViewById(R.id.pending_count);

        AdminName = (TextView) findViewById(R.id.AdminName);


        File acceptedClaims = new File(AcceptedFolder);
        File rejectedClaims = new File(RejectedFolder);
        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

        //Accepted & Rejected
        int countAccepted = 0;
        int countRejected = 0;
        if(acceptedClaims.listFiles() != null){
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

        if(rejectedClaims.listFiles() != null){
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
        if(pendingFolder.listFiles() != null){
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

        if(trashFolder.listFiles() != null){
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


    @Override
    public void onResume() {
        refreshCont();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_language) {
                    new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.Select_Language))
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setItems(lang,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if (lang[which].toString() == "English")Language="en";else Language="sw";
                        if (lang[which].toString() == "English")Language="en";else Language="fr";

                        _General.ChangeLanguage(MainActivity.this, Language);
                        isSDCardAvailable();
                        finish();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);

                    }

                }).show();
            return true;
        }
        if (id == R.id.login_logout) {
            Global global = new Global();

            if(tokenl.getTokenText().length() <= 0){
                LoginDialogBox("MainActivity");
            }else{
                global.setUserId(0);
                item.setTitle("Login");
                Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.Logout_Successful),Toast.LENGTH_LONG).show();


            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
        }
        if (id == R.id.nav_enquire) {
            if(!_General.isNetworkAvailable(MainActivity.this)){
                pd.dismiss();
                Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.InternetRequired),Toast.LENGTH_LONG).show();
                return false;
            }
            if(isUserLogged){
                Intent intent = new Intent(this, EnquireActivity.class);
                startActivity(intent);
            }else{
                LoginDialogBoxServices("Enquire");
            }
        }
        if (id == R.id.nav_Map_Items) {
            Intent intent = new Intent(this, MapItems.class);
            startActivity(intent);
        }
        if (id == R.id.nav_Map_Services) {
            Intent intent = new Intent(this, MapServices.class);
            startActivity(intent);
        }
        if (id == R.id.nav_Refresh_Map) {
            if(!_General.isNetworkAvailable(MainActivity.this)){
                Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.InternetRequired),Toast.LENGTH_LONG).show();
                return false;
            }
            //Are you sure dialog
            Global global = new Global();
            int userid = global.getUserId();
            if(tokenl.getTokenText().length() > 0){
                ShowComfirmationDialog();
                //Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();

            }else{
                LoginDialogBox("refresh_map");
            }
        }
        if (id == R.id.nav_claim) {
            Intent intent = new Intent(this, ClaimActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_Reports) {
            Intent intent = new Intent(getApplicationContext(), Report.class);
            startActivity(intent);
/*
                if(!_General.isNetworkAvailable(ClaimActivity.this)){
                    ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }

                if(etHealthFacility.getText().length() == 0){
                    ShowDialog(etHealthFacility,getResources().getString(R.string.MissingHealthFacility));
                    return false;
                }
                if(etClaimAdmin.getText().length() == 0){
                    ShowDialog(etClaimAdmin,getResources().getString(R.string.MissingClaimAdmin));
                    return false;
                }

                Intent Stats = new Intent(ClaimActivity.this,Statistics.class);
                Stats.putExtra("HFCode",etHealthFacility.getText());
                Stats.putExtra("ClaimAdmin",etClaimAdmin.getText());
                ClaimActivity.this.startActivity(Stats);
                return true;*/

        } else if (id == R.id.nav_Sync) {
            if(!_General.isNetworkAvailable(MainActivity.this)){
                Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.InternetRequired),Toast.LENGTH_LONG).show();
                return false;
            }
            Intent intent = new Intent(getApplicationContext(), Synchronize.class);
            startActivity(intent);
        } else if (id == R.id.nav_quit) {
            QuitConfirmDialogBox();

        }else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        } else if (id == R.id.nav_Retrieve) {
            if(!_General.isNetworkAvailable(MainActivity.this)){
                Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.InternetRequired),Toast.LENGTH_LONG).show();
                return false;
            }
            if (tokenl.getTokenText().length() <= 0) {
                LoginDialogBox("search_claims");
            } else {
                Intent intent = new Intent(this, SearchClaims.class);
                startActivity(intent);
            }
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void QuitConfirmDialogBox() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.quit_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Global global = new Global();
                                global.setOfficerCode("");
                                dialog.cancel();
                                finish();
                            }
                        })
                .setNegativeButton(R.string.No,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void initializeDb3File(SQLHandler sql) {
        if (checkDataBase()) {
/*            sql = new SQLHandler(this);
            sql.onOpen(db);*/
            //if(sql.getAdjustibility("ClaimAdministrator").length() == 0){
            if (_General.isNetworkAvailable(this)) {
                //DownloadMasterDialog();
                if (getControls()) {
                    try {
                        if (global.getOfficerCode() == null || global.getOfficerCode().equals("")) {
                            if (!sql.getAdjustibility("ClaimAdministrator").equals("N")) {
                                ClaimAdminDialogBox();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DownloadMasterDialog();
                    }
                } else {
                    DownloadMasterDialog();
                }
            } else {
                if (!sql.checkIfAny("tblControls")) {
                    ErrorDialogBox(getResources().getString(R.string.noControls) + " " + getResources().getString(R.string.provideExtractOrInternet),true);
                } else if (!sql.checkIfAny("tblClaimAdmins")) {
                    if (sql.getAdjustibility("ClaimAdministrator").equals("M"))
                        ErrorDialogBox(getResources().getString(R.string.noAdmins) + " " + getResources().getString(R.string.provideExtractOrInternet),true);
                } else {
                    ClaimAdminDialogBox();
                }
            }


//            }else{
//                ClaimAdminDialogBox();
//            }
        }
    }
    public void makeImisDirectories(){

        String externalDirectory= Environment.getExternalStorageDirectory().toString();
        File folder= new File(externalDirectory + "/IMIS");
        folder.mkdir();

        String Path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
        File myDir = new File(Path1);
        myDir.mkdir();

        File DirRejected = new File(Path1 + "RejectedClaims");
        DirRejected.mkdir();

        File DirAccepted = new File(Path1 + "AcceptedClaims");
        DirAccepted.mkdir();

        File DirTrash = new File(Path1 + "Trash");
        DirTrash.mkdir();

/*        sql = new SQLHandler(this);
        sql.onOpen(db);*/
    }

    public void CreateFolders(){
        String MainPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
        //Here we are creating a directory
        File MyDir = new File(MainPath);
        MyDir.mkdir();
    }
    private void isSDCardAvailable(){

        if (_General.isSDCardAvailable() == 0){
            //Toast.makeText(this, "SD Card is in read only mode.", Toast.LENGTH_LONG);
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardReadOnly))
                    .setCancelable(false)
                    .setPositiveButton("Force close", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();

        }else if(_General.isSDCardAvailable() == -1){
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardMissing))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }else{

        }
    }

    public void LoginDialogBox(final String page) {

        final int[] userid = {0};

        Global global = new Global();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (_General.isNetworkAvailable(MainActivity.this)) {
                                    if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                        pd = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                        new Thread() {
                                            public void run() {
                                                isUserLogged = new Login().LoginToken(username.getText().toString(), password.getText().toString());

                                                if (!isUserLogged) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            pd.dismiss();
                                                            //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                            LoginDialogBox(page);
                                                        }
                                                    });

                                                } else {
                                                    final String finalToken = tokenl.getTokenText();
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (finalToken.length() > 0) {
                                                                pd.dismiss();
                                                                //updateMenuTitlesLogout();
                                                                if (page.equals("MainActivity")) {
/*                                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                            startActivity(intent);*/
                                                                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                                }
                                                                if (page.equals("Enquire")) {
                                                                    Intent intent = new Intent(MainActivity.this, EnquireActivity.class);
                                                                    startActivity(intent);
                                                                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                                }
                                                                if (page.equals("refresh_map")) {
                                                                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                                    ShowComfirmationDialog();
                                                                }
                                                                if(page.equals("search_claims")) {
                                                                    Intent intent = new Intent(MainActivity.this, SearchClaims.class);
                                                                    startActivity(intent);
                                                                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                                }
                                                            } else {
                                                                pd.dismiss();
                                                                //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                                Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                                LoginDialogBox(page);
                                                            }
                                                        }
                                                    });
                                                }


                                            }
                                        }.start();


                                    } else {
                                        LoginDialogBox(page);
                                        Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    ErrorDialogBox(getResources().getString(R.string.CheckInternet));
                                }


                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void LoginDialogBoxServices(final String page) {

        final int[] userid = {0};

        final Global global = new Global();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                    pd = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                    new Thread() {
                                        public void run() {
                                            isUserLogged = new Login().LoginToken(username.getText().toString(), password.getText().toString());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isUserLogged) {
                                                        pd.dismiss();
                                                        //updateMenuTitlesLogout();
                                                        if (page.equals("MainActivity")) {
/*                                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                            startActivity(intent);*/
                                                            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        }
                                                        if (page.equals("Enquire")) {
                                                            Intent intent = new Intent(MainActivity.this, EnquireActivity.class);
                                                            startActivity(intent);
                                                            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        }

                                                    } else {
                                                        pd.dismiss();
                                                        //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                        Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                        LoginDialogBoxServices(page);
                                                    }
                                                }
                                            });
                                        }
                                    }.start();


                                } else {
                                    LoginDialogBox(page);
                                    Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void ClaimAdminDialogBox() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.claim_code_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText claim_code = (EditText) promptsView.findViewById(R.id.ClaimCode);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Continue,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                validateClaimAdminCode(claim_code.getText().toString());
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                finish();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void ErrorDialogBox(final String message) {
        ErrorDialogBox(message,false);
    }

    public void ErrorDialogBox(final String message, final boolean critical) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.error_message_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView error = (TextView) promptsView.findViewById(R.id.error_message);
        error.setText(message);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                if(critical)
                                    finish();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public AlertDialog ShowComfirmationDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.AreYouSure))
                .setCancelable(false)

                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject l = null;
                        try {

/*                            if(!getLastUpdateDate().equals("")){
                                //String date = getLastUpdateDate().substring(0, getLastUpdateDate().indexOf("."));
                                object.put("last_update_date",getLastUpdateDate());
                            }*/
                            JSONObject object1 = new JSONObject();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                            String dateS = formatter.format(new Date(0));
                            object1.put("last_update_date",dateS);

                            DownLoadDiagnosesServicesItemsAgain(object1, sql);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                })
                .show();
    }

    public AlertDialog DownloadMasterDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.getMasterData))
                .setCancelable(false)

                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(getControls()){
                            try{
                                if(global.getOfficerCode() == null || global.getOfficerCode().equals("")){
                                    if(!sql.getAdjustibility("ClaimAdministrator").equals("N")){
                                        ClaimAdminDialogBox();
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                DownloadMasterDialog();
                            }
                        }else{
                            DownloadMasterDialog();
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                finish();
                            }
                        })
                .show();
    }

    private void CheckForUpdates(){
        if(_General.isNetworkAvailable(MainActivity.this)){
            if(_General.isNewVersionAvailable(VersionField,MainActivity.this,getApplicationContext().getPackageName())){
                //Show notification bar
                mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                //final Notification NotificationDetails = new Notification(R.drawable.ic_launcher, getResources().getString(R.string.NotificationAlertText), System.currentTimeMillis());
                //NotificationDetails.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
                //NotificationDetails.setLatestEventInfo(context, ContentTitle, ContentText, intent);
                //mNotificationManager.notify(SIMPLE_NOTFICATION_ID, NotificationDetails);
                Context context = getApplicationContext();
                CharSequence ContentTitle = getResources().getString(R.string.ContentTitle);
                CharSequence ContentText = getResources().getString(R.string.ContentText);

                Intent NotifyIntent = new Intent(this, MainActivity.class);

                PendingIntent intent = PendingIntent.getActivity(this, 0, NotifyIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notification1");
                builder.setAutoCancel(false);
                builder.setContentTitle(ContentTitle);
                builder.setContentText(ContentText);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setContentIntent(intent);
                builder.setOngoing(false);
/*				String s = "ring";
				int res_sound_id = context.getResources().getIdentifier(s, "raw", context.getPackageName());
				Uri u = Uri.parse("android.resource://" + context.getPackageName() + "/" + res_sound_id);
				builder.setSound(u);*/

                try{
                    mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, builder.build());
                }catch (Exception e){
                    e.printStackTrace();
                }

                vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(500);
            }
        }
    }
    private void updateMenuTitlesLogout() {
        MenuItem login_logout = menu.findItem(R.id.login_logout);
        login_logout.setTitle(R.string.Logout);
    }

    public void makeTrashFolder(){

        File DirRejected = new File(Path + "AcceptedClaims");
        File DirAccepted = new File(Path + "RejectedClaims");
        File DirTrash = new File(Path + "Trash");

        DirAccepted.mkdir();
        DirRejected.mkdir();
        DirTrash.mkdir();
    }
    public void refreshCont(){
        File acceptedClaims = new File(AcceptedFolder);
        File rejectedClaims = new File(RejectedFolder);
        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

        int countAccepted = 0;
        int countRejected = 0;
        if(acceptedClaims.listFiles() != null){
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

        if(rejectedClaims.listFiles() != null){
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
        if(pendingFolder.listFiles() != null){
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

        if(trashFolder.listFiles() != null){
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


    public void requestPermisionWRITE_EXTERNAL_STORAGE(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }
    public void requestPermisionINTERNET(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);
    }
    public void requestPermisionVIBRATE(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.VIBRATE},
                1);
    }
    public void requestPermisionCAMERA(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);
    }
    public void requestPermisionACCESS_NETWORK_STATE(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                1);
    }
    public void requestPermisionCHANGE_WIFI_STATE(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CHANGE_WIFI_STATE},
                1);
    }
    public void requestPermisionACCESS_WIFI_STATE(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                        makeImisDirectories();
                        sql = new SQLHandler(this);
                        sql.onOpen(db);
                        sql.createTables();

                        initializeDb3File(sql);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    //Ask for permission
    public void requestPermision(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(Path + "ImisData.db3", null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database doesn't exist yet.
            return false;
        }
        return true;
    }

    public boolean getControls(){
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.getControls);
            pd = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread(){
                public void run() {
                    String controls = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "Claims/Controls";
                    try {
                        String content = toRestApi.getFromRestApi(functionName);

                        JSONObject ob = null;
                        JSONObject obContent = null;

                        ob = new JSONObject(content);
                        error_occurred = ob.getString("error_occured");
                        if (error_occurred.equals("false")) {
                            controls = ob.getString("controls");
                            sql.ClearAll("tblControls");
                            //Insert Diagnosese
                            JSONArray arrControls = null;
                            JSONObject objControls = null;
                            arrControls = new JSONArray(controls);
                            for (int i = 0; i < arrControls.length(); i++) {
                                objControls = arrControls.getJSONObject(i);
                                sql.InsertControls(objControls.getString("fieldName").toString(), objControls.getString("adjustibility"));
                            }

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    getClaimAdmins();
                                }
                            });


                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                }
                            });
                            error_message = ob.getString("error_message");
                            ErrorDialogBox(error_message);
                        }
                    } catch (JSONException e ) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        });

                    } catch ( IOException e ) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    }
                }
            };
            thread.start();
        }else{
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
            return false;
        }
        return true;
    }

    public boolean getClaimAdmins(){
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.application);
            pd = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread(){
                public void run() {
                    String controls = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "Claims/GetClaimAdmins";
                    try {
                        String content = toRestApi.getFromRestApi(functionName);

                        JSONObject ob = null;
                        JSONObject obContent = null;

                        ob = new JSONObject(content);
                        //error_occurred = ob.getString("error_occured");
                        //if(error_occurred.equals("true")){
                        controls = ob.getString("claim_admins");
                        sql.ClearAll("tblClaimAdmins");
                        //Insert Diagnosese
                        JSONArray arrControls = null;
                        JSONObject objControls = null;
                        arrControls = new JSONArray(controls);
                        for (int i = 0; i < arrControls.length(); i++) {
                            objControls = arrControls.getJSONObject(i);
                            String lastName = objControls.getString("lastName").toString();
                            String otherNames = objControls.getString("otherNames").toString();
                            String name = lastName + " " + otherNames;
                            sql.InsertClaimAdmins(objControls.getString("claimAdminCode"), name);
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.initializing_complete), Toast.LENGTH_LONG).show();
                            }
                        });
                        /*}else {
                            runOnUiThread(new Runnable() {
                                public void run() {pd.dismiss();
                                }
                            });
                            error_message = ob.getString("error_message");
                            ErrorDialogBox(error_message);
                        }*/
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        });

                    } catch ( IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    }
                }
            };
            thread.start();
        }else{
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
            return false;
        }
        return true;
    }

    public void validateClaimAdminCode(final String ClaimCode){
        if(ClaimCode.equals("")){
            Toast.makeText(getBaseContext(), R.string.MissingClaimCode, Toast.LENGTH_LONG).show();
            ClaimAdminDialogBox();
        }else{
            String ClaimName = sql.getClaimAdmin(ClaimCode);
            if(ClaimName.equals("")){
                Toast.makeText(MainActivity.this,getResources().getString(R.string.invalid_code),Toast.LENGTH_LONG).show();
                ClaimAdminDialogBox();
            }else{
                if(!sql.getAdjustibility("ClaimAdministrator").equals("N")){
                    Global global = new Global();
                    global.setOfficerCode(ClaimCode);
                    global.setOfficerName(ClaimName);
                    AdminName = (TextView) findViewById(R.id.AdminName);
                    AdminName.setText(global.getOfficeName());
                    sql = new SQLHandler(MainActivity.this);
                    Cursor c = sql.getMapping("I");
                    if(c.getCount() == 0){
                        try {
                                /* if(!getLastUpdateDate().equals("")){
                                     //String date = getLastUpdateDate().substring(0, getLastUpdateDate().indexOf("."));
                                       object.put("last_update_date",getLastUpdateDate());
                                }*///object.put("last_update_date","2019/02/12");

                            pd.dismiss();
                            JSONObject object = new JSONObject();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                            String dateS = formatter.format(new Date(0));
                            object.put("last_update_date",dateS);
                            try {
                                DownLoadDiagnosesServicesItems(object, sql);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    Global global = new Global();
                    sql = new SQLHandler(MainActivity.this);
                    Cursor c = sql.getMapping("I");
                    if(c.getCount() == 0){
                        try {
                                /* if(!getLastUpdateDate().equals("")){
                                     //String date = getLastUpdateDate().substring(0, getLastUpdateDate().indexOf("."));
                                       object.put("last_update_date",getLastUpdateDate());
                                }*///object.put("last_update_date","2019/02/12");

                            pd.dismiss();
                            JSONObject object = new JSONObject();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                            String dateS = formatter.format(new Date(0));
                            object.put("last_update_date",dateS);
                            try {
                                DownLoadDiagnosesServicesItems(object, sql);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public void DownLoadDiagnosesServicesItems(final JSONObject object, final SQLHandler sql) throws IOException {

        final String[] content = new String[1];
        final HttpResponse[] resp = {null};
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.Diagnoses)+", "+getResources().getString(R.string.Services)+", "+getResources().getString(R.string.Items)+"...";
            pd = ProgressDialog.show(this, getResources().getString(R.string.Checking_For_Updates), progress_message);
            Thread thread = new Thread(){
                public void run() {
                    String diagnoses = null;
                    String services = null;
                    String items = null;
                    String last_update_date = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "GetDiagnosesServicesItems";

                    try {
                        HttpResponse response = toRestApi.postToRestApi(object,functionName);
                        resp[0] = response;
                        HttpEntity respEntity = response.getEntity();
                        if (respEntity != null) {
                            final String[] code = {null};
                            // EntityUtils to get the response content

                            content[0] = EntityUtils.toString(respEntity);

                        }

                        JSONObject ob = null;
                        try {
                            ob = new JSONObject(content[0]);
                            if(String.valueOf(response.getStatusLine().getStatusCode()).equals("200")){
                                diagnoses = ob.getString("diagnoses");
                                services = ob.getString("services");
                                items = ob.getString("items");
                                last_update_date = ob.getString("update_since_last");
                                saveLastUpdateDate(last_update_date);

                                sql.ClearAll("tblReferences");
                                sql.ClearMapping("S");
                                sql.ClearMapping("I");
                                //Insert Diagnosese
                                JSONArray arrDiagnoses = null;
                                JSONObject objDiagnoses = null;
                                arrDiagnoses = new JSONArray(diagnoses);
                                for(int i=0; i < arrDiagnoses.length(); i++){
                                    objDiagnoses = arrDiagnoses.getJSONObject(i);
                                    sql.InsertReferences(objDiagnoses.getString("code").toString(),objDiagnoses.getString("name").toString(),"D","");
                                }

                                //Insert Services
                                JSONArray arrServices = null;
                                JSONObject objServices = null;
                                arrServices = new JSONArray(services);
                                for(int i=0; i < arrServices.length(); i++){
                                    objServices = arrServices.getJSONObject(i);
                                    sql.InsertReferences(objServices.getString("code").toString(),objServices.getString("name").toString(),"S",objServices.getString("price").toString());
                                    sql.InsertMapping(objServices.getString("code").toString(),objServices.getString("name").toString(),"S");
                                }

                                //Insert Items
                                JSONArray arrItems = null;
                                JSONObject objItems = null;
                                arrItems = new JSONArray(items);
                                for(int i=0; i < arrItems.length(); i++){
                                    objItems = arrItems.getJSONObject(i);
                                    sql.InsertReferences(objItems.getString("code").toString(),objItems.getString("name").toString(),"I",objItems.getString("price").toString());
                                    sql.InsertMapping(objItems.getString("code").toString(),objItems.getString("name").toString(),"I");
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this,getResources().getString(R.string.installed_updates),Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else {
                                error_occurred = ob.getString("error_occured");
                                if(error_occurred.equals("true")){
                                    error_message = ob.getString("error_message");

                                    final String finalError_message = error_message;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            Toast.makeText(MainActivity.this,finalError_message,Toast.LENGTH_LONG).show();
                                            ClaimAdminDialogBox();
                                        }
                                    });
                                }else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            Toast.makeText(MainActivity.this,getResources().getString(R.string.SomethingWentWrongServer),Toast.LENGTH_LONG).show();
                                            ClaimAdminDialogBox();
                                        }
                                    });

                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    ClaimAdminDialogBox();
                                }
                            });
                            Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();

                        }
                    }catch (Exception e){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this,resp[0].getStatusLine().getStatusCode()+"-"+getResources().getString(R.string.SomethingWentWrongServer),Toast.LENGTH_LONG).show();
                                ClaimAdminDialogBox();
                            }
                        });
                    }
                }
            };

            thread.start();
        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                }
            });
            ClaimAdminDialogBox();
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    public void DownLoadDiagnosesServicesItemsAgain(final JSONObject object, final SQLHandler sql) throws IOException {

        final String[] content = new String[1];
        final HttpResponse[] resp = {null};
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.refresh_mapping);
            pd = ProgressDialog.show(this, getResources().getString(R.string.Checking_For_Updates), progress_message);
            Thread thread = new Thread(){
                public void run() {
                    String diagnoses = null;
                    String services = null;
                    String items = null;
                    String last_update_date = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "GetDiagnosesServicesItems";

                    try {
                        HttpResponse response = toRestApi.postToRestApi(object,functionName);
                        resp[0] = response;
                        HttpEntity respEntity = response.getEntity();
                        if (respEntity != null) {
                            final String[] code = {null};
                            // EntityUtils to get the response content

                            content[0] = EntityUtils.toString(respEntity);

                        }

                        JSONObject ob = null;
                        try {
                            ob = new JSONObject(content[0]);
                            if(String.valueOf(response.getStatusLine().getStatusCode()).equals("200")){
                                diagnoses = ob.getString("diagnoses");
                                services = ob.getString("services");
                                items = ob.getString("items");
                                last_update_date = ob.getString("update_since_last");
                                saveLastUpdateDate(last_update_date);

                                sql.ClearAll("tblReferences");
                                sql.ClearMapping("S");
                                sql.ClearMapping("I");
                                //Insert Diagnosese
                                JSONArray arrDiagnoses = null;
                                JSONObject objDiagnoses = null;
                                arrDiagnoses = new JSONArray(diagnoses);
                                for(int i=0; i < arrDiagnoses.length(); i++){
                                    objDiagnoses = arrDiagnoses.getJSONObject(i);
                                    sql.InsertReferences(objDiagnoses.getString("code").toString(),objDiagnoses.getString("name").toString(),"D","");
                                }

                                //Insert Services
                                JSONArray arrServices = null;
                                JSONObject objServices = null;
                                arrServices = new JSONArray(services);
                                for(int i=0; i < arrServices.length(); i++){
                                    objServices = arrServices.getJSONObject(i);
                                    sql.InsertReferences(objServices.getString("code").toString(),objServices.getString("name").toString(),"S",objServices.getString("price").toString());
                                    sql.InsertMapping(objServices.getString("code").toString(),objServices.getString("name").toString(),"S");
                                }

                                //Insert Items
                                JSONArray arrItems = null;
                                JSONObject objItems = null;
                                arrItems = new JSONArray(items);
                                for(int i=0; i < arrItems.length(); i++){
                                    objItems = arrItems.getJSONObject(i);
                                    sql.InsertReferences(objItems.getString("code").toString(),objItems.getString("name").toString(),"I",objItems.getString("price").toString());
                                    sql.InsertMapping(objItems.getString("code").toString(),objItems.getString("name").toString(),"I");
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this,getResources().getString(R.string.installed_updates),Toast.LENGTH_LONG).show();

                                        Global global = new Global();
                                        JSONObject object = new JSONObject();
                                        try {
                                            object.put("claim_administrator_code",global.getOfficerCode());
                                            DownLoadServicesItemsPriceList(object, sql);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });

                            }else {
                                error_occurred = ob.getString("error_occured");
                                if(error_occurred.equals("true")){
                                    error_message = ob.getString("error_message");

                                    final String finalError_message = error_message;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            Toast.makeText(MainActivity.this,finalError_message,Toast.LENGTH_LONG).show();
                                            ClaimAdminDialogBox();
                                        }
                                    });
                                }else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            Toast.makeText(MainActivity.this,getResources().getString(R.string.SomethingWentWrongServer),Toast.LENGTH_LONG).show();
                                            ClaimAdminDialogBox();
                                        }
                                    });

                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    ClaimAdminDialogBox();
                                }
                            });
                            Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();

                        }
                    }catch (Exception e){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this,resp[0].getStatusLine().getStatusCode()+"-"+getResources().getString(R.string.SomethingWentWrongServer),Toast.LENGTH_LONG).show();
                                ClaimAdminDialogBox();
                            }
                        });
                    }
                }
            };

            thread.start();
        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                }
            });
            //ClaimAdminDialogBox();
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    private void DownLoadServicesItemsPriceList(final JSONObject object, final  SQLHandler sql) throws IOException {
        final HttpResponse[] resp = {null};
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.Services)+", "+getResources().getString(R.string.Items)+"...";
            pd = ProgressDialog.show(this, getResources().getString(R.string.mapping), progress_message);
            Thread thread = new Thread() {
                public void run() {

                    String services = null;
                    String items = null;
                    String error_occurred = null;
                    String error_message = null;
                    String last_update_date = null;
                    String content = null;

                    String functionName = "getpaymentlists";
                    try{
                        HttpResponse response = toRestApi.postToRestApiToken(object,functionName);
                        resp[0] = response;
                        HttpEntity respEntity = response.getEntity();
                        if (respEntity != null) {
                            final String[] code = {null};
                            // EntityUtils to get the response content
                            try {
                                content = EntityUtils.toString(respEntity);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        int code = response.getStatusLine().getStatusCode();

                        JSONObject ob = null;
                        try {
                            ob = new JSONObject(content);
                            if(String.valueOf(code).equals("200")){
                                services = ob.getString("pricelist_services");
                                items = ob.getString("pricelist_items");
                                last_update_date = ob.getString("update_since_last");
                                saveLastUpdateDate(last_update_date);

                                //sql.ClearReferencesSI();
                                sql.ClearMapping("S");
                                sql.ClearMapping("I");

                                //Insert Services
                                JSONArray arrServices = null;
                                JSONObject objServices = null;
                                arrServices = new JSONArray(services);
                                for (int i = 0; i < arrServices.length(); i++) {
                                    objServices = arrServices.getJSONObject(i);
                                    //sql.InsertReferences(objServices.getString("code").toString(), objServices.getString("name").toString(), "S", objServices.getString("price").toString());
                                    sql.InsertMapping(objServices.getString("code").toString(),objServices.getString("name").toString(),"S");
                                }

                                //Insert Items
                                JSONArray arrItems = null;
                                JSONObject objItems = null;
                                arrItems = new JSONArray(items);
                                for (int i = 0; i < arrItems.length(); i++) {
                                    objItems = arrItems.getJSONObject(i);
                                    //sql.InsertReferences(objItems.getString("code").toString(), objItems.getString("name").toString(), "I", objItems.getString("price").toString());
                                    sql.InsertMapping(objItems.getString("code").toString(),objItems.getString("name").toString(),"I");
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this,getResources().getString(R.string.MapSuccessful),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{
                                error_occurred = ob.getString("error_occured");
                                if(error_occurred.equals("true")){
                                    if(code >= 400){
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                pd.dismiss();
                                                LoginDialogBox("refresh_map");
                                            }
                                        });
                                    }else{
                                        error_message = ob.getString("error_message");
                                        final String finalError_message = error_message;
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                pd.dismiss();
                                            }
                                        });
                                        ErrorDialogBox(finalError_message);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            runOnUiThread(new Runnable() {
                                public void run() {pd.dismiss();}});
                            Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this,resp[0].getStatusLine().getStatusCode() +"-"+getResources().getString(R.string.AccessDenied),Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            };
            thread.start();
        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                }
            });
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }

    }
    public void saveLastUpdateDate(String last_update_date) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //handle case of no SDCARD present
        } else {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/Authentications/";
            //create folder
            File folder = new File(dir); //folder name
            folder.mkdirs();

            //create file
            File file = new File(dir, "last_update_date.txt");
            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(last_update_date);
                myOutWriter.close();
                fOut.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getLastUpdateDate(){
        String aBuffer = "";
        try {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/Authentications/";
            File myFile = new File("/"+dir+"/last_update_date.txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }

}
