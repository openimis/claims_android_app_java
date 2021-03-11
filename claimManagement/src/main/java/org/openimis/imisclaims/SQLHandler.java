package org.openimis.imisclaims;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.database.DatabaseUtils.sqlEscapeString;

/**
 * Created by user on 10/01/2018.
 */

public class SQLHandler extends SQLiteOpenHelper{

	private static final String DB_NAME = MainActivity.Path + "Mapping.db3";
	private static final String CreateTable = "CREATE TABLE IF NOT EXISTS tblMapping(Code text,Name text,Type text);";
	private static final String CreateTableControls = "CREATE TABLE IF NOT EXISTS tblControls(FieldName text, Adjustibility text);";
	private static final String CreateTableClaimAdmins = "CREATE TABLE IF NOT EXISTS tblClaimAdmins(Code text, Name text);";
	private static final String CreateTableReferences = "CREATE TABLE IF NOT EXISTS tblReferences(Code text, Name text, Type text, Price text);";
	//private static final String CreateTableDateUpdates = "CREATE TABLE tblDateUpdates(Id INTEGER PRIMARY KEY AUTOINCREMENT, last_update_date text);";

	SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(MainActivity.Path + "ImisData.db3",null);
	//SQLiteDatabase dbMapping = SQLiteDatabase.openOrCreateDatabase(ClaimManagementActivity.Path + "Mapping.db3", null);
	SQLiteDatabase dbMapping = this.getWritableDatabase();

