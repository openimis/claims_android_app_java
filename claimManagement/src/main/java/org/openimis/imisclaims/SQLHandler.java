package org.openimis.imisclaims;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;

import java.util.Locale;

public class SQLHandler extends SQLiteOpenHelper {
    private static final String LOG_TAG = "SQLHELPER";

    public static final String CA_NAME_COLUMN = "Name";
    public static final String CA_HF_CODE_COLUMN = "HFCode";

    public static final String CLAIM_UPLOAD_STATUS_ACCEPTED = "Accepted";
    public static final String CLAIM_UPLOAD_STATUS_REJECTED = "Rejected";
    public static final String CLAIM_UPLOAD_STATUS_ERROR = "Error";
    public static final String CLAIM_UPLOAD_STATUS_EXPORTED = "Exported";
    public static final String CLAIM_UPLOAD_STATUS_ENTERED = "Entered";
    public static final String CLAIM_UPLOAD_STATUS_ARCHIVED = "Archived";

    public static final String DB_NAME_MAPPING = Global.getGlobal().getSubdirectory("Databases") + "/" + "Mapping.db3";
    public static final String DB_NAME_DATA = Global.getGlobal().getSubdirectory("Databases") + "/" + "ImisData.db3";

    private static final String CreateTableMapping = "CREATE TABLE IF NOT EXISTS tblMapping(Code TEXT,Name TEXT,Type TEXT);";
    private static final String createTablePolicyInquiry = "CREATE TABLE IF NOT EXISTS tblPolicyInquiry(InsureeNumber text,Photo BLOB, InsureeName Text, DOB Text, Gender Text, ProductCode Text, ProductName Text, ExpiryDate Text, Status Text, DedType Int, Ded1 Int, Ded2 Int, Ceiling1 Int, Ceiling2 Int);";
    private static final String CreateTableControls = "CREATE TABLE IF NOT EXISTS tblControls(FieldName TEXT, Adjustability TEXT);";
    private static final String CreateTableClaimAdmins = "CREATE TABLE IF NOT EXISTS tblClaimAdmins(Code TEXT, HFCode TEXT ,Name TEXT);";
    private static final String CreateTableReferences = "CREATE TABLE IF NOT EXISTS tblReferences(Code TEXT, Name TEXT, Type TEXT, Price TEXT);";
    private static final String createTableClaimDetails = "CREATE TABLE IF NOT EXISTS tblClaimDetails(ClaimUUID TEXT, ClaimDate TEXT, HFCode TEXT, ClaimAdmin TEXT, ClaimCode TEXT, GuaranteeNumber TEXT, InsureeNumber TEXT, StartDate TEXT, EndDate TEXT, ICDCode TEXT, Comment TEXT, Total TEXT, ICDCode1 TEXT, ICDCode2 TEXT, ICDCode3 TEXT, ICDCode4 TEXT, VisitType TEXT);";
    private static final String createTableClaimItems = "CREATE TABLE IF NOT EXISTS tblClaimItems(ClaimUUID TEXT, ItemCode TEXT, ItemPrice TEXT, ItemQuantity TEXT);";
    private static final String createTableClaimServices = "CREATE TABLE IF NOT EXISTS tblClaimServices(ClaimUUID TEXT, ServiceCode TEXT, ServicePrice TEXT, ServiceQuantity TEXT);";
    private static final String createTableClaimUploadStatus = "CREATE TABLE IF NOT EXISTS tblClaimUploadStatus(ClaimUUID TEXT, UploadDate TEXT, UploadStatus TEXT, UploadMessage TEXT);";

    public final String REFERENCE_UNKNOWN;

    private final Global global;
    private SQLiteDatabase db;
    private SQLiteDatabase dbMapping;

    public SQLHandler(Context context) {
        super(context, DB_NAME_MAPPING, null, 3);
        global = (Global) context.getApplicationContext();
        REFERENCE_UNKNOWN = context.getResources().getString(R.string.Unknown);
        createOrOpenDatabases();
    }

