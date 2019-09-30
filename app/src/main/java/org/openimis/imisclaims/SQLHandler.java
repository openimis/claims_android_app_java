package org.openimis.imisclaims;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * Created by user on 10/01/2018.
 */

public class SQLHandler extends SQLiteOpenHelper{

	SQLiteDatabase db;
	String dbPath = "/data/data/org.openimis.imisclaims/databases/ImisData.db3";


	public static final String DB_NAME = "ImisData.db3";
	private static final String CreateTableMapping = "CREATE TABLE tblMapping(Code text,Name text,Type text)";
	private static final String CreateTableControls = "CREATE TABLE tblControls(FieldName text, Adjustibility text)";
	private static final String CreateTableClaimAdmins = "CREATE TABLE tblAdministrators(Code text, Name text, hfCode text)";
	private static final String CreateTableReferences = "CREATE TABLE tblReferences(Code text, Name text, Type text, Price text)";
	//private static final String CreateTableDateUpdates = "CREATE TABLE tblDateUpdates(Id INTEGER PRIMARY KEY AUTOINCREMENT, last_update_date text);";


	public SQLHandler(Context context) {
		super(context, DB_NAME, null, 3);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CreateTableMapping);
		db.execSQL(CreateTableClaimAdmins);
		db.execSQL(CreateTableControls);
		db.execSQL(CreateTableReferences);
	}

/*	@Override
	public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
    }*/

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//db.execSQL("DROP TABLE IF EXISTS tblMapping");
		db.execSQL("DROP TABLE IF EXISTS tblControls");
		db.execSQL("DROP TABLE IF EXISTS tblAdministrators");
		db.execSQL("DROP TABLE IF EXISTS tblReferences");
	}


	private void openDatabase() {
		try{
			String dbPath = "/data/data/org.openimis.imisclaims/databases/" + DB_NAME;
			if (db != null && db.isOpen()) {
				return;
			}
			db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		}catch (SQLException e){
			e.printStackTrace();
		}
	}
	public void closeDb(){
		db.close();
    }



/*	public Cursor getData(String Table,String Columns[],String Criteria){
		try {
			//db = SQLiteDatabase.openDatabase(ClaimManagementActivity.Path + "ImisData.db3", null,SQLiteDatabase.OPEN_READONLY);

			Cursor c = dbMapping.query(Table, Columns, Criteria, null, null, null, null);

			return c;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("ErrorOnFetchingData", e.getMessage());
			return null;
		}
	}*/


	public String getHealthFacilityCode(String Code) {
		String Name = "";
		try {
			openDatabase();
			String query = "SELECT hfCode FROM tblAdministrators WHERE upper(Code) like '"+Code.toUpperCase()+"'";
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

	public Cursor getMapping(String Type){
		openDatabase();
		Cursor c;
		try {
		    String sql = "SELECT R.Code,R.Name,M.Type FROM tblReferences R LEFT OUTER JOIN tblMapping M ON R.Code = M.Code where R.Type = '" + Type + "'";
			c =  db.rawQuery(sql, null);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return c;
	}

	public boolean InsertMapping(String Code,String Name,String Type){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblMapping(Code,Name,Type)VALUES('"+ Code.replace("'", "''") +"','"+ Name.replace("'", "''") +"','"+ Type +"')";
			db.execSQL(sSQL);
		} catch (SQLiteFullException e){
			return false;
		}
		return true;
	}
	public void ClearMapping(String Type){
        openDatabase();
		String sSQL = "";
		sSQL = "DELETE FROM tblMapping WHERE Type = '"+ Type +"'";
		db.execSQL(sSQL);
	}
	public void InsertReferences(String Code,String Name,String Type, String Price){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblReferences(Code,Name,Type,Price)VALUES('"+Code+"','"+Name+"','"+Type+"','"+Price+"')";
			db.execSQL(sSQL);
		} catch (Exception e){
			e.printStackTrace();

		}
	}
		public Cursor SearchItems(String InputText){
	    openDatabase();
            Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblMapping WHERE Type = 'I' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblMapping WHERE Type = 'I' ",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	public Cursor SearchServices(String InputText){
        openDatabase();
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblMapping WHERE Type = 'S' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
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
	public void InsertClaimAdmins(String Code,String Name, String hfCode){
		try {
			String sSQL = "";
			sSQL = "INSERT INTO tblAdministrators(Code,Name,hfCode)VALUES('"+Code+"','"+Name+"','"+hfCode+"')";
			db.execSQL(sSQL);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void ClearReferencesSI(){
		String sSQL = "";
		sSQL = "DELETE FROM tblReferences WHERE Type != 'D'";
		db.execSQL(sSQL);
	}
	public void ClearAll(String tblName){
		openDatabase();
		try {
			String sSQL = "";
			sSQL = "DELETE FROM "+tblName+"";
			db.execSQL(sSQL);
		}catch (SQLException e){
			e.printStackTrace();
		}

	}
	public Cursor SearchDisease(String InputText){
        openDatabase();
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
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
			String query = "SELECT Name FROM tblAdministrators WHERE upper(Code) like '"+Code.toUpperCase()+"'";
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


}
