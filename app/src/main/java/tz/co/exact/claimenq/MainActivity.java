package tz.co.exact.claimenq;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.general.General;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String Language;
    General _General = new General();
    SQLHandler sql;
    SQLiteDatabase db;

    TextView accepted_count;
    TextView rejected_count;
    TextView pending_count;

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
    final CharSequence[] lang = {"English","Swahili"};

    final static String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";

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

        CreateFolders();

        requestPermision();

/*        requestPermisionWRITE_EXTERNAL_STORAGE();
        requestPermisionACCESS_NETWORK_STATE();
        requestPermisionACCESS_WIFI_STATE();
        requestPermisionCAMERA();
        requestPermisionCHANGE_WIFI_STATE();
        requestPermisionINTERNET();
        requestPermisionVIBRATE();*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        new Thread() {
            public void run() {
                CheckForUpdates();
            }

        }.start();
        Global global = new Global();

        File myDir = new File(Path);
        myDir.mkdir();
        //connection db


        if(checkDataBase()){
            sql = new SQLHandler(this);
            sql.onOpen(db);


            try{
                if(global.getOfficerCode() == null || global.getOfficerCode().equals("")){
                    if(!sql.getAdjustibility("ClaimAdministrator").equals("N")){
                        ClaimAdminDialogBox();
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }




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
                .setTitle("Select Language")
                .setCancelable(false)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setItems(lang,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (lang[which].toString() == "English")Language="en";else Language="fr";
                        //Language="en";
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
            if(global.getUserId() <= 0){
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
            Global global = new Global();
            int userid = global.getUserId();
            if(userid > 0){
                Intent intent = new Intent(this, EnquireActivity.class);
                startActivity(intent);
            }else{
                LoginDialogBox("Enquire");
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
            Intent intent = new Intent(getApplicationContext(), Synchronize.class);
            startActivity(intent);
        } else if (id == R.id.nav_quit) {
            Global global = new Global();
            global.setOfficerCode("");
            finish();
        }else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                    .setPositiveButton("Force close", new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();

        }else if(_General.isSDCardAvailable() == -1){
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardMissing))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), new android.content.DialogInterface.OnClickListener() {

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
                            public void onClick(DialogInterface dialog,int id) {
                                if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){
                                    pd = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                    new Thread() {
                                        public void run() {
                                            CallSoap callSoap = new CallSoap();
                                            callSoap.setFunctionName("isValidLogin");
                                            userid[0] = callSoap.isUserLoggedIn(username.getText().toString(),password.getText().toString());

                                            Global global = new Global();
                                            global.setUserId(userid[0]);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(userid[0] > 0){
                                                        pd.dismiss();
                                                        updateMenuTitlesLogout();
                                                        if(page.equals("MainActivity")){
/*                                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                            startActivity(intent);*/
                                                            Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();
                                                        }
                                                        if(page.equals("Enquire")){
                                                            Intent intent = new Intent(MainActivity.this, EnquireActivity.class);
                                                            startActivity(intent);
                                                            Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();
                                                        }

                                                    }else{
                                                        pd.dismiss();
                                                        //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                        Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                        LoginDialogBox(page);
                                                    }
                                                }
                                            });

                                        }
                                    }.start();


                                }else{
                                    LoginDialogBox(page);
                                    Toast.makeText(MainActivity.this,MainActivity.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
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
                                if(!sql.getAdjustibility("ClaimAdministrator").equals("O")){
                                    if(!claim_code.getText().toString().equals("")){
                                        Global global = new Global();
                                        global.setOfficerCode(claim_code.getText().toString());
                                    }else{
                                        Toast.makeText(getBaseContext(),R.string.MissingClaimCode, Toast.LENGTH_LONG).show();
                                        ClaimAdminDialogBox();
                                    }
                                }

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
    public void ClaimCodeDialogBox(){

        ((MainActivity) this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(getBaseContext());
                View promptsView = li.inflate(R.layout.claim_code_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final TextView claim_code = (TextView) promptsView.findViewById(R.id.ClaimCode);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.Continue,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        if(!claim_code.getText().toString().equals("")){

                                        }else{
                                            Toast.makeText(getBaseContext(),R.string.MissingClaimCode, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

    }
    public AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)

                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
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

                Intent NotifyIntent = new Intent(this, EnquireActivity.class);

                PendingIntent intent = PendingIntent.getActivity(this, 0, NotifyIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setAutoCancel(false);
                builder.setContentTitle(ContentTitle);
                builder.setContentText(ContentText);
                //builder.setSmallIcon(R.drawable.silverware);
                builder.setContentIntent(intent);
                builder.setOngoing(false);
/*				String s = "ring";
				int res_sound_id = context.getResources().getIdentifier(s, "raw", context.getPackageName());
				Uri u = Uri.parse("android.resource://" + context.getPackageName() + "/" + res_sound_id);
				builder.setSound(u);*/

                mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, builder.build());
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
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

}