    public void createOrOpenDatabases() {
        if (!checkDatabase()) {
            db = SQLiteDatabase.openOrCreateDatabase(DB_NAME_DATA, null);
        }
        if (!checkMapping()) {
            dbMapping = SQLiteDatabase.openOrCreateDatabase(DB_NAME_MAPPING, null);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Nullable
    public Cursor getMapping(String Type) {
        try {
            try {
                db.execSQL("ATTACH DATABASE '" + DB_NAME_MAPPING + "' AS dbMapping1");
            } catch (SQLException ignored) {
            }
            return db.rawQuery("select I.code,I.name,M.Type AS isMapped FROM tblReferences I LEFT OUTER JOIN dbMapping1.tblMapping M ON I.Code = M.Code WHERE I.Type =?", new String[]{Type});
        } catch (SQLException e) {
            Log.d("ErrorOnFetchingData", e.getMessage());
            return null;
        }
    }

    public String getPrice(String code, String type) {
        String price = "0";
        try (Cursor c = db.query("tblReferences", new String[]{"Price"}, "LOWER(Code) = LOWER(?) AND LOWER(Type) = LOWER(?)", new String[]{code, type}, null, null, null, "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                String result = c.getString(0);
                if (!TextUtils.isEmpty(result)) {
                    price = result;
                }
            }
        } catch (SQLException e) {
            Log.d("ErrorOnFetchingData", String.format("Error while getting price of %s", code), e);
        }
        return price;
    }

    public String getItemPrice(String code) {
        return getPrice(code, "I");
    }

    public String getServicePrice(String code) {
        return getPrice(code, "S");
    }

    public boolean InsertMapping(String Code, String Name, String Type) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("Code", Code);
            cv.put("Name", Name);
            cv.put("Type", Type);

            dbMapping.insert("tblMapping", null, cv);
        } catch (SQLiteFullException e) {
            return false;
        }
        return true;
    }

    public void InsertReferences(String Code, String Name, String Type, String Price) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("Code", Code);
            cv.put("Name", Name);
            cv.put("Type", Type);
            cv.put("Price", Price);

