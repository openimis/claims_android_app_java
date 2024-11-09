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

public class HFAdapter extends CursorAdapter implements AdapterView.OnItemClickListener {
    SQLHandler sqlHandler;
    SQLiteDatabase db;

    public HFAdapter(Context context, SQLHandler sqlHandler) {
        super(context, null, 0);
        this.sqlHandler = sqlHandler;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.hf_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int itemColumnIndex = cursor.getColumnIndexOrThrow("Code");
        final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
        String Suggestion = cursor.getString(itemColumnIndex) + " " + cursor.getString(descColumnIndex);
        TextView text1 = view.findViewById(R.id.textHF);
        text1.setText(Suggestion);

    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        return sqlHandler.SearchHF((constraint != null ? constraint.toString() : ""));
    }

    @Override
    public CharSequence convertToString(Cursor c) {
        return c.getString(c.getColumnIndexOrThrow("Code"));
    }


    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
    }
}