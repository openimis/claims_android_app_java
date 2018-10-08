package tz.co.exact.claimenq;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.UpdateLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.general.General;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static tz.co.exact.claimenq.ClaimActivity.Path;

public class Synchronize extends AppCompatActivity {

    TextView tvUploadClaims,tvZipClaims;
    RelativeLayout UploadClaims,zip_claims;

    String FileName;
    File ClaimFile;
    File ClaimFileJSON;
    File[] Claims;
    File[] ClaimsJSON;
    int TotalClaims,UploadCounter,TotalItemService;
    int result;
    General _General = new General();

    ProgressDialog pd;

    private Menu menu;

    String PendingFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
    String TrashFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Trash";

    Runnable ChangeMessage = new Runnable() {

        @Override
        public void run() {
            //Change progress dialog message here
            pd.setMessage(UploadCounter + " " + getResources().getString(R.string.Of) + " " + TotalClaims + " " + getResources().getString(R.string.Uploading));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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
                Global global = new Global();
                if(global.getUserId() <= 0){
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

    //Methods
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
                .setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){
                                    pd = ProgressDialog.show(Synchronize.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

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
                                                    pd.dismiss();
                                                    if(userid[0] > 0){
                                                        if(page.equals("Synchronize")){
                                                            finish();
                                                            Intent intent = new Intent(Synchronize.this, Synchronize.class);
                                                            startActivity(intent);
                                                            Toast.makeText(Synchronize.this,Synchronize.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();
                                                        }

                                                    }else{
                                                        pd.dismiss();
                                                        Toast.makeText(Synchronize.this,Synchronize.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                        //ShowDialog(Synchronize.this.getResources().getString(R.string.LoginFail));
                                                        LoginDialogBox(page);
                                                    }
                                                }
                                            });

                                        }
                                    }.start();


                                }else{
                                    LoginDialogBox(page);
                                    Toast.makeText(Synchronize.this,Synchronize.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
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
                        boolean res2 = UploadAllClaimsTrash();
                        if(res1 == true || res2 == true){
                            finish();
                            Intent intent = new Intent(Synchronize.this, Synchronize.class);
                            startActivity(intent);
                        }
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
        //Get the total number of files to upload
        Claims = GetListOfFiles(Path);
        ClaimsJSON = GetListOfJSONFiles(Path);
        TotalClaims = Claims.length;
        int TotalClaimsTrash;
        if(GetListOfFiles(TrashFolder) != null){
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
        if (!_General.isNetworkAvailable(this)){
            ShowDialog(getResources().getString(R.string.CheckInternet));
            result = -1;
            return false;
        }

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        pd = ProgressDialog.show(this,"",getResources().getString(R.string.Uploading));

        new Thread(){
            public void run(){
                //Check if valid ftp credentials are available
                //Start Uploading images
                UploadAllJSONClaims();

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
        final String Path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Trash";
        //Get the total number of files to upload
        Claims = GetListOfFiles(Path1);
        ClaimsJSON = GetListOfJSONFiles(Path);
        TotalClaims = Claims.length;
        int TotalClaimsTrash = Integer.parseInt(String.valueOf(GetListOfFiles(TrashFolder).length));

        //If there are no files to upload give the message and exit
        if (TotalClaims == 0){
            ShowDialog(getResources().getString(R.string.NoClaim));
            return false;
        }

        //If internet is not available then give message and exit
        if (!_General.isNetworkAvailable(this)){
            ShowDialog(getResources().getString(R.string.CheckInternet));
            result = -1;
            return false;
        }

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        pd = ProgressDialog.show(this,"",getResources().getString(R.string.Uploading));

        new Thread(){
            public void run(){
                //Check if valid ftp credentials are available
                //Start Uploading images
                UploadAllJSONClaims();

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

    private void UploadAllJSONClaims(){
        CallSoap cs = new CallSoap();
        cs.setFunctionName("UploadClaim");
        for(int i=0;i<ClaimsJSON.length;i++){
            UploadCounter = i + 1;
            runOnUiThread(ChangeMessage);
            String claim = getClaimText(ClaimsJSON[i].getName());
            if(cs.UploadClaim(claim,Claims[i].getName())){
                ClaimFile = Claims[i];
                ClaimFileJSON = ClaimsJSON[i];
                int res = ServerResponse();
                if(res == 1){
                    result = 1;
                } else if(res == 0){
                    result = 2;
                }else if(res == 2){
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
    }
    private File[] GetListOfFiles(String DirectoryPath){
        File Directory = new File(DirectoryPath);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("Claim_");
            }
        };
        return Directory.listFiles(filter);
    }
    private File[] GetListOfJSONFiles(String DirectoryPath){
        File Directory = new File(DirectoryPath);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("ClaimJSON_");
            }
        };
        return Directory.listFiles(filter);
    }
    private void MoveFile(File file){
        switch(result){
            case 1:
                file.renameTo(new File(Path + "AcceptedClaims/" + file.getName()));
                break;
            case 2:
                file.renameTo(new File(Path + "RejectedClaims/" + file.getName()));
                break;
        }
    }
    public String getClaimText(String fileName){
        String aBuffer = "";
        try {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/";
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
    private int ServerResponse(){
        CallSoap cs = new CallSoap();
        cs.setFunctionName("isValidClaim");
        return cs.isClaimAccepted(ClaimFile.getName().toString());
    }

    public void zipFiles(){
        Global global = new Global();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatZip = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        String dzip = formatZip.format(cal.getTime());

        String targetPathClaims = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
        String zipFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Claims_"+global.getOfficerCode()+"_"+dzip+".rar";
        //String unzippedFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/Photos_"+global.getOfficerCode()+"_"+d+"";
        String password = ")(#$1HsD"; // keep it EMPTY<""> for applying no password protection

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
                        MoveFileToTrash(Claim.listFiles()[i]);
                    }
                }
            }

            ShowDialog(getResources().getString(R.string.ZipXMLCreated));
            reFreshCount();
        }else{
            ShowDialog(getResources().getString(R.string.NoClaim));
        }

        //Compressor.unzip(zipFilePath, unzippedFolderPath, password);
    }

    private void MoveFileToTrash(File file){
        file.renameTo(new File(Path + "Trash/" + file.getName()));
    }

    public void reFreshCount(){
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
        }else{
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
        }else{
            count_trash = 0;
        }


        int total_pending = count_trash + count_pending;

        tvUploadClaims.setText(String.valueOf(total_pending));
        tvZipClaims.setText(String.valueOf(count_pending));

        tvUploadClaims.setText(String.valueOf(count_pending));
        tvZipClaims.setText(String.valueOf(count_pending));
    }

}
