//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imisclaims;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.openimis.imisclaims.BuildConfig.API_BASE_URL;
import static org.openimis.imisclaims.BuildConfig.RAR_PASSWORD;
import static org.openimis.imisclaims.BuildConfig.APP_DIR;

/**
 * Created by HP on 05/16/2017.
 */

public class Global extends Application {
    private static Global instance;
    private String OfficerCode;
    private String OfficerName;
    private int UserId;
    private int OfficerId;
    private String token;
    private boolean isLogged;
    private String MainDirectory;
    private Map<String, String> SubDirectories = new HashMap<>();
    private String ImageFolder;
    private static final String _Domain = API_BASE_URL;
    private static final String _DefaultRarPassword = RAR_PASSWORD;

    public Global() { instance = this; }
    public static Context getContext() { return instance; }
    public String getDomain(){
        return _Domain;
    }
    public String getDefaultRarPassword() {
        return _DefaultRarPassword;
    }
    public String getOfficerCode() {
        return OfficerCode;
    }
    public void setOfficerCode(String officerCode) {
        OfficerCode = officerCode;
    }
    public int getUserId() {
        return UserId;
    }
    public void setUserId(int userId) {
        UserId = userId;
    }
    public boolean getIslogged() {
        return isLogged;
    }
    public void setIsLogged(boolean logged) {
        isLogged = logged;
    }
    public void setOfficerName(String officerName) {
        OfficerName = officerName;
    }
    public String getOfficeName(){
        return this.OfficerName;
    }

    private String createOrCheckDirectory(String path) {
        File dir = new File(path);

        if (dir.exists() || dir.mkdir()) {
            return path;
        } else {
            return "";
        }
    }

    public String getMainDirectory() {
        if (MainDirectory == null) {
            String documentsDir = createOrCheckDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString());
            MainDirectory = createOrCheckDirectory(documentsDir + File.separator + APP_DIR);

            if ("".equals(documentsDir) || "".equals(MainDirectory)) {
                Log.w("DIRS", "Main directory could not be created");
            }
        }
        return MainDirectory;
    }

    public String getSubdirectory(String subdirectory) {
        if (!SubDirectories.containsKey(subdirectory)) {
            String subDir = createOrCheckDirectory(getMainDirectory() + File.separator + subdirectory);

            if ("".equals(subDir)) {
                Log.w("DIRS", subdirectory + " directory could not be created");
                return null;
            } else {
                SubDirectories.put(subdirectory, subDir);
            }
        }
        return SubDirectories.get(subdirectory);
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return (ni != null && ni.isConnected());
    }

    public void ChangeLanguage(String Language){
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration config = res.getConfiguration();
        config.locale = new Locale(Language.toLowerCase());
        res.updateConfiguration(config, dm);
    }

    // ToDo: remove this method if published to Google Play
    public boolean isNewVersionAvailable(String Field, String PackageName){
        return false;
    }

    public String getSDCardStatus(){
        return Environment.getExternalStorageState();
    }
}