	public SQLHandler(Context context) {
		super(context, DB_NAME, null, 3);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		checkDataBase();
		db.execSQL(CreateTable);
		//db.execSQL(CreateTableControls);
		//db.execSQL(CreateTableDateUpdates);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(MainActivity.Path + "ImisData.db3", null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database doesn't exist yet.
			return false;
		}
		return true;
	}
	public Cursor getData(String Table,String Columns[],String Criteria){
		try {
			//db = SQLiteDatabase.openDatabase(ClaimManagementActivity.Path + "ImisData.db3", null,SQLiteDatabase.OPEN_READONLY);

			Cursor c = dbMapping.query(Table, Columns, Criteria, null, null, null, null);

			return c;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("ErrorOnFetchingData", e.getMessage());
			return null;
		}
	}

	public Cursor getMapping(String Type){
		String dbMappingPath = MainActivity.Path + "Mapping.db3";

		try {
			db.execSQL("ATTACH DATABASE '"+ dbMappingPath +"' AS dbMapping1");
			Cursor c =  db.rawQuery("select I.code,I.name,M.Type AS isMapped FROM tblReferences I LEFT OUTER JOIN dbMapping1.tblMapping M ON I.Code = M.Code WHERE I.Type =?", new String[]{Type});
			return c;
		} catch (SQLException e) {
			Log.d("ErrorOnFetchingData", e.getMessage());
			return null;
		}
	}

	public boolean InsertMapping(String Code,String Name,String Type){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblMapping(Code,Name,Type)VALUES('"+ Code.replace("'", "''") +"','"+ Name.replace("'", "''") +"','"+ Type +"')";
			dbMapping.execSQL(sSQL);
		} catch (SQLiteFullException e){
			return false;
		}
		return true;
	}
	public void InsertReferences(String Code,String Name,String Type, String Price){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblReferences(Code,Name,Type,Price)VALUES(\""+Code+"\",\""+Name+"\",\""+Type+"\",\""+Price+"\")";
			db.execSQL(sSQL);
		} catch (Exception e){
			e.printStackTrace();

		}
	}
	public void InsertControls(String FieldName,String Adjustibility){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblControls(FieldName,Adjustibility)VALUES('"+FieldName+"','"+Adjustibility+"')";
			db.execSQL(sSQL);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public void InsertClaimAdmins(String Code,String Name){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblClaimAdmins(Code,Name)VALUES("+sqlEscapeString(Code)+","+sqlEscapeString(Name)+")";
			db.execSQL(sSQL);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void ClearMapping(String Type){
		String sSQL = "";
		sSQL = "DELETE FROM tblMapping WHERE TYpe = '"+ Type +"'";
		dbMapping.execSQL(sSQL);
	}
	public void ClearReferencesSI(){
		String sSQL = "";
		sSQL = "DELETE FROM tblReferences WHERE Type != 'D'";
		db.execSQL(sSQL);
	}
	public void ClearAll(String tblName){
		try {
			String sSQL = "";
			sSQL = "DELETE FROM "+tblName+"";
			db.execSQL(sSQL);
		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public Cursor SearchDisease(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	public String getDiseaseCode(String disease)
	{
		String code = "";
		try {
			String table = "tblReferences";
			String[] columns = {"Code"};
			String selection = "Type='D' and Name=?";
			String[] selectionArgs = {disease};
			String limit = "1";
			Cursor c = db.query(table,columns,selection,selectionArgs,null,null,null,limit);
			if(c.getCount()==1) {
				c.moveToFirst();
				code = c.getString(c.getColumnIndexOrThrow("Code"));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return code;
	}

	public Cursor SearchItems(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = dbMapping.rawQuery("SELECT Code as _id,Code, Name FROM tblMapping WHERE Type = 'I' ",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	public Cursor SearchServices(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = dbMapping.rawQuery("SELECT Code as _id,Code, Name FROM tblMapping WHERE Type = 'S' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	//Created by Herman 27.03.2018
	public String getAdjustibility(String FieldName) {
		String adjustibility = "M";
		try {
			String query = "SELECT Adjustibility FROM tblControls WHERE FieldName = '" + FieldName + "'";
			Cursor cursor1 = db.rawQuery(query, null);
			// looping through all rows
			if (cursor1.moveToFirst()) {
				do {
					adjustibility = cursor1.getString(0);
				} while (cursor1.moveToNext());
			}
		}catch (Exception e){
			return adjustibility;
		}

		return adjustibility;
	}

	public String checkIfAny() {
		String any = null;
		try {
			String query = "SELECT * FROM tblControls";
			Cursor cursor1 = db.rawQuery(query, null);
			// looping through all rows
			if (cursor1.moveToFirst()) {
				do {
					any = cursor1.getString(0);
				} while (cursor1.moveToNext());
			}
		}catch (Exception e){
			return any;
		}

		return any;
	}

	public String getClaimAdmin(String Code) {
		String Name = "";
		try {
			String query = "SELECT Name FROM tblClaimAdmins WHERE upper(Code) like '"+Code.toUpperCase()+"'";
			Cursor cursor1 = db.rawQuery(query, null);
			// looping through all rows
			if (cursor1.moveToFirst()) {
				do {
					Name = cursor1.getString(0);
				} while (cursor1.moveToNext());
			}
		}catch (Exception e){
			return Name;
		}

		return Name;
	}

	public int getAllAdjustibility() {
		int count = 0;
		try {
			String query = "SELECT * FROM tblControls";
			Cursor cursor1 = db.rawQuery(query, null);
			count = cursor1.getColumnCount();
			// looping through all rows
		}catch (Exception e){
			return count;
		}
		return count;
	}

	public int getMaxId() {
		int id = 1;
		try {
			String query = "SELECT MAX(Id) FROM tblDateUpdates";
			Cursor cursor1 = db.rawQuery(query, null);
			if (cursor1.moveToFirst()) {
				do {
					id = cursor1.getInt(0);
				} while (cursor1.moveToNext());
			}
		}catch (Exception e){
			return id;
		}
		return id;
	}

	public void createTables(){
		try {
			db.execSQL(CreateTableControls);
			db.execSQL(CreateTableReferences);
			db.execSQL(CreateTableClaimAdmins);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void getTables(){
		Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
		if (c != null){
			c.moveToFirst();
		}
	}

}
