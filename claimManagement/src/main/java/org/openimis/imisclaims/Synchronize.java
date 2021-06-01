package org.openimis.imisclaims;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Synchronize extends AppCompatActivity {

    ToRestApi toRestApi;
    Token tokenl;
    boolean isUserLogged;
    Global global;

    TextView tvUploadClaims,tvZipClaims;
    RelativeLayout UploadClaims,zip_claims;

    String FileName;
    File ClaimFile;
    File ClaimFileJSON;
    File[] Claims;
    File[] ClaimsJSON;
    int TotalClaims,UploadCounter,TotalItemService;
    int result;

    ProgressDialog pd;

    private Menu menu;

    private String salt;

    String PendingFolder,TrashFolder;

    Runnable ChangeMessage = new Runnable() {

        @Override
        public void run() {
            //Change progress dialog message here
            //pd.setMessage(UploadCounter + " " + getResources().getString(R.string.Of) + " " + TotalClaims + " " + getResources().getString(R.string.Uploading));
            pd.setMessage(UploadCounter + " File(s) " + getResources().getString(R.string.Uploading));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        global = (Global)getApplication();
        PendingFolder = global.getMainDirectory();
        TrashFolder = global.getSubdirectory("Trash");
        toRestApi = new ToRestApi();
        tokenl = new Token();

        tvUploadClaims = (TextView) findViewById(R.id.tvUploadClaims);
        tvZipClaims = (TextView) findViewById(R.id.tvZipClaims);

        UploadClaims = (RelativeLayout) findViewById(R.id.upload_claims);
        zip_claims = (RelativeLayout) findViewById(R.id.zip_claims);

        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

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

        tvUploadClaims.setText(String.valueOf(total_pending));
        tvZipClaims.setText(String.valueOf(count_pending));


        UploadClaims.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!global.getIslogged()){
                    LoginDialogBox("Synchronize");
                }else{
                    ConfirmUploadClaims();
                }
            }
        });
        zip_claims.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmXMLCreation();

            }
        });
    }

    @Override
    public void onResume() {
        reFreshCount();
        super.onResume();
    }

    //Methods
    public void LoginDialogBox(final String page) {

        final int[] userid = {0};

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
                .setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                    pd = ProgressDialog.show(Synchronize.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                    new Thread() {
                                        public void run() {
                                            Login login = new Login();

                                            isUserLogged = login.LoginToken(username.getText().toString(), password.getText().toString());
                                            global.setIsLogged(isUserLogged);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pd.dismiss();
                                                    if (isUserLogged) {
                                                        if (page.equals("Synchronize")) {
                                                            finish();
                                                            Intent intent = new Intent(Synchronize.this, Synchronize.class);
                                                            startActivity(intent);
                                                            Toast.makeText(Synchronize.this, Synchronize.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        pd.dismiss();
                                                        if (!global.isNetworkAvailable()) {
                                                            LoginDialogBox(page);
                                                            ShowDialog(Synchronize.this.getResources().getString(R.string.CheckInternet));
                                                        } else {
                                                            Toast.makeText(Synchronize.this, Synchronize.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                            LoginDialogBox(page);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }.start();


                                } else {
                                    LoginDialogBox(page);
                                    Toast.makeText(Synchronize.this, Synchronize.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
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

    public AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    public AlertDialog ConfirmXMLCreation() {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.AreYouSure)
                .setCancelable(false)
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        zipFiles();
                    }
                }).show();
    }
    public AlertDialog ConfirmUploadClaims() {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.AreYouSure)
                .setCancelable(false)
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean res1 = UploadAllClaims();
                        //boolean res2 = UploadAllClaimsTrash();
/*                        if(res1 == true || res2 == true){
                            reFreshCount();
*//*                            finish();
                            Intent intent = new Intent(Synchronize.this, Synchronize.class);
                            startActivity(intent);*//*
                        }*/
                    }
                }).show();
    }

  public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public boolean UploadAllClaims(){

        try {
            //Get the total number of files to upload
            Claims = GetListOfJSONFiles(ClaimActivity.Path,"Claim_");
            ClaimsJSON = GetListOfJSONFiles(ClaimActivity.Path, "ClaimJSON");
            TotalClaims = Claims.length;
            int TotalClaimsTrash;
            if(GetListOfFiles(TrashFolder).length > 0){
                TotalClaimsTrash = Integer.parseInt(String.valueOf(GetListOfFiles(TrashFolder).length));
            }else{
                TotalClaimsTrash = 0;
            }

            //If there are no files to upload give the message and exit
            if (TotalClaims == 0 && TotalClaimsTrash == 0){
                ShowDialog(getResources().getString(R.string.NoClaim));
                return false;
            }
            if (TotalClaims == 0){
                return false;
            }

            //If internet is not available then give message and exit
            if (!global.isNetworkAvailable()){
                ShowDialog(getResources().getString(R.string.CheckInternet));
                result = -1;
                return false;
            }

            pd = new ProgressDialog(this);
            pd.setCancelable(false);

            pd = ProgressDialog.show(this,"",getResources().getString(R.string.Uploading));
        }catch (Exception e){
            e.printStackTrace();
        }

        Claims = GetListOfJSONFiles(ClaimActivity.Path,"Claim_");
        ClaimsJSON = GetListOfJSONFiles(ClaimActivity.Path,"ClaimJSON_");
        TotalClaims = Claims.length;

        new Thread(){
            public void run(){
                //Check if valid ftp credentials are available
                //Start Uploading images
                try {
                    UploadAllJSONClaims(Claims, ClaimsJSON);
                }catch (Exception e){
                    e.printStackTrace();
                }


                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        switch(result){
                            case -1:
                                ShowDialog(getResources().getString(R.string.FTPConnectionFailed));
                                break;
                            case -2:
                                ShowDialog(getResources().getString(R.string.SomethingWentWrongServer));
                                break;
                            case -3:
                                ShowDialog(getResources().getString(R.string.FailToUpload));
                                break;
                            default:
                                ShowDialog(getResources().getString(R.string.BulkUpload));
                        }
                    }
                });

                pd.dismiss();
            }

        }.start();
    return true;
    }

    public boolean UploadAllClaimsTrash(){
        final String Path1 = global.getSubdirectory("Trash");
        //Get the total number of files to upload
        Claims = GetListOfJSONFiles(Path1,"Claim_");
        ClaimsJSON = GetListOfJSONFiles(Path1,"ClaimJSON_");
        TotalClaims = Claims.length;
        int TotalClaimsTrash = Integer.parseInt(String.valueOf(GetListOfFiles(TrashFolder).length));

        //If there are no files to upload give the message and exit
        if (TotalClaims == 0){
            //ShowDialog(getResources().getString(R.string.NoClaim));
            return false;
        }

        //If internet is not available then give message and exit
        if (!global.isNetworkAvailable()){
            ShowDialog(getResources().getString(R.string.CheckInternet));
            result = -1;
            return false;
        }

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        pd = ProgressDialog.show(this,"",getResources().getString(R.string.Uploading));

        Claims = GetListOfJSONFiles(Path1,"Claim_");
        ClaimsJSON = GetListOfJSONFiles(Path1,"ClaimJSON_");
        TotalClaims = Claims.length;

        new Thread(){
            public void run(){
                //Check if valid ftp credentials are available
                //Start Uploading images
                UploadAllJSONClaims(Claims, ClaimsJSON);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        switch(result){
                            case -1:
                                ShowDialog(getResources().getString(R.string.FTPConnectionFailed));
                                break;
                            case -2:
                                ShowDialog(getResources().getString(R.string.SomethingWentWrongServer));
                                break;
                            case -3:
                                ShowDialog(getResources().getString(R.string.FailToUpload));
                                break;
                            default:
                                ShowDialog(getResources().getString(R.string.BulkUpload));
                        }
                    }
                });

                pd.dismiss();
            }

        }.start();
        return true;
    }

    private void UploadAllJSONClaims(File[] Claims, File[] ClaimsJSON){

        ToRestApi rest = new ToRestApi();
        int resInt = 2;

        for(int i=0;i<ClaimsJSON.length;i++){
            UploadCounter = i + 1;
            TotalClaims = Claims.length;
            runOnUiThread(ChangeMessage);
            String claim = getClaimText(ClaimsJSON[i].getName());

            try{
                JSONObject jo = new JSONObject(claim);
                JSONObject jobj = jo.getJSONObject("Claim");

                JSONObject datailsObj = jobj.getJSONObject("Details");
                JSONArray itemsArray = jobj.getJSONArray("Items");
                JSONArray servicesArray = jobj.getJSONArray("Services");

                JSONArray itemsArrayRes = new JSONArray();
                for (int k = 0; k < itemsArray.length(); k++){
                    itemsArrayRes.put(itemsArray.getJSONObject(k).getJSONObject("Item"));
                }

                JSONArray servicesArrayRes = new JSONArray();
                for (int k = 0; k < servicesArray.length(); k++){
                    servicesArrayRes.put(servicesArray.getJSONObject(k).getJSONObject("Service"));
                }

                JSONObject obj = new JSONObject();

                obj.put("details", datailsObj);
                obj.put("items", itemsArrayRes);
                obj.put("services", servicesArrayRes);

                resInt = Integer.parseInt(rest.postObjectToRestApiObjectToken(obj,"claim"));
            }catch(Exception e){}

            boolean isSaved = false;

            if(resInt != 2) isSaved = true;

            if(isSaved){
                ClaimFile = Claims[i];
                ClaimFileJSON = ClaimsJSON[i];
                if(resInt == 1){
                    result = 1;
                } else if(resInt == 0){
                    result = 2;
                }else if(resInt == 2){
                    result = -2;
                }else{
                    result = -3;
                }
                MoveFile(ClaimFile);
                MoveFile(ClaimFileJSON);
            }else{
                result = -2;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reFreshCount();
            }
        });

    }

    private File[] GetListOfFiles(String DirectoryPath){
        File Directory = new File(DirectoryPath);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("ClaimJSON_");
            }
        };
        return Directory.listFiles(filter);
    }

    private File[] GetListOfJSONFiles(String DirectoryPath, final String filePrefix){
        File Directory = new File(DirectoryPath);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(filePrefix);
            }
        };
        return Directory.listFiles(filter);
    }
    private void MoveFile(File file){
        switch(result){
            case 1:
                file.renameTo(new File(global.getSubdirectory("AcceptedClaims"), file.getName()));
                break;
            case 2:
                file.renameTo(new File(global.getSubdirectory("RejectedClaims"), file.getName()));
                break;
        }
    }
    public String getClaimText(String fileName){
        String aBuffer = "";
        try {
            String dir = global.getMainDirectory();
            File myFile = new File("/" + dir + "/" + fileName + "");//"/"+dir+"/MasterData.txt"
//            BufferedReader myReader = new BufferedReader(
//                    new InputStreamReader(
//                            new FileInputStream(myFile), "UTF32"));
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();

            myReader.close();
/*            Scanner in = new Scanner(new FileReader("/"+dir+"/MasterData.txt"));
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }
            in.close();
            aBuffer = sb.toString();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }

    public void zipFiles(){
        Global global = (Global) getApplicationContext();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatZip = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        String dzip = formatZip.format(cal.getTime());

        String targetPathClaims = global.getMainDirectory();
        String zipFilePath = global.getSubdirectory("Claims") + "_" + global.getOfficerCode()+"_"+dzip+".rar";
        //String unzippedFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Photos_"+global.getOfficerCode()+"_"+d+"";

        String password = "";
        try{
            Settings settings = new Settings();
            SharedPreferences sharedPreferences = getSharedPreferences("MyPref", 0);
            if (!sharedPreferences.contains("rarPwd")){
                password = global.getDefaultRarPassword();
            }
            else{
                String encryptedRarPassword = sharedPreferences.getString("rarPwd", global.getDefaultRarPassword());
                String trimEncryptedPassword = encryptedRarPassword.trim();
                salt = sharedPreferences.getString("salt", null);
                String trimSalt = salt.trim();
                password = settings.decryptRarPwd(trimEncryptedPassword, trimSalt);
            }
        }
        catch (Exception e){
            e.getMessage();
        }

        ArrayList<File> FilesToAdd = new ArrayList<File>();

        File Claim = new File(targetPathClaims);
        if(Claim.listFiles() != null){
            for(int i = 0; i< Claim.listFiles().length; i++){
                if(Claim.listFiles()[i].isFile()){
                    String fname = Claim.listFiles()[i].getName();
                    String str;
                    try{
                        str = fname.substring(0,6);
                    }catch (StringIndexOutOfBoundsException e){
                        continue;
                    }
                    if(str.equals("Claim_")){
                        FilesToAdd.add(new File(Claim.listFiles()[i].getPath()));
                    }
                }
            }

            Compressor.zip(FilesToAdd, zipFilePath, password);
            // list of files to process to  check move to trash
            File[] listFilesToCheck = Claim.listFiles();
            for(int i = 0; i< listFilesToCheck.length; i++){
                if(listFilesToCheck[i].isFile()){
                    String fname = listFilesToCheck[i].getName();
                    String str;
                    try{
                        str = fname.substring(0,6);
                    }catch (StringIndexOutOfBoundsException e){
                        continue;
                    }
                    if(str.equals("Claim_")){
                        MoveFileToTrash(listFilesToCheck[i]);
                    }
                    if(fname.startsWith("ClaimJSON_")){
                        MoveFileToTrash(listFilesToCheck[i]);
                    }
                }
            }

            if(FilesToAdd.size() == 0){
                ShowDialog(getResources().getString(R.string.NoClaim));
            }else{
                ShowDialog(getResources().getString(R.string.ZipXMLCreated));
            }
            reFreshCount();
        }else{
            ShowDialog(getResources().getString(R.string.NoClaim));
        }

        //Compressor.unzip(zipFilePath, unzippedFolderPath, password);
    }

    private void MoveFileToTrash(File file){
        file.renameTo(new File(global.getSubdirectory("Trash"), file.getName()));
    }

    public void reFreshCount(){
        File pendingFolder = new File(PendingFolder);
        File trashFolder = new File(TrashFolder);

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
        }else{
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
        }else{
            count_trash = 0;
        }


        int total_pending = count_pending;

        tvUploadClaims.setText(String.valueOf(total_pending));
        tvZipClaims.setText(String.valueOf(count_pending));

        tvUploadClaims.setText(String.valueOf(count_pending));
        tvZipClaims.setText(String.valueOf(count_pending));
    }

}
