package org.openimis.imisclaims;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;

public class AddServices extends AppCompatActivity {
    ListView lvServices;
    TextView tvCode, tvName;
    EditText etSQuantity, etSAmount;
    Button btnAdd;
    AutoCompleteTextView etServices;
    SQLHandler sqlHandler;

    int Pos;

    HashMap<String, String> oService;
    SimpleAdapter alAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addservices);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.app_name_claim));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sqlHandler = new SQLHandler(this);

        lvServices = findViewById(R.id.lvServices);
        tvCode = findViewById(R.id.tvCode);
        tvName = findViewById(R.id.tvName);
        etSQuantity = findViewById(R.id.etSQuantity);
        etSAmount = findViewById(R.id.etSAmount);
        etServices = findViewById(R.id.etService);

        ServiceAdapter serviceAdapter = new ServiceAdapter(AddServices.this, null);

        etServices.setAdapter(serviceAdapter);
        etServices.setThreshold(1);
        etServices.setOnItemClickListener((parent, view, position, l) -> {
            if (position >= 0) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final int itemColumnIndex = cursor.getColumnIndexOrThrow("Code");
                final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
                String Code = cursor.getString(itemColumnIndex);
                String Name = cursor.getString(descColumnIndex);

                oService = new HashMap<>();
                oService.put("Code", Code);
                oService.put("Name", Name);

                etSQuantity.setText("1");
                etSAmount.setText(sqlHandler.getServicePrice(Code));
            }
        });

        etServices.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                        && etSQuantity.getText().toString().trim().length() != 0
                        && etSAmount.getText().toString().trim().length() != 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                        && etServices.getText().toString().trim().length() != 0
                        && etSAmount.getText().toString().trim().length() != 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                        && etSQuantity.getText().toString().trim().length() != 0
                        && etServices.getText().toString().trim().length() != 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        alAdapter = new SimpleAdapter(AddServices.this, ClaimActivity.lvServiceList, R.layout.lvitem,
                new String[]{"Code", "Name", "Price", "Quantity"},
                new int[]{R.id.tvLvCode, R.id.tvLvName, R.id.tvLvPrice, R.id.tvLvQuantity});

        lvServices.setAdapter(alAdapter);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setEnabled(false);
        btnAdd.setOnClickListener(v -> {
            try {

                if (oService == null) return;

                String Amount, Quantity;

                HashMap<String, String> lvService = new HashMap<>();
                lvService.put("Code", oService.get("Code"));
                lvService.put("Name", oService.get("Name"));
                Amount = etSAmount.getText().toString();
                lvService.put("Price", Amount);
                if (etSQuantity.getText().toString().length() == 0) Quantity = "1";
                else Quantity = etSQuantity.getText().toString();
                lvService.put("Quantity", Quantity);
                ClaimActivity.lvServiceList.add(lvService);

                alAdapter.notifyDataSetChanged();

                etServices.setText("");
                etSAmount.setText("");
                etSQuantity.setText("");

            } catch (Exception e) {
                Log.d("AddLvError", e.getMessage());
            }
        });

        lvServices.setOnItemLongClickListener((parent, view, position, id) -> {
            try {
                Pos = position;
                HideAllDeleteButtons();

                Button d = view.findViewById(R.id.btnDelete);
                d.setVisibility(View.VISIBLE);

                d.setOnClickListener(v -> {
                    ClaimActivity.lvServiceList.remove(Pos);
                    HideAllDeleteButtons();
                    alAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.d("ErrorOnLongClick", e.getMessage());
            }
            return true;
        });


    }

    private void HideAllDeleteButtons() {
        for (int i = 0; i <= lvServices.getLastVisiblePosition(); i++) {
            Button Delete = lvServices.getChildAt(i).findViewById(R.id.btnDelete);
            Delete.setVisibility(View.GONE);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
