package org.openimis.imisclaims;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

public class MainActivity extends ImisActivity {
    ArrayList<String> broadcastList;
    final CharSequence[] lang = {"English", "Francais"};
    String Language;

    SQLHandler sql;
    SQLiteDatabase db;
    ToRestApi toRestApi;

    TextView accepted_count;
    TextView rejected_count;
    TextView pending_count;
    TextView AdminName;
    DrawerLayout drawer;
    ProgressDialog pd;

    Menu menu;
    static String Path;

    final String VersionField = "AppVersionEnquire";
    NotificationManager mNotificationManager;
    final int SIMPLE_NOTIFICATION_ID = 1;
    Vibrator vibrator;

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        String action = intent.getAction();
        if (SynchronizeService.ACTION_CLAIM_COUNT_RESULT.equals(action)) {
            accepted_count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ACCEPTED, 0)));
            rejected_count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_REJECTED, 0)));
            pending_count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_PENDING, 0)));
        }
    }

    @Override
    protected ArrayList<String> getBroadcastList() {
        return broadcastList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        isSDCardAvailable();

        broadcastList = new ArrayList<>();
        broadcastList.add(SynchronizeService.ACTION_CLAIM_COUNT_RESULT);

        toRestApi = new ToRestApi();

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        new Thread(this::checkForUpdates).start();

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        accepted_count = findViewById(R.id.accepted_count);
        rejected_count = findViewById(R.id.rejected_count);
        pending_count = findViewById(R.id.pending_count);

        accepted_count.setText("0");
        rejected_count.setText("0");
        pending_count.setText("0");

        AdminName = findViewById(R.id.AdminName);
    }

    @Override
    public void onResume() {
        super.onResume();
        SynchronizeService.getClaimCount(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.change_language) {
            showSelectDialog(
                    getResources().getString(R.string.Select_Language),
                    lang,
                    (dialog, which) -> {
                        //if (lang[which].toString() == "English")Language="en";else Language="sw";
                        if (lang[which].toString().equals("English")) Language = "en";
                        else Language = "fr";
                        global.ChangeLanguage(Language);
                        refresh();
                    },
                    null
            );
            return true;
        } else if (id == R.id.login_logout) {
//            if(tokenl.getTokenText().length() <= 0){
//                LoginDialogBox("MainActivity");
//            }else {
//                global.setUserId(0);
//                item.setTitle("Login");
//                Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.Logout_Successful), Toast.LENGTH_LONG).show();
//            }
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_enquire) {
            doLoggedIn(() -> {
                Intent intent = new Intent(this, EnquireActivity.class);
                startActivity(intent);
            });
        } else if (id == R.id.nav_Map_Items) {
            Intent intent = new Intent(this, MapItems.class);
            startActivity(intent);
        } else if (id == R.id.nav_Map_Services) {
            Intent intent = new Intent(this, MapServices.class);
            startActivity(intent);
        } else if (id == R.id.nav_Refresh_Map) {
            confirmRefreshMap();
        } else if (id == R.id.nav_claim) {
            Intent intent = new Intent(this, ClaimActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Reports) {
            Intent intent = new Intent(getApplicationContext(), Report.class);
            startActivity(intent);
        } else if (id == R.id.nav_Sync) {
            Intent intent = new Intent(getApplicationContext(), SynchronizeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_quit) {
            showDialog(
                    getResources().getString(R.string.AreYouSure),
                    (dialog, i) -> {
                        global.setOfficerCode("");
                        finish();
                    },
                    (dialog, i) -> dialog.cancel()
            );
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        } else if (id == R.id.nav_Retrieve) {
            doLoggedIn(() -> {
                Intent intent = new Intent(this, SearchClaimsActivity.class);
                startActivity(intent);
            });
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public AlertDialog confirmRefreshMap() {
        return showDialog(
                getResources().getString(R.string.AreYouSure),
                (dialog, i) -> {
                    try {
                        JSONObject object1 = new JSONObject();
                        object1.put("last_update_date", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(0)));
                        DownLoadDiagnosesServicesItemsAgain(object1, sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void initializeDb3File(SQLHandler sql) {
        if (checkDataBase()) {
            if (global.isNetworkAvailable()) {
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
                    CriticalErrorDialogBox(getResources().getString(R.string.noControls) + " " + getResources().getString(R.string.provideExtractOrInternet));
                } else if (!sql.checkIfAny("tblClaimAdmins")) {
                    if (sql.getAdjustibility("ClaimAdministrator").equals("M"))
                        CriticalErrorDialogBox(getResources().getString(R.string.noAdmins) + " " + getResources().getString(R.string.provideExtractOrInternet));
                } else {
                    ClaimAdminDialogBox();
                }
            }

        }
    }

    private void isSDCardAvailable() {
        String status = global.getSDCardStatus();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            //Toast.makeText(this, "SD Card is in read only mode.", Toast.LENGTH_LONG);
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardReadOnly))
                    .setCancelable(false)
                    .setPositiveButton("Force close", (dialog, which) -> finish()).show();

        } else if (!Environment.MEDIA_MOUNTED.equals(status)) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardMissing))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), (dialog, which) -> finish()).show();
        }
    }

    public void ClaimAdminDialogBox() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.claim_code_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final EditText claim_code = promptsView.findViewById(R.id.ClaimCode);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Continue,
                        (dialog, id) -> validateClaimAdminCode(claim_code.getText().toString()))
                .setNegativeButton(R.string.Cancel,
                        (dialog, id) -> finish())
                .show();
    }

    public void ErrorDialogBox(final String message) {
        showDialog(message);
    }

    public void CriticalErrorDialogBox(final String message) {
        showDialog(message, (dialog, i) -> finish());
    }

    public AlertDialog DownloadMasterDialog() {
        return showDialog(getResources().getString(R.string.getMasterData),
                (dialogInterface, i) -> {
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
                },
                (dialog, id) -> finish()
        );
    }

    private void checkForUpdates() {
        if (global.isNetworkAvailable()) {
            if (global.isNewVersionAvailable(VersionField, getApplicationContext().getPackageName())) {
                //Show notification bar
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                CharSequence ContentTitle = getResources().getString(R.string.ContentTitle);
                CharSequence ContentText = getResources().getString(R.string.ContentText);

                Intent NotifyIntent = new Intent(this, MainActivity.class);

                PendingIntent intent = PendingIntent.getActivity(this, 0, NotifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notification1");
                builder.setAutoCancel(false);
                builder.setContentTitle(ContentTitle);
                builder.setContentText(ContentText);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setContentIntent(intent);
                builder.setOngoing(false);

                try {
                    mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(500);
            }
        }
    }

    public void refreshCount() {
        SynchronizeService.getClaimCount(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                //check if databases exist in phone
                File database = new File(SQLHandler.DB_NAME_DATA);
                File databaseMapping = new File(SQLHandler.DB_NAME_MAPPING);
                //if one of database not exist - then init it and fill master data etc
                if (!database.exists() || !databaseMapping.exists()) {
                    sql = new SQLHandler(this);
                    sql.onOpen(db);
                    sql.createTables();
                    initializeDb3File(sql);
                } else {
                    //if databases exist: show only claim admin dialog box without init data
                    sql = new SQLHandler(this);
                    sql.onOpen(db);
                    ClaimAdminDialogBox();
                }
                refreshCount();

            } else {
                // permission denied
            }
        }
    }

    //Ask for permission
    public void requestPermissions() {
        int PERMISSION_ALL = 1;
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE};
        }
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }

        // Ask for "Manage External Storage" permission, required in Android 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
            }
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
            checkDB = SQLiteDatabase.openDatabase(SQLHandler.DB_NAME_DATA, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database doesn't exist yet.
            return false;
        }
        return true;
    }

    public boolean getControls() {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.getControls);
            pd = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    String controls = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "claim/Controls";
                    try {
                        String content = toRestApi.getFromRestApi(functionName);

                        JSONObject ob;

                        ob = new JSONObject(content);
                        error_occurred = ob.getString("error_occured");
                        if (error_occurred.equals("false")) {
                            controls = ob.getString("controls");
                            sql.ClearAll("tblControls");
                            //Insert Diagnosese
                            JSONArray arrControls;
                            JSONObject objControls;
                            arrControls = new JSONArray(controls);
                            for (int i = 0; i < arrControls.length(); i++) {
                                objControls = arrControls.getJSONObject(i);
                                sql.InsertControls(objControls.getString("fieldName"), objControls.getString("adjustibility"));
                            }

                            runOnUiThread(() -> {
                                pd.dismiss();
                                getClaimAdmins();
                            });


                        } else {
                            runOnUiThread(() -> pd.dismiss());
                            error_message = ob.getString("error_message");
                            ErrorDialogBox(error_message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> pd.dismiss());
                    }
                }
            };
            thread.start();
        } else {
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
            return false;
        }
        return true;
    }

    public boolean getClaimAdmins() {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.application);
            pd = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    String controls;

                    String functionName = "claim/GetClaimAdmins";
                    try {
                        String content = toRestApi.getFromRestApi(functionName);

                        JSONObject ob;

                        ob = new JSONObject(content);
                        controls = ob.getString("claim_admins");
                        sql.ClearAll("tblClaimAdmins");
                        //Insert Diagnosese
                        JSONArray arrControls;
                        JSONObject objControls;
                        arrControls = new JSONArray(controls);
                        for (int i = 0; i < arrControls.length(); i++) {
                            objControls = arrControls.getJSONObject(i);
                            String lastName = objControls.getString("lastName");
                            String otherNames = objControls.getString("otherNames");
                            String name = lastName + " " + otherNames;
                            sql.InsertClaimAdmins(objControls.getString("claimAdminCode"), name);
                        }

                        runOnUiThread(() -> {
                            pd.dismiss();
                            showToast(R.string.initializing_complete);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> pd.dismiss());
                    }
                }
            };
            thread.start();
        } else {
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
            return false;
        }
        return true;
    }

    public void validateClaimAdminCode(final String ClaimCode) {
        if (ClaimCode.equals("")) {
            Toast.makeText(getBaseContext(), R.string.MissingClaimCode, Toast.LENGTH_LONG).show();
            ClaimAdminDialogBox();
        } else {
            String ClaimName = sql.getClaimAdmin(ClaimCode);
            if (ClaimName.equals("")) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.invalid_code), Toast.LENGTH_LONG).show();
                ClaimAdminDialogBox();
            } else {
                if (!sql.getAdjustibility("ClaimAdministrator").equals("N")) {
                    global.setOfficerCode(ClaimCode);
                    global.setOfficerName(ClaimName);
                    AdminName = (TextView) findViewById(R.id.AdminName);
                    AdminName.setText(global.getOfficeName());
                    sql = new SQLHandler(MainActivity.this);
                    Cursor c = sql.getMapping("I");
                    if (c.getCount() == 0) {
                        try {
                                /* if(!getLastUpdateDate().equals("")){
                                     //String date = getLastUpdateDate().substring(0, getLastUpdateDate().indexOf("."));
                                       object.put("last_update_date",getLastUpdateDate());
                                }*///object.put("last_update_date","2019/02/12");

                            pd.dismiss();
                            JSONObject object = new JSONObject();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            String dateS = formatter.format(new Date(0));
                            object.put("last_update_date", dateS);
                            try {
                                DownLoadDiagnosesServicesItems(object, sql);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    sql = new SQLHandler(MainActivity.this);
                    Cursor c = sql.getMapping("I");
                    if (c.getCount() == 0) {
                        try {
                                /* if(!getLastUpdateDate().equals("")){
                                     //String date = getLastUpdateDate().substring(0, getLastUpdateDate().indexOf("."));
                                       object.put("last_update_date",getLastUpdateDate());
                                }*///object.put("last_update_date","2019/02/12");

                            pd.dismiss();
                            JSONObject object = new JSONObject();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            String dateS = formatter.format(new Date(0));
                            object.put("last_update_date", dateS);
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
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.Diagnoses) + ", " + getResources().getString(R.string.Services) + ", " + getResources().getString(R.string.Items) + "...";
            pd = ProgressDialog.show(this, getResources().getString(R.string.Checking_For_Updates), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    String diagnoses = null;
                    String services = null;
                    String items = null;
                    String last_update_date = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "claim/GetDiagnosesServicesItems";

                    try {
                        HttpResponse response = toRestApi.postToRestApi(object, functionName);
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
                            if (String.valueOf(response.getStatusLine().getStatusCode()).equals("200")) {
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
                                for (int i = 0; i < arrDiagnoses.length(); i++) {
                                    objDiagnoses = arrDiagnoses.getJSONObject(i);
                                    sql.InsertReferences(objDiagnoses.getString("code"), objDiagnoses.getString("name"), "D", "");
                                }

                                //Insert Services
                                JSONArray arrServices = null;
                                JSONObject objServices = null;
                                arrServices = new JSONArray(services);
                                for (int i = 0; i < arrServices.length(); i++) {
                                    objServices = arrServices.getJSONObject(i);
                                    sql.InsertReferences(objServices.getString("code"), objServices.getString("name"), "S", objServices.getString("price"));
                                    sql.InsertMapping(objServices.getString("code"), objServices.getString("name"), "S");
                                }

                                //Insert Items
                                JSONArray arrItems = null;
                                JSONObject objItems = null;
                                arrItems = new JSONArray(items);
                                for (int i = 0; i < arrItems.length(); i++) {
                                    objItems = arrItems.getJSONObject(i);
                                    sql.InsertReferences(objItems.getString("code"), objItems.getString("name"), "I", objItems.getString("price"));
                                    sql.InsertMapping(objItems.getString("code"), objItems.getString("name"), "I");
                                }

                                runOnUiThread(() -> {
                                    pd.dismiss();
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.installed_updates), Toast.LENGTH_LONG).show();
                                });

                            } else {
                                error_occurred = ob.getString("error_occured");
                                if (error_occurred.equals("true")) {
                                    error_message = ob.getString("error_message");

                                    final String finalError_message = error_message;
                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this, finalError_message, Toast.LENGTH_LONG).show();
                                        ClaimAdminDialogBox();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.SomethingWentWrongServer), Toast.LENGTH_LONG).show();
                                        ClaimAdminDialogBox();
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                pd.dismiss();
                                ClaimAdminDialogBox();
                            });
                            Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, resp[0].getStatusLine().getStatusCode() + "-" + getResources().getString(R.string.SomethingWentWrongServer), Toast.LENGTH_LONG).show();
                            ClaimAdminDialogBox();
                        });
                    }
                }
            };

            thread.start();
        } else {
            runOnUiThread(() -> pd.dismiss());
            ClaimAdminDialogBox();
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    public void DownLoadDiagnosesServicesItemsAgain(final JSONObject object, final SQLHandler sql) throws IOException {

        final String[] content = new String[1];
        final HttpResponse[] resp = {null};
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.refresh_mapping);
            pd = ProgressDialog.show(this, getResources().getString(R.string.Checking_For_Updates), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    String diagnoses = null;
                    String services = null;
                    String items = null;
                    String last_update_date = null;
                    String error_occurred = null;
                    String error_message = null;

                    String functionName = "claim/GetDiagnosesServicesItems";

                    try {
                        HttpResponse response = toRestApi.postToRestApi(object, functionName);
                        resp[0] = response;
                        HttpEntity respEntity = response.getEntity();
                        if (respEntity != null) {
                            // EntityUtils to get the response content

                            content[0] = EntityUtils.toString(respEntity);

                        }

                        JSONObject ob;
                        try {
                            ob = new JSONObject(content[0]);
                            if (String.valueOf(response.getStatusLine().getStatusCode()).equals("200")) {
                                diagnoses = ob.getString("diagnoses");
                                services = ob.getString("services");
                                items = ob.getString("items");
                                last_update_date = ob.getString("update_since_last");
                                saveLastUpdateDate(last_update_date);

                                sql.ClearAll("tblReferences");
                                sql.ClearMapping("S");
                                sql.ClearMapping("I");
                                //Insert Diagnosese
                                JSONArray arrDiagnoses;
                                JSONObject objDiagnoses;
                                arrDiagnoses = new JSONArray(diagnoses);
                                for (int i = 0; i < arrDiagnoses.length(); i++) {
                                    objDiagnoses = arrDiagnoses.getJSONObject(i);
                                    sql.InsertReferences(objDiagnoses.getString("code"), objDiagnoses.getString("name"), "D", "");
                                }

                                //Insert Services
                                JSONArray arrServices;
                                JSONObject objServices;
                                arrServices = new JSONArray(services);
                                for (int i = 0; i < arrServices.length(); i++) {
                                    objServices = arrServices.getJSONObject(i);
                                    sql.InsertReferences(objServices.getString("code"), objServices.getString("name"), "S", objServices.getString("price"));
                                    sql.InsertMapping(objServices.getString("code"), objServices.getString("name"), "S");
                                }

                                //Insert Items
                                JSONArray arrItems;
                                JSONObject objItems;
                                arrItems = new JSONArray(items);
                                for (int i = 0; i < arrItems.length(); i++) {
                                    objItems = arrItems.getJSONObject(i);
                                    sql.InsertReferences(objItems.getString("code"), objItems.getString("name"), "I", objItems.getString("price"));
                                    sql.InsertMapping(objItems.getString("code"), objItems.getString("name"), "I");
                                }

                                runOnUiThread(() -> {
                                    pd.dismiss();
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.installed_updates), Toast.LENGTH_LONG).show();

                                    JSONObject object1 = new JSONObject();
                                    try {
                                        object1.put("claim_administrator_code", global.getOfficerCode());
                                        DownLoadServicesItemsPriceList(object1, sql);
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }

                                });

                            } else {
                                error_occurred = ob.getString("error_occured");
                                if (error_occurred.equals("true")) {
                                    error_message = ob.getString("error_message");

                                    final String finalError_message = error_message;
                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this, finalError_message, Toast.LENGTH_LONG).show();
                                        ClaimAdminDialogBox();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.SomethingWentWrongServer), Toast.LENGTH_LONG).show();
                                        ClaimAdminDialogBox();
                                    });

                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                pd.dismiss();
                                ClaimAdminDialogBox();
                            });
                            Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, resp[0].getStatusLine().getStatusCode() + "-" + getResources().getString(R.string.SomethingWentWrongServer), Toast.LENGTH_LONG).show();
                            ClaimAdminDialogBox();
                        });
                    }
                }
            };

            thread.start();
        } else {
            runOnUiThread(() -> pd.dismiss());
            //ClaimAdminDialogBox();
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    private void DownLoadServicesItemsPriceList(final JSONObject object, final SQLHandler sql) throws IOException {
        final HttpResponse[] resp = {null};
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.Services) + ", " + getResources().getString(R.string.Items) + "...";
            pd = ProgressDialog.show(this, getResources().getString(R.string.mapping), progress_message);
            Thread thread = new Thread() {
                public void run() {

                    String services = null;
                    String items = null;
                    String error_occurred = null;
                    String error_message = null;
                    String last_update_date = null;
                    String content = null;

                    String functionName = "claim/getpaymentlists";
                    try {
                        HttpResponse response = toRestApi.postToRestApiToken(object, functionName);
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
                            if (String.valueOf(code).equals("200")) {
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
                                    sql.InsertMapping(objServices.getString("code"), objServices.getString("name"), "S");
                                }

                                //Insert Items
                                JSONArray arrItems = null;
                                JSONObject objItems = null;
                                arrItems = new JSONArray(items);
                                for (int i = 0; i < arrItems.length(); i++) {
                                    objItems = arrItems.getJSONObject(i);
                                    //sql.InsertReferences(objItems.getString("code").toString(), objItems.getString("name").toString(), "I", objItems.getString("price").toString());
                                    sql.InsertMapping(objItems.getString("code"), objItems.getString("name"), "I");
                                }
                                runOnUiThread(() -> {
                                    pd.dismiss();
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.MapSuccessful), Toast.LENGTH_LONG).show();
                                });
                            } else {
                                error_occurred = ob.getString("error_occured");
                                if (error_occurred.equals("true")) {
                                    if (code >= 400) {
                                        runOnUiThread(() -> {
                                            pd.dismiss();
                                            confirmRefreshMap();
                                        });
                                    } else {
                                        error_message = ob.getString("error_message");
                                        final String finalError_message = error_message;
                                        runOnUiThread(() -> pd.dismiss());
                                        ErrorDialogBox(finalError_message);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> pd.dismiss());
                            Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, resp[0].getStatusLine().getStatusCode() + "-" + getResources().getString(R.string.AccessDenied), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            };
            thread.start();
        } else {
            runOnUiThread(() -> pd.dismiss());
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }

    }

    public void saveLastUpdateDate(String lastUpdateDate) {
        if (global.getSDCardStatus().equals(Environment.MEDIA_MOUNTED)) {
            String dir = global.getSubdirectory("Authentications");
            global.writeText(dir, "last_update_date.txt", lastUpdateDate);
        }
    }
}