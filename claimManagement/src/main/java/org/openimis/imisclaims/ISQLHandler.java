package org.openimis.imisclaims;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public interface ISQLHandler {

    // --- Mapping & References ---
    @Nullable Cursor getMapping(String type);
    boolean InsertMapping(String code, String name, String type);
    void InsertReferences(String code, String name, String type, String price);
    void ClearMapping(String type);
    void ClearReferencesSI();
    String getPrice(String code, String type);
    String getItemPrice(String code);
    String getServicePrice(String code);
    String getReferenceName(@NonNull String referenceCode);

    // --- Controls ---
    void InsertControls(String fieldName, String adjustability);
    String getAdjustability(String fieldName);

    // --- Claim admins ---
    void InsertClaimAdmins(String code, String hfCode, String name);
    String getClaimAdminInfo(String code, String column);

    // --- Tables ---
    void createTables();
    void createMappingTables();
    boolean checkTableExists(String table);
    boolean checkIfAny(String table);
    boolean checkIfExists(String table, String whereClause, String... whereArgs);
    void ClearAll(String tableName);

    // --- Claims ---
    void saveClaim(@NonNull ContentValues claimDetails,
                   @NonNull Iterable<ContentValues> claimItems,
                   @NonNull Iterable<ContentValues> claimServices);

    void insertClaim(ContentValues claimDetails,
                     Iterable<ContentValues> claimItems,
                     Iterable<ContentValues> claimServices);

    void updateClaim(ContentValues claimDetails,
                     Iterable<ContentValues> claimItems,
                     Iterable<ContentValues> claimServices);

    void deleteClaim(String claimUUID);

    @Nullable JSONObject getClaim(String claimUUID);
    @NonNull JSONArray getAllPendingClaims();
    @NonNull JSONArray getClaimItems(String claimUUID);
    @NonNull JSONArray getClaimServices(String claimUUID);

    void insertClaimUploadStatus(@NonNull String claimUUID,
                                 @NonNull String uploadDate,
                                 @NonNull String uploadStatus,
                                 String uploadMessage);

    String getClaimUUIDForCode(@NonNull String claimCode);
    @NonNull JSONObject getClaimCounts();

    // --- Query helpers ---
    @NonNull JSONArray getQueryResultAsJsonArray(@NonNull String rawQuery, String[] selectionArgs);
    @NonNull JSONArray getQueryResultAsJsonArray(@NonNull String tableName,
                                                 String[] columns,
                                                 String selection,
                                                 String[] selectionArgs);

    // --- Claim infos ---
    @NonNull JSONArray getClaimInfo(String selection, String[] selectionArgs);
    @NonNull JSONArray getEnteredClaimInfo();
    @NonNull JSONArray getAcceptedClaimInfo();
    @NonNull JSONArray getRejectedClaimInfo();

    // --- Services & Items ---
    String getServiceName(String code);
    String getPackageType(String code);
    String getServiceId(String code);
    String getManualPrice(String code);

    void InsertService(String id, String code, String name, String type,
                       String price, String packageType, int manualPrice);

    void InsertItem(String id, String code, String name, String type, String price);
    void InsertSubServices(String serviceId, String serviceLinked, String qty, String price);
    void InsertSubItems(String itemId, String serviceId, String qty, String price);

    JSONArray getSubServicesIds(String id);
    JSONArray getSubItemsId(String id);
    JSONObject getService(String serviceId);
    JSONObject getItem(String itemId);

    // --- Search ---
    Cursor SearchDisease(String inputText);
    Cursor searchItems(String filter);
    Cursor searchServices(String filter);
    Cursor SearchHF(String inputText);

    // --- Health facilities ---
    void InsertHealthFacilities(String id, String code, String name);

    // --- Lifecycle ---
    void closeDatabases();
}
