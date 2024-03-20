package org.openimis.imisclaims;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.apache.commons.io.IOUtils;
import org.openimis.imisclaims.claimlisting.ClaimListingActivity;
import org.openimis.imisclaims.domain.entity.ClaimAdmin;
import org.openimis.imisclaims.domain.entity.Control;
import org.openimis.imisclaims.domain.entity.DiagnosesServicesMedications;
import org.openimis.imisclaims.domain.entity.Diagnosis;
import org.openimis.imisclaims.domain.entity.Medication;
import org.openimis.imisclaims.domain.entity.PaymentList;
import org.openimis.imisclaims.domain.entity.Service;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.usecase.FetchClaimAdmins;
import org.openimis.imisclaims.usecase.FetchControls;
import org.openimis.imisclaims.usecase.FetchDiagnosesServicesItems;
import org.openimis.imisclaims.usecase.FetchPaymentList;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ImisActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private static final int REQUEST_ALL_FILES_ACCESS_CODE = 2;
    private static final String LOG_TAG = "MainActivity";
    ArrayList<String> broadcastList;
    final CharSequence[] lang = {"English", "Francais"};
    String Language;

    TextView accepted_count;
    TextView rejected_count;
    TextView entered_Count;
    TextView AdminName;
    DrawerLayout drawer;
    TextView loginText;

    Menu menu;
    static String Path;

    final String VersionField = "AppVersionEnquire";
    NotificationManager mNotificationManager;
    final int SIMPLE_NOTIFICATION_ID = 1;
    private static final int REQUEST_PICK_MD_FILE = 3;
    Vibrator vibrator;

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        String action = intent.getAction();
        if (SynchronizeService.ACTION_CLAIM_COUNT_RESULT.equals(action)) {
            accepted_count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ACCEPTED, 0)));
            rejected_count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_REJECTED, 0)));
            entered_Count.setText(String.valueOf(intent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ENTERED, 0)));
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

        isSDCardAvailable();

        broadcastList = new ArrayList<>();
        broadcastList.add(SynchronizeService.ACTION_CLAIM_COUNT_RESULT);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

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

        View header = navigationView.getHeaderView(0);
        loginText = header.findViewById(R.id.LoginText);
        loginText.setText(global.isLoggedIn() ? R.string.Logout : R.string.Login);
        RelativeLayout loginButton = header.findViewById(R.id.LoginButton);
        loginButton.setOnClickListener((view) -> changeLoginState());

        accepted_count = findViewById(R.id.accepted_count);
        rejected_count = findViewById(R.id.rejected_count);
        entered_Count = findViewById(R.id.entered_count);

        accepted_count.setText("0");
        rejected_count.setText("0");
        entered_Count.setText("0");

        AdminName = findViewById(R.id.AdminName);

        if (checkRequirements()) {
            onAllRequirementsMet();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCount();
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
                        if (lang[which].toString().equals("English")) Language = "en";
                        else Language = "fr";
                        global.setSavedLanguage(Language);
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
            doLoggedIn(() -> startActivity(new Intent(this, EnquireActivity.class)));
        } else if (id == R.id.nav_Map_Items) {
            Intent intent = new Intent(this, MapItems.class);
            startActivity(intent);
        } else if (id == R.id.nav_Map_Services) {
            Intent intent = new Intent(this, MapServices.class);
            startActivity(intent);
        } else if (id == R.id.nav_Refresh_Map) {
            doLoggedIn(this::confirmRefreshMap);
        } else if (id == R.id.nav_claim) {
            //Intent intent = new Intent(this, ClaimActivity.class);
            Intent intent = new Intent(this, ClaimListingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Reports) {
            Intent intent = new Intent(getApplicationContext(), Report.class);
            startActivity(intent);
        } else if (id == R.id.nav_Sync) {
            startActivity(new Intent(getApplicationContext(), SynchronizeActivity.class));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (checkRequirements()) {
                onAllRequirementsMet();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        File databaseFile;

        if (requestCode == REQUEST_ALL_FILES_ACCESS_CODE) {
            if (checkRequirements()) {
                onAllRequirementsMet();
            }
        } else if (requestCode == REQUEST_PICK_MD_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        byte[] bytes = IOUtils.toByteArray(getContentResolver().openInputStream(uri));
                        databaseFile = new File(SQLHandler.DB_NAME_DATA);
                        if (databaseFile.exists() || databaseFile.createNewFile()) {
                            new FileOutputStream(databaseFile).write(bytes);
                            onAllRequirementsMet();
                        } else {
                            showDialog(getResources().getString(R.string.ImportMasterDataFailed),
                                    (d, i) -> finish());
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error while copying master data.", e);
                    }
                }
            }
        }
    }

    public void PickMasterDataFileDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getResources().getString(R.string.NoInternetTitle))
                .setMessage(getResources().getString(R.string.DoImportClaimsMasterData))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Yes),
                        (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            try {
                                startActivityForResult(intent, REQUEST_PICK_MD_FILE);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFileExporerInstalled), Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(getResources().getString(R.string.No),
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        }).show();
    }

    public AlertDialog confirmRefreshMap() {
        return showDialog(
                getResources().getString(R.string.AreYouSure),
                (dialog, i) -> {
                    try {
                        doLoggedIn(() -> DownLoadDiagnosesServicesItems(global.getOfficerCode()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                (dialog, i) -> dialog.cancel());
    }

    public void changeLoginState() {
        if (global.isLoggedIn()) {
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.Logout), getResources().getString(R.string.InProgress));
            loginText.setText(R.string.Login);
            runOnNewThread(() -> global.getLoginRepository().saveToken(null, null),
                    () -> progressDialog.dismiss(),
                    500);
        } else {
            doLoggedIn(() -> loginText.setText(R.string.Logout));
        }
    }

    private void initializeDb3File(SQLHandler sql) {
        if (checkDataBase()) {
            if (global.isNetworkAvailable()) {
                getControls();
            } else {
                if (!sql.checkIfAny("tblControls")) {
                    CriticalErrorDialogBox(getResources().getString(R.string.noControls) + " " + getResources().getString(R.string.provideExtractOrInternet));
                } else if (!sql.checkIfAny("tblClaimAdmins")) {
                    if (sql.getAdjustability("ClaimAdministrator").equals("M"))
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
                        (dialog, id) -> validateClaimAdminCode(claim_code.getText().toString().trim()))
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
                                if (!sqlHandler.getAdjustability("ClaimAdministrator").equals("N")) {
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

                PendingIntent intent = PendingIntent.getActivity(this, 0, NotifyIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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
        if (sqlHandler.checkTableExists("tblClaimDetails"))
            SynchronizeService.getClaimCount(this);
    }

    public boolean checkDataBase() {
        return sqlHandler.checkDatabase();
    }

    public boolean getControls() {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.getControls);
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        List<Control> controls = new FetchControls().execute();
                        for (Control control : controls) {
                            sqlHandler.InsertControls(control.getName(), control.getAdjustability());
                        }

                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            doLoggedIn(MainActivity.this::getClaimAdmins);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            ErrorDialogBox(e.getMessage());
                        });
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

    public void getClaimAdmins() {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.application);
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.initializing), progress_message);
            Thread thread = new Thread(() -> {
                try {
                    List<ClaimAdmin> claimAdmins = new FetchClaimAdmins().execute();
                    sqlHandler.ClearAll("tblClaimAdmins");
                    for (ClaimAdmin claimAdmin : claimAdmins) {
                        sqlHandler.InsertClaimAdmins(
                                claimAdmin.getClaimAdminCode(),
                                claimAdmin.getHealthFacilityCode(),
                                claimAdmin.getDisplayName()
                        );
                    }

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showToast(R.string.initializing_complete);
                        if (checkRequirements()) {
                            onAllRequirementsMet();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> progressDialog.dismiss());
                }
            });
            thread.start();
        } else {
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    public void validateClaimAdminCode(final String claimAdminCode) {
        if (claimAdminCode.equals("")) {
            Toast.makeText(getBaseContext(), R.string.MissingClaimAdmin, Toast.LENGTH_LONG).show();
            ClaimAdminDialogBox();
        } else {
            String ClaimName = sqlHandler.getClaimAdminInfo(claimAdminCode, SQLHandler.CA_NAME_COLUMN);
            String HealthFacilityName = sqlHandler.getClaimAdminInfo(claimAdminCode, SQLHandler.CA_HF_CODE_COLUMN);
            if (ClaimName.equals("")) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.invalidClaimAdminCode), Toast.LENGTH_LONG).show();
                ClaimAdminDialogBox();
            } else {
                if (!sqlHandler.getAdjustability("ClaimAdministrator").equals("N")) {
                    global.setOfficerCode(claimAdminCode);
                    global.setOfficerName(ClaimName);
                    global.setOfficerHealthFacility(HealthFacilityName);
                    AdminName = findViewById(R.id.AdminName);
                    AdminName.setText(global.getOfficeName());
                }
                Cursor c = sqlHandler.getMapping("I");
                if (c != null) {
                    if (c.getCount() == 0) {
                        try {
                            progressDialog.dismiss();
                            doLoggedIn(() -> DownLoadDiagnosesServicesItems(null));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    c.close();
                }
            }
        }
    }

    public void DownLoadDiagnosesServicesItems(@Nullable final String officerCode) {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.Diagnoses) + ", " + getResources().getString(R.string.Services) + ", " + getResources().getString(R.string.Items) + "...";
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.Checking_For_Updates), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        DiagnosesServicesMedications diagnosesServicesMedications = new FetchDiagnosesServicesItems().execute();
                        saveLastUpdateDate(diagnosesServicesMedications.getLastUpdated());
                        sqlHandler.ClearAll("tblReferences");
                        sqlHandler.ClearMapping("S");
                        sqlHandler.ClearMapping("I");
                        //Insert Diagnoses
                        for (Diagnosis diagnosis : diagnosesServicesMedications.getDiagnoses()) {
                            sqlHandler.InsertReferences(diagnosis.getCode(), diagnosis.getName(), "D", "");
                        }

                        //Insert Services
                        for (Service service : diagnosesServicesMedications.getServices()) {
                            sqlHandler.InsertReferences(service.getCode(), service.getName(), "S", String.valueOf(service.getPrice()));
                            sqlHandler.InsertMapping(service.getCode(), service.getName(), "S");
                        }

                        //Insert Items
                        for (Medication medication : diagnosesServicesMedications.getMedications()) {
                            sqlHandler.InsertReferences(medication.getCode(), medication.getName(), "I", String.valueOf(medication.getPrice()));
                            sqlHandler.InsertMapping(medication.getCode(), medication.getName(), "I");
                        }

                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.installed_updates), Toast.LENGTH_LONG).show();
                            if (officerCode != null) {
                                DownLoadServicesItemsPriceList(officerCode);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage() + "-" + getResources().getString(R.string.SomethingWentWrongServer), Toast.LENGTH_LONG).show();
                            ClaimAdminDialogBox();
                        });
                    }
                }
            };
            thread.start();
        } else {
            runOnUiThread(() -> progressDialog.dismiss());
            if (officerCode == null) {
                ClaimAdminDialogBox();
            }
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }
    }

    private void DownLoadServicesItemsPriceList(@NonNull final String claimAdministratorCode) {
        if (global.isNetworkAvailable()) {
            String progress_message = getResources().getString(R.string.Services) + ", " + getResources().getString(R.string.Items) + "...";
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.mapping), progress_message);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        PaymentList paymentList = new FetchPaymentList().execute(claimAdministratorCode);
                        sqlHandler.ClearMapping("S");
                        sqlHandler.ClearMapping("I");

                        //Insert Services
                        for (Service service : paymentList.getServices()) {
                            sqlHandler.InsertMapping(service.getCode(), service.getName(), "S");
                        }

                        //Insert Items
                        for (Medication medication : paymentList.getMedications()) {
                            sqlHandler.InsertMapping(medication.getCode(), medication.getName(), "I");
                        }
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.MapSuccessful), Toast.LENGTH_LONG).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage() + "-" + getResources().getString(R.string.AccessDenied), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            };
            thread.start();
        } else {
            runOnUiThread(() -> progressDialog.dismiss());
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }

    }

    public void saveLastUpdateDate(String lastUpdateDate) {
        if (global.getSDCardStatus().equals(Environment.MEDIA_MOUNTED)) {
            String dir = global.getSubdirectory("Authentications");
            global.writeText(dir, "last_update_date.txt", lastUpdateDate);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                        && !permission.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                    // MANAGE_EXTERNAL_STORAGE always report as denied by design
                    return false;
                }
            }
        }
        return true;
    }

    public void permissionsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.Permissions)
                .setMessage(getResources().getString(R.string.PermissionsInfo, getResources().getString(R.string.app_name_claims)))
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        (dialog, id) -> ActivityCompat.requestPermissions(this, global.getPermissions(), REQUEST_PERMISSIONS_CODE))
                .setNegativeButton(R.string.ForceClose,
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        });

        alertDialogBuilder.show();
    }

    public boolean checkRequirements() {
        if (!hasPermissions(this, global.getPermissions())) {
            permissionsDialog();
            return false;
        }

        boolean isAppInitialized = sqlHandler.checkIfAny("tblControls")
                && (sqlHandler.getAdjustability("ClaimAdministrator").equals("N") || sqlHandler.checkIfAny("tblClaimAdmins"));
        if (!isAppInitialized) {
            if (global.isNetworkAvailable()) {
                sqlHandler.createOrOpenDatabases();
                sqlHandler.createTables();
                sqlHandler.createMappingTables();
                initializeDb3File(sqlHandler);
            } else {
                sqlHandler.createMappingTables();
                PickMasterDataFileDialog();
                showToast(R.string.CheckInternet);
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onUserLoggedIn() {
        loginText.setText(global.isLoggedIn() ? R.string.Logout : R.string.Login);
    }

    public void onAllRequirementsMet() {
        if (!sqlHandler.getAdjustability("ClaimAdministrator").equals("N")) {
            ClaimAdminDialogBox();
        }
        refreshCount();
    }
}
