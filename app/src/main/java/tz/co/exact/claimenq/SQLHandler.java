package tz.co.exact.claimenq;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tz.co.exact.claimenq.ClaimActivity;

/**
 * Created by user on 10/01/2018.
 */

public class SQLHandler extends SQLiteOpenHelper{

	private static final String DB_NAME = MainActivity.Path + "Mapping.db3";
	private static final String CreateTable = "CREATE TABLE tblMapping(Code text,Name text,Type text);";

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
		db.execSQL(CreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public Cursor getData(String Table,String Columns[],String Criteria){
		try {
			//db = SQLiteDatabase.openDatabase(ClaimManagementActivity.Path + "ImisData.db3", null,SQLiteDatabase.OPEN_READONLY);

			Cursor c = dbMapping.query(Table, Columns, Criteria, null, null, null, null);

			return c;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("ErroOnFetchingData", e.getMessage());
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
			Log.d("ErroOnFetchingData", e.getMessage());
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

	public void ClearMapping(String Type){
		String sSQL = "";
		sSQL = "DELETE FROM tblMapping WHERE TYpe = '"+ Type +"'";
		dbMapping.execSQL(sSQL);
	}
	public Cursor SearchDisease(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	public Cursor SearchItems(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'I' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		if (c != null){
			c.moveToFirst();
		}

		return c;
	}

	public Cursor SearchServices(String InputText){
		//Cursor c = db.rawQuery("SELECT Code as _id,Code, Name,Code + ' ' + Name AS Disease FROM tblReferences WHERE Type = 'D' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
		Cursor c = db.rawQuery("SELECT Code as _id,Code, Name FROM tblReferences WHERE Type = 'S' AND (Code LIKE '%"+ InputText +"%' OR Name LIKE '%"+ InputText +"%')",null);
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
			e.printStackTrace();
		}

		return adjustibility;
	}
}
