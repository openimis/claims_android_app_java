package org.openimis.imisclaims;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;

import org.openimis.imisclaims.tools.Log;

public class AddItems extends ImisActivity {
    ListView lvItems;
    TextView tvCode, tvName;
    EditText etQuantity, etAmount;
    Button btnAdd;
    AutoCompleteTextView etItems;

    int Pos;

    HashMap<String, String> oItem;
    SimpleAdapter alAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additems);

        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.app_name_claim));
        }

        lvItems = findViewById(R.id.lvItems);
        tvCode = findViewById(R.id.tvCode);
        tvName = findViewById(R.id.tvName);
        etQuantity = findViewById(R.id.etQuantity);
        etAmount = findViewById(R.id.etAmount);
        etItems = findViewById(R.id.etItems);
        btnAdd = findViewById(R.id.btnAdd);

        alAdapter = new SimpleAdapter(AddItems.this, ClaimActivity.lvItemList, R.layout.lvitem,
                new String[]{"Code", "Name", "Price", "Quantity"},
                new int[]{R.id.tvLvCode, R.id.tvLvName, R.id.tvLvPrice, R.id.tvLvQuantity});
        lvItems.setAdapter(alAdapter);

        if (isIntentReadonly()) {
            disableView(etQuantity);
            disableView(etAmount);
            disableView(etItems);
            disableView(btnAdd);
        } else {
            ItemAdapter itemAdapter = new ItemAdapter(this, sqlHandler);
            etItems.setAdapter(itemAdapter);
            etItems.setThreshold(1);

            etItems.setOnItemClickListener((parent, view, position, l) -> {
                if (position >= 0) {

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    final int itemColumnIndex = cursor.getColumnIndexOrThrow("Code");
                    final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
                    String Code = cursor.getString(itemColumnIndex);
                    String Name = cursor.getString(descColumnIndex);

                    oItem = new HashMap<>();
                    oItem.put("Code", Code);
                    oItem.put("Name", Name);

                    etQuantity.setText("1");
                    etAmount.setText(sqlHandler.getItemPrice(Code));
                }

            });

            etItems.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                            && etQuantity.getText().toString().trim().length() != 0
                            && etAmount.getText().toString().trim().length() != 0);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                            && etItems.getText().toString().trim().length() != 0
                            && etAmount.getText().toString().trim().length() != 0);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            etAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                            && etQuantity.getText().toString().trim().length() != 0
                            && etItems.getText().toString().trim().length() != 0);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            lvItems.setAdapter(alAdapter);

            btnAdd.setEnabled(false);
            btnAdd.setOnClickListener(v -> {
                try {
                    if (oItem == null) return;

                    String Amount, Quantity;

                    HashMap<String, String> lvItem = new HashMap<>();
                    lvItem.put("Code", oItem.get("Code"));
                    lvItem.put("Name", oItem.get("Name"));
                    Amount = etAmount.getText().toString();
                    lvItem.put("Price", Amount);
                    if (etQuantity.getText().toString().length() == 0) Quantity = "1";
                    else Quantity = etQuantity.getText().toString();
                    lvItem.put("Quantity", Quantity);
                    ClaimActivity.lvItemList.add(lvItem);

                    alAdapter.notifyDataSetChanged();

                    etItems.setText("");
                    etAmount.setText("");
                    etQuantity.setText("");

                } catch (Exception e) {
                    Log.d("AddLvError", e.getMessage());
                }
            });

            lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
                try {

                    Pos = position;
                    HideAllDeleteButtons();

                    Button d = view.findViewById(R.id.btnDelete);
                    d.setVisibility(View.VISIBLE);

                    d.setOnClickListener(v -> {
                        ClaimActivity.lvItemList.remove(Pos);
                        HideAllDeleteButtons();
//						alAdapter.notifyDataSetChanged();
                    });


                } catch (Exception e) {
                    Log.d("ErrorOnLongClick", e.getMessage());
                }
                return true;
            });
        }
    }

    private boolean isIntentReadonly() {
        Intent intent = getIntent();
        return intent.getBooleanExtra(ClaimActivity.EXTRA_READONLY, false);
    }

    private void HideAllDeleteButtons() {
        for (int i = 0; i <= lvItems.getLastVisiblePosition(); i++) {
            Button Delete = (Button) lvItems.getChildAt(i).findViewById(R.id.btnDelete);
            Delete.setVisibility(View.GONE);
        }
    }
}
