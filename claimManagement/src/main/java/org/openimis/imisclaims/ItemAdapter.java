package org.openimis.imisclaims;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ItemAdapter extends CursorAdapter {
    SQLHandler sqlHandler;
    SQLiteDatabase db;

    public ItemAdapter(Context context, SQLHandler sqlHandler) {
        super(context, null, 0);
        this.sqlHandler = sqlHandler;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.spinneritem, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvCode = (TextView) view.findViewById(R.id.tvCode);
        tvCode.setText(cursor.getString(cursor.getColumnIndexOrThrow("Code")));

        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(cursor.getString(cursor.getColumnIndexOrThrow("Name")));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
        return sqlHandler.searchItems((constraint != null ? constraint.toString() : ""));
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow("Code"));
    }
}
