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

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.openimis.imisclaims.BuildConfig.RAR_PASSWORD;
import static org.openimis.imisclaims.BuildConfig.APP_DIR;

public class Global extends Application {
    private static final String SHPREF_NAME = "SHPref";
    private static final String SHPREF_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static Global instance;
    private String OfficerCode;
    private String OfficerName;
    private int UserId;
    private String MainDirectory;
    private String AppDirectory;
    private final Map<String, String> SubDirectories = new HashMap<>();
    private static final String _DefaultRarPassword = RAR_PASSWORD;
    private Token JWTToken;
    private String[] permissions;

    private final List<String> ProtectedDirectories = Arrays.asList("Authentications", "Databases");

    public Global() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE};
        }

    }

    public static Global getGlobal() {
        return instance;
    }

    public Context getContext() {
        return instance.getApplicationContext();
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

    public void setOfficerName(String officerName) {
        OfficerName = officerName;
    }

    public String getOfficeName() {
        return this.OfficerName;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public Token getJWTToken() {
        if (JWTToken == null)
            JWTToken = new Token();
        return JWTToken;
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = getJWTToken().isTokenValidJWT();
        if (!isLoggedIn) {
            getJWTToken().clearToken();
        }
        return isLoggedIn;
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
        if (MainDirectory == null || "".equals(MainDirectory)) {
            String documentsDir = createOrCheckDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString());
            MainDirectory = createOrCheckDirectory(documentsDir + File.separator + APP_DIR);

            if ("".equals(documentsDir) || "".equals(MainDirectory)) {
                Log.w("DIRS", "Main directory could not be created");
            }
        }
        return MainDirectory;
    }

    public String getAppDirectory() {
        if (AppDirectory == null || "".equals(AppDirectory)) {
            AppDirectory = createOrCheckDirectory(getApplicationInfo().dataDir);

            if ("".equals(AppDirectory)) {
                Log.w("DIRS", "App directory could not be created");
            }
        }
        return AppDirectory;
    }

    public String getSubdirectory(String subdirectory) {
        if (!SubDirectories.containsKey(subdirectory) || "".equals(SubDirectories.get(subdirectory))) {
            String directory;

            if (ProtectedDirectories.contains(subdirectory)) {
                directory = getAppDirectory();
            } else {
                directory = getMainDirectory();
            }

            String subDirPath = createOrCheckDirectory(directory + File.separator + subdirectory);

            if ("".equals(subDirPath)) {
                Log.w("DIRS", subdirectory + " directory could not be created");
                return "";
            } else {
                SubDirectories.put(subdirectory, subDirPath);
            }
        }
        return SubDirectories.get(subdirectory);
    }

    public String getFileText(File file) {
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                line = reader.readLine();
                if (line != null) {
                    stringBuilder.append(line);

                    line = reader.readLine();
                    while (line != null) {
                        stringBuilder.append("\n");
                        stringBuilder.append(line);
                        line = reader.readLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public String getFileText(String dir, String filename) {
        return getFileText(new File(dir, filename));
    }

    public void writeText(File file, String text) {
        try {
            if (file.exists()) {
                file.delete();
            }
            if (file.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(text);
                myOutWriter.close();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeText(String dir, String filename, String text) {
        writeText(new File(new File(dir), filename), text);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return (ni != null && ni.isConnected());
    }

    public void setLanguage(Context c, String Language) {
        Resources res = c.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration config = res.getConfiguration();
        config.locale = new Locale(Language.toLowerCase());
        res.updateConfiguration(config, dm);
    }

    public boolean isNewVersionAvailable(String Field, String PackageName) {
        return false;
    }

    public String getSDCardStatus() {
        return Environment.getExternalStorageState();
    }

    public String getRarPwd() {
        String password = "";
        SharedPreferences sharedPreferences = getDefaultSharedPreferences();
        if (!sharedPreferences.contains("rarPwd")) {
            password = getDefaultRarPassword();
        } else {
            String encryptedRarPassword = sharedPreferences.getString("rarPwd", getDefaultRarPassword());
            String trimEncryptedPassword = encryptedRarPassword.trim();
            String salt = sharedPreferences.getString("salt", null);
            String trimSalt = salt.trim();
            try {
                password = decryptRarPwd(trimEncryptedPassword, trimSalt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return password;
    }

    private String decryptRarPwd(String dataToDecrypt, String decPassword) throws Exception {
        SecretKeySpec key = generateKey(decPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(dataToDecrypt, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }

    private SecretKeySpec generateKey(String encPassword) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = encPassword.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

    public SharedPreferences getDefaultSharedPreferences() {
        return this.getSharedPreferences(SHPREF_NAME, MODE_PRIVATE);
    }

    public String getSavedLanguage() {
        SharedPreferences sp = getDefaultSharedPreferences();
        return sp.getString(SHPREF_LANGUAGE, DEFAULT_LANGUAGE_CODE);
    }

    public void setSavedLanguage(String languageCode) {
        SharedPreferences sp = getDefaultSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SHPREF_LANGUAGE, languageCode);
        editor.apply();
    }
}
