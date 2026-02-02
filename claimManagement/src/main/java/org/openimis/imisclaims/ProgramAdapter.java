package org.openimis.imisclaims;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.openimis.imisclaims.tools.Log;

import java.util.ArrayList;
import java.util.List;

public class ProgramAdapter extends CursorAdapter implements AdapterView.OnItemClickListener {
    SQLHandler sqlHandler;
    SQLiteDatabase db;

    public ProgramAdapter(Context context, SQLHandler sqlHandler) {
        super(context, null, 0);
        this.sqlHandler = sqlHandler;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.program_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
        String Suggestion = cursor.getString(descColumnIndex);
        TextView text1 = view.findViewById(R.id.text2);
        text1.setText(Suggestion);

    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        return sqlHandler.SearchProgram((constraint != null ? constraint.toString() : ""));
    }

    @Override
    public CharSequence convertToString(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow("Name"));
    }


    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
    }
}
