package org.openimis.imisclaims;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import org.openimis.imisclaims.tools.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MasterDataService extends JobIntentService {
    private static final int JOB_ID = 32094567; //Random unique Job id
    private static final String LOG_TAG = "MDSERVICE";

    private static final String ACTION_DOWNLOAD_MD = "MasterDataService.ACTION_DOWNLOAD_MD";
    private static final String ACTION_IMPORT_MD = "MasterDataService.ACTION_IMPORT_MD";

    public static final String EXTRA_MD_URI = "MasterDataService.EXTRA_MD_URI";

    public static final String ACTION_DOWNLOAD_SUCCESS = "MasterDataService.ACTION_DOWNLOAD_SUCCESS";
    public static final String ACTION_DOWNLOAD_ERROR = "MasterDataService.ACTION_DOWNLOAD_ERROR";
    public static final String ACTION_IMPORT_SUCCESS = "MasterDataService.ACTION_IMPORT_SUCCESS";
    public static final String ACTION_IMPORT_ERROR = "MasterDataService.ACTION_IMPORT_ERROR";

    public static final String EXTRA_ERROR_MESSAGE = "MasterDataService.EXTRA_ERROR_MESSAGE";

    Global global;
    ToRestApi toRestApi;
    String lastAction;

    @Override
    public void onCreate() {
        super.onCreate();
        global = (Global) getApplicationContext();
        toRestApi = new ToRestApi();
    }

    public static void downloadMasterData(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DOWNLOAD_MD);
        enqueueWork(context, MasterDataService.class, JOB_ID, intent);
    }

    public static void importMasterData(Context context, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(ACTION_IMPORT_MD);
        intent.putExtra(EXTRA_MD_URI, uri.toString());
        enqueueWork(context, MasterDataService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        lastAction = intent.getAction();
        if (ACTION_DOWNLOAD_MD.equals(lastAction)) {
            handleDownloadMasterData();
        } else if (ACTION_IMPORT_MD.equals(lastAction)) {
            Uri uri = Uri.parse(intent.getStringExtra(EXTRA_MD_URI));
            handleImportMasterData(uri);
        }

    }

    private void handleImportMasterData(Uri uri) {
        try {
            File tempDatabaseFile = new File(global.getSubdirectory("Databases"), "temp.db3");

            if (!copyContent(uri, tempDatabaseFile)) {
                broadcastError(ACTION_IMPORT_ERROR, getResources().getString(R.string.importMasterDataFailed));
                tempDatabaseFile.delete();
                return;
            }

            if (!isDatabaseValid(tempDatabaseFile)) {
                broadcastError(ACTION_IMPORT_ERROR, getResources().getString(R.string.importMasterDataFailed));
                tempDatabaseFile.delete();
                return;
            }

            if (!applyDatabase(tempDatabaseFile)) {
                broadcastError(ACTION_IMPORT_ERROR, getResources().getString(R.string.importMasterDataFailed));
                tempDatabaseFile.delete();
                return;
            }

            broadcastSuccess(ACTION_IMPORT_SUCCESS);
        } catch (SQLiteException e) {
            e.printStackTrace();
            broadcastError(ACTION_IMPORT_ERROR, getResources().getString(R.string.importMasterDataFailed));
        }
    }

    private void handleDownloadMasterData() {
        //TODO not yet implemented
    }

    private void broadcastSuccess(String action) {
        Intent successIntent = new Intent(action);
        sendBroadcast(successIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s", lastAction, action));
    }

    private void broadcastError(String action, String errorMessage) {
        Intent errorIntent = new Intent(action);
        errorIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
        Log.i(LOG_TAG, String.format("%s finished with %s, error message: %s", lastAction, action, errorMessage));
    }

    private boolean isDatabaseValid(File databaseFile) {
        boolean isValid;
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, 0)) {
            isValid = tableExists(db, "tblControls") &&
                    tableExists(db, "tblClaimAdmins") &&
                    tableExists(db, "tblReferences");
        } catch (SQLiteException e) {
            e.printStackTrace();
            isValid = false;
        }
        new File(databaseFile.getAbsolutePath() + "-journal").delete();
        return isValid;
    }

    private boolean tableExists(SQLiteDatabase db, String table) {
        Cursor c = db.query("sqlite_master", new String[]{"tbl_name "}, "tbl_name = ?", new String[]{table}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    private boolean copyContent(Uri uri, File file) {
        boolean databaseCopied = false;
        try {
            if ((!file.exists() || file.delete()) && file.createNewFile()) {
                try (ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r")) {
                    long size = fileDescriptor.getStatSize();

                    if (size > 0 && size < Integer.MAX_VALUE) {
                        byte[] buff = new byte[(int) size];
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        if (inputStream.read(buff) == size) {
                            new FileOutputStream(file).write(buff);
                            databaseCopied = true;
                        }
                        inputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return databaseCopied;
    }

    private boolean applyDatabase(File database) {
        File imisDatabase = new File(SQLHandler.DB_NAME_DATA);
        if (!imisDatabase.exists() || imisDatabase.delete()) {
            return database.renameTo(imisDatabase);
        }
        return false;
    }
}