            db.insert("tblReferences", null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertControls(String FieldName, String Adjustability) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("FieldName", FieldName);
            cv.put("Adjustability", Adjustability);
            db.insert("tblControls", null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertClaimAdmins(String Code, String hfCode, String Name) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("Code", Code);
            cv.put("Name", Name);
            cv.put("hfCode", hfCode);
            db.insert("tblClaimAdmins", null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ClearMapping(String Type) {
        dbMapping.delete("tblMapping", "Type = ?", new String[]{Type});
    }

    public void ClearReferencesSI() {
        db.delete("tblReferences", "Type != ?", new String[]{"D"});
    }

    public void ClearAll(String tblName) {
        try {
            db.delete(tblName, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Cursor SearchDisease(String InputText) {
        //Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
        Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%" + InputText + "%' OR Name LIKE '%" + InputText + "%')", null);
        if (c != null) {
            c.moveToFirst();
        }

        return c;
    }

    public String getDiseaseCode(String disease) {
        String code = "";
        try {
            String table = "tblReferences";
            String[] columns = {"Code"};
            String selection = "Type='D' and Name=?";
            String[] selectionArgs = {disease};
            String limit = "1";
            Cursor c = db.query(table, columns, selection, selectionArgs, null, null, null, limit);
            if (c.getCount() == 1) {
                c.moveToFirst();
                code = c.getString(c.getColumnIndexOrThrow("Code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public Cursor filterItemsServices(String nameFilter, String typeFilter) {
        String wildcardNameFilter = "%" + nameFilter + "%";
        Cursor c = dbMapping.query("tblMapping",
                new String[]{"Code AS _id", "Code", "Name"},
                "type = ? AND (Code LIKE ? OR Name LIKE ?)",
                new String[]{typeFilter, wildcardNameFilter, wildcardNameFilter},
                null,
                null,
                null);

        if (c != null) {
            c.moveToFirst();
        }

        return c;
    }

    public Cursor searchItems(String filter) {
        return filterItemsServices(filter, "I");
    }

    public Cursor searchServices(String filter) {
        return filterItemsServices(filter, "S");
    }

    public String getAdjustability(String FieldName) {
        String adjustability = "M";
        Cursor cursor = null;
        try {
            String query = "SELECT Adjustability FROM tblControls WHERE FieldName = '" + FieldName + "'";
            cursor = db.rawQuery(query, null);
            // looping through all rows
            if (cursor.moveToFirst()) {
                do {
                    adjustability = cursor.getString(0);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            return adjustability;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return adjustability;
    }

    public boolean checkTableExists(String table) {
        boolean tableExists = false;
        try (Cursor c = db.query(true, "sqlite_master", new String[]{"tbl_name"}, "tbl_name = ?", new String[]{table},
                null, null, null, "1")) {
            tableExists = c.getCount() > 0;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while checking if table exists: " + table, e);
        }
        return tableExists;
    }

    public boolean checkIfAny(String table) {
        boolean any = false;
        if (checkTableExists(table)) {
            try (Cursor c = db.query(table, null, null, null, null, null, null, "1")) {
                any = c.getCount() > 0;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while checking if any: " + table, e);
            }
        }
        return any;
    }

    public boolean checkIfExists(String table, String whereClause, String... whereArgs) {
        boolean exists = false;
        if (checkTableExists(table)) {
            try (Cursor c = db.query(table, null, whereClause, whereArgs, null, null, null, "1")) {
                exists = c.getCount() > 0;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while checking if any: " + table, e);
            }
        }
        return exists;
    }

    public String getClaimAdminInfo(String Code, String column) {
        String Info = "";
        String query = "SELECT " + column + " FROM tblClaimAdmins WHERE upper(Code) like '" + Code.toUpperCase() + "'";
        try (Cursor cursor1 = db.rawQuery(query, null)) {
            // looping through all rows
            if (cursor1.moveToFirst()) {
                do {
                    Info = cursor1.getString(0);
                } while (cursor1.moveToNext());
            }
        } catch (Exception e) {
            return Info;
        }

        return Info;
    }

    public void createTables() {
        String[] commands = {CreateTableControls, CreateTableReferences, CreateTableClaimAdmins,
                createTablePolicyInquiry, createTableClaimDetails, createTableClaimItems, createTableClaimServices,
                createTableClaimUploadStatus};
        for (String command : commands) {
            try {
                db.execSQL(command);
            } catch (Exception e) {
                Log.e("SQL", "Error while excecutiong executing command: " + command, e);
            }
        }
    }

    public void createMappingTables() {
        String[] commandsMapping = {CreateTableMapping};
        for (String command : commandsMapping) {
            try {
                dbMapping.execSQL(command);
            } catch (Exception e) {
                Log.e("SQL", "Error while excecutiong executing command (mapping): " + command, e);
            }
        }
    }

    public void closeDatabases() {
        db.close();
        dbMapping.close();
    }

    public boolean checkDatabase() {
        return db != null && db.isOpen();
    }

    public boolean checkMapping() {
        return dbMapping != null && dbMapping.isOpen();
    }

    public void saveClaim(@NonNull ContentValues claimDetails, @NonNull Iterable<ContentValues> claimItems, @NonNull Iterable<ContentValues> claimServices) {
        if (checkIfExists("tblClaimDetails", "ClaimUUID = ?", claimDetails.getAsString("ClaimUUID"))) {
            updateClaim(claimDetails, claimItems, claimServices);
        } else {
            insertClaim(claimDetails, claimItems, claimServices);
        }

    }

    public void insertClaim(ContentValues claimDetails, Iterable<ContentValues> claimItems, Iterable<ContentValues> claimServices) {
        try {
            db.beginTransaction();
            db.insertOrThrow("tblClaimDetails", null, claimDetails);
            for (ContentValues claimItem : claimItems) {
                db.insertOrThrow("tblClaimItems", null, claimItem);
            }
            for (ContentValues claimService : claimServices) {
                db.insertOrThrow("tblClaimServices", null, claimService);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while inserting claim", e);
        } finally {
            db.endTransaction();
        }
    }

    public void updateClaim(ContentValues claimDetails, Iterable<ContentValues> claimItems, Iterable<ContentValues> claimServices) {
        try {
            db.beginTransaction();
            String claimUUID = claimDetails.getAsString("ClaimUUID");
            db.update("tblClaimDetails", claimDetails, "ClaimUUID = ?", new String[]{claimUUID});
            db.delete("tblClaimItems", "ClaimUUID = ?", new String[]{claimUUID});
            db.delete("tblClaimServices", "ClaimUUID = ?", new String[]{claimUUID});
            for (ContentValues claimItem : claimItems) {
                db.insert("tblClaimItems", null, claimItem);
            }
            for (ContentValues claimService : claimServices) {
                db.insert("tblClaimServices", null, claimService);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while inserting claim", e);
        } finally {
            db.endTransaction();
        }
    }

    public void deleteClaim(String claimUUID) {
        try {
            String claimUUIDUpper = claimUUID.toUpperCase(Locale.US);
            db.beginTransaction();
            db.delete("tblClaimItems", "UPPER(ClaimUUID) = ?", new String[]{claimUUIDUpper});
            db.delete("tblClaimServices", "UPPER(ClaimUUID) = ?", new String[]{claimUUIDUpper});
            db.delete("tblClaimUploadStatus", "UPPER(ClaimUUID) = ?", new String[]{claimUUIDUpper});
            db.delete("tblClaimDetails", "UPPER(ClaimUUID) = ?", new String[]{claimUUIDUpper});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while inserting claim", e);
        } finally {
            db.endTransaction();
        }
    }

    public JSONObject getClaim(String claimUUID) {
        JSONArray claimDetails = getQueryResultAsJsonArray("tblClaimDetails",
                new String[]{"ClaimUUID", "ClaimDate", "HFCode", "ClaimAdmin", "ClaimCode", "GuaranteeNumber", "InsureeNumber", "StartDate", "EndDate", "ICDCode", "Comment", "Total", "ICDCode1", "ICDCode2", "ICDCode3", "ICDCode4", "VisitType"},
                "LOWER(ClaimUUID) = ?",
                new String[]{claimUUID.toLowerCase(Locale.ROOT)});

        if (claimDetails.length() == 0) {
            return null;
        }

        if (claimDetails.length() > 1) {
            Log.e(LOG_TAG, "Multiple claims with the same UUID");
            return null;
        }

        try {
            JSONObject resultClaim = new JSONObject();
            resultClaim.put("details", claimDetails.getJSONObject(0));
            resultClaim.put("items", getClaimItems(claimUUID));
            resultClaim.put("services", getClaimServices(claimUUID));

            return resultClaim;
        } catch (JSONException e) {
            Log.e(LOG_TAG, String.format("Error while processing claim (%s)", claimUUID));
        }

        return null;
    }

    @NonNull
    public JSONArray getAllPendingClaims() {
        // Rename InsureeNumber to CHFID
        // This is required to support legacy Rest API and Web App
        JSONArray claims = getQueryResultAsJsonArray(
                "SELECT ClaimUUID, ClaimDate, HFCode, ClaimAdmin, ClaimCode, GuaranteeNumber, InsureeNumber AS CHFID, StartDate, EndDate, ICDCode, Comment, Total, ICDCode1, ICDCode2, ICDCode3, ICDCode4, VisitType" +
                        " FROM tblClaimDetails cd" +
                        " WHERE NOT EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND cus.UploadStatus != ?)",
                new String[]{CLAIM_UPLOAD_STATUS_ERROR}
        );

        JSONArray result = new JSONArray();
        try {
            for (int i = 0; i < claims.length(); i++) {
                JSONObject claim = claims.getJSONObject(i);
                String ClaimUUID = (String) claim.remove("ClaimUUID");

                JSONObject resultClaim = new JSONObject();
                resultClaim.put("details", claim);
                resultClaim.put("items", getClaimItems(ClaimUUID));
                resultClaim.put("services", getClaimServices(ClaimUUID));
                result.put(resultClaim);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while getting pending claims", e);
        }
        return result;
    }

    @NonNull
    public JSONArray getClaimItems(String claimUUID) {
        return getQueryResultAsJsonArray(
                "tblClaimItems",
                new String[]{"ItemCode", "ItemPrice", "ItemQuantity"},
                "ClaimUUID = ?",
                new String[]{claimUUID}
        );
    }

    @NonNull
    public JSONArray getClaimServices(String claimUUID) {
        return getQueryResultAsJsonArray(
                "tblClaimServices",
                new String[]{"ServiceCode", "ServicePrice", "ServiceQuantity"},
                "ClaimUUID = ?",
                new String[]{claimUUID}
        );
    }

    @NonNull
    public JSONArray getQueryResultAsJsonArray(@NonNull String rawQuery, String[] selectionArgs) {
        JSONArray resultArray = new JSONArray();

        try (Cursor c = db.rawQuery(rawQuery, selectionArgs)) {
            while (c.moveToNext()) {
                JSONObject row = new JSONObject();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    row.put(c.getColumnName(i), c.getString(i));
                }

                resultArray.put(row);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while getting executing a query", e);
        }

        return resultArray;
    }

    @NonNull
    public JSONArray getQueryResultAsJsonArray(@NonNull String tableName, String[] columns, String selection, String[] selectionArgs) {
        JSONArray resultArray = new JSONArray();

        try (Cursor c = db.query(tableName, columns, selection, selectionArgs, null, null, null)) {
            while (c.moveToNext()) {
                JSONObject row = new JSONObject();
                for (String column : columns) {
                    row.put(column, c.getString(c.getColumnIndexOrThrow(column)));
                }
                resultArray.put(row);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while getting executing a query", e);
        }

        return resultArray;
    }

    public void insertClaimUploadStatus(@NonNull String claimUUID, @NonNull String uploadDate, @NonNull String uploadStatus, String uploadMessage) {
        ContentValues cv = new ContentValues();
        cv.put("ClaimUUID", claimUUID);
        cv.put("UploadDate", uploadDate);
        cv.put("UploadStatus", uploadStatus);
        if (uploadMessage != null) {
            cv.put("UploadMessage", uploadMessage);
        }

        try {
            db.insert("tblClaimUploadStatus", "ClaimUUID", cv);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while inserting claim upload status", e);
        }
    }

    public String getClaimUUIDForCode(@NonNull String claimCode) {
        JSONArray claims = getQueryResultAsJsonArray("tblClaimDetails", new String[]{"ClaimUUID"}, "ClaimCode = ?", new String[]{claimCode});
        if (claims.length() < 1) {
            return null;
        }
        if (claims.length() > 1) {
            Log.e(LOG_TAG, "Multiple claims for claim code: " + claimCode);
        }

        try {
            return claims.getJSONObject(0).getString("ClaimUUID");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while getting claim uuid", e);
        }

        return null;
    }

    @NonNull
    public JSONObject getClaimCounts() {
        JSONArray claimCounts = getQueryResultAsJsonArray(
                "SELECT CASE WHEN cus.UploadStatus IS NULL OR cus.UploadStatus = ? THEN ? ELSE cus.UploadStatus END AS Status, count(*) AS Amount" +
                        " FROM tblClaimDetails cd LEFT JOIN tblClaimUploadStatus cus on cd.ClaimUUID=cus.ClaimUUID" +
                        " GROUP BY Status",
                new String[]{CLAIM_UPLOAD_STATUS_ERROR, CLAIM_UPLOAD_STATUS_ENTERED}
        );

        JSONObject result = new JSONObject();
        try {
            for (int i = 0; i < claimCounts.length(); i++) {
                JSONObject row = claimCounts.getJSONObject(i);
                result.put(row.getString("Status"), row.getString("Amount"));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while parsing claim counts", e);
        }

        return result;
    }

    @NonNull
    public JSONArray getClaimInfo(String selection, String[] selectionArgs) {
        String query = "SELECT " +
                "ClaimUUID, ClaimCode, ClaimDate, InsureeNumber, " +
                "COALESCE((SELECT SUM(ItemPrice*ItemQuantity) FROM tblClaimItems ci WHERE ci.ClaimUUID = cd.ClaimUUID GROUP BY ci.ClaimUUID), 0) " +
                "+ COALESCE((SELECT SUM(ServicePrice*ServiceQuantity) FROM tblClaimServices cs WHERE cs.ClaimUUID = cd.ClaimUUID GROUP BY cs.ClaimUUID), 0) AS TotalClaimed " +
                "FROM tblClaimDetails cd";

        if (selection != null) {
            query = query + " WHERE " + selection;
        }

        return getQueryResultAsJsonArray(query, selectionArgs);
    }

    @NonNull
    public JSONArray getEnteredClaimInfo() {
        return getClaimInfo("NOT EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND cus.UploadStatus != ?)", new String[]{CLAIM_UPLOAD_STATUS_ERROR});
    }

    @NonNull
    public JSONArray getAcceptedClaimInfo() {
        return getClaimInfo("EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND (cus.UploadStatus = ? OR cus.UploadStatus = ?))" +
                " AND NOT EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND cus.UploadStatus = ?)", new String[]{CLAIM_UPLOAD_STATUS_ACCEPTED, CLAIM_UPLOAD_STATUS_EXPORTED, CLAIM_UPLOAD_STATUS_ARCHIVED});
    }

    @NonNull
    public JSONArray getRejectedClaimInfo() {
        return getClaimInfo("EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND cus.UploadStatus = ?)" +
                " AND NOT EXISTS (SELECT cus.ClaimUUID FROM tblClaimUploadStatus cus WHERE cus.ClaimUUID = cd.ClaimUUID AND cus.UploadStatus = ?)", new String[]{CLAIM_UPLOAD_STATUS_REJECTED, CLAIM_UPLOAD_STATUS_ARCHIVED});
    }

    @NonNull
    public String getReferenceName(@NonNull String referenceCode) {
        JSONArray result = getQueryResultAsJsonArray("tblReferences", new String[]{"Name"}, "Code = ?", new String[]{referenceCode});

        if (result.length() < 1) {
            Log.w(LOG_TAG, "Unknown reference name for code: " + referenceCode);
            return REFERENCE_UNKNOWN;
        } else if (result.length() > 1) {
            Log.w(LOG_TAG, "Multiple reference names for code: " + referenceCode);
        }

        try {
            JSONObject resultObject = result.getJSONObject(0);
            return resultObject.getString("Name");
        } catch (JSONException | IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Error while parsing reference name result", e);
            return REFERENCE_UNKNOWN;
        }
    }

}
