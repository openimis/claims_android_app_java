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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class AddServices extends ImisActivity {
    ListView lvServices, listServices;
    TextView tvCode, tvName;
    LinearLayout llSService;
    EditText etSQuantity, etSName, etsSQuantity;
    LinearLayout.LayoutParams layoutParams;
    public static EditText etSAmount;
    public static float amount;
    Button btnAdd;
    AutoCompleteTextView etServices;
    int Pos;
    HashMap<String, String> oService;
    SimpleAdapter alAdapter;
    CustomAdapter ssAdapterServicesItems;
    float sServicePrice;
    public ArrayList<EditModel> editModelArrayListServices;
    public static ArrayList<HashMap<String, String>> lvSServiceList;
    public static ArrayList<HashMap<String, String>> lvSItemList;
    public static String packageType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addservices);

        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.app_name_claim));
        }

        lvSServiceList = new ArrayList<>();
        lvSItemList = new ArrayList<>();

        lvServices = findViewById(R.id.lvServices);
        etSQuantity = findViewById(R.id.etSQuantity);
        etSQuantity.setKeyListener(null);
        etSAmount = findViewById(R.id.etSAmount);
        etSAmount.setKeyListener(null);
        etSName = findViewById(R.id.etSName);
        etSName.setKeyListener(null);
        etServices = findViewById(R.id.etService);
        llSService = findViewById(R.id.llSService);
        layoutParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, 300);
        btnAdd = findViewById(R.id.btnAdd);

        alAdapter = new SimpleAdapter(AddServices.this, ClaimActivity.lvServiceList, R.layout.lvitem,
                new String[]{"Code", "Name", "Price", "Quantity"},
                new int[]{R.id.tvLvCode, R.id.tvLvName, R.id.tvLvPrice, R.id.tvLvQuantity});

        lvServices.setAdapter(alAdapter);

        if (isIntentReadonly()) {
            disableView(etSQuantity);
            disableView(etSAmount);
            disableView(etServices);
            disableView(btnAdd);
        } else {
            ServiceAdapter serviceAdapter = new ServiceAdapter(this, sqlHandler);
            amount = 0;
            etServices.setAdapter(serviceAdapter);
            etServices.setThreshold(1);
            etServices.setOnItemClickListener((parent, view, position, l) -> {
                if (position >= 0) {

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    final int itemColumnIndex = cursor.getColumnIndexOrThrow("Code");
                    final int descColumnIndex = cursor.getColumnIndexOrThrow("Name");
                    String Code = cursor.getString(itemColumnIndex);
                    String Name = cursor.getString(descColumnIndex);
                    packageType = sqlHandler.getPackageType(Code);
                    String id = sqlHandler.getServiceId(Code);

                    oService = new HashMap<>();
                    oService.put("Code", Code);
                    oService.put("Name", Name);
                    oService.put("PackageType", packageType);

                    etSQuantity.setText("1");
                    etSAmount.setText(sqlHandler.getServicePrice(Code));
                    etSName.setText(sqlHandler.getServiceName(Code));
                    if (!packageType.equals("S")) {
                        etSAmount.setText("");
                        sServicePrice = 0;
                        try {
                            JSONArray subServices = sqlHandler.getSubServicesIds(id);
                            JSONArray subServiceArr = new JSONArray();
                            for (int i = 0; i < subServices.length(); i++) {
                                JSONObject objService = sqlHandler.getService(subServices.getJSONObject(i).getString("ServiceId"));
                                objService.put("QuantityMax", subServices.getJSONObject(i).getString("Quantity"));
                                objService.put("Price", subServices.getJSONObject(i).getString("Price"));
                                subServiceArr.put(objService);
                            }
                            JSONArray subItemIds = sqlHandler.getSubItemsId(id);
                            JSONArray subItemArr = new JSONArray();
                            for (int i = 0; i < subItemIds.length(); i++) {
                                JSONObject objItem = sqlHandler.getItem(subItemIds.getJSONObject(i).getString("ItemId"));
                                objItem.put("QuantityMax", subItemIds.getJSONObject(i).getString("Quantity"));
                                objItem.put("Price", subItemIds.getJSONObject(i).getString("Price"));
                                subItemArr.put(objItem);
                            }
                            for (int i = 0; i < subServiceArr.length(); i++) {
                                JSONObject obj = subServiceArr.getJSONObject(i);
                                HashMap<String, String> sService = new HashMap<>();
                                sService.put("Code", obj.getString("Code"));
                                sService.put("Name", obj.getString("Name"));
                                sService.put("Price", obj.getString("Price"));
                                sService.put("Quantity", "0");
                                sService.put("QtyMax", obj.getString("QuantityMax"));
                                lvSServiceList.add(sService);
                            }
                            Log.e("subItems",subItemArr.toString());
                            for (int i = 0; i < subItemArr.length(); i++) {
                                JSONObject obj = subItemArr.getJSONObject(i);
                                HashMap<String, String> sItem = new HashMap<>();
                                sItem.put("Code", obj.getString("Code"));
                                sItem.put("Name", obj.getString("Name"));
                                sItem.put("Price", obj.getString("Price"));
                                sItem.put("Quantity", "0");
                                sItem.put("QtyMax", obj.getString("QuantityMax"));
                                lvSItemList.add(sItem);
                            }
                            editModelArrayListServices = populateListServicesItems();
                            ssAdapterServicesItems = new CustomAdapter(AddServices.this, editModelArrayListServices);
                            TextView textServices = new TextView(AddServices.this);
                            textServices.setText("Sub-Services & Items");
                            textServices.setPadding(0, 0, 0, 10);
                            textServices.setTextSize(18);
                            listServices = new ListView(AddServices.this);
                            if ((lvSServiceList.size() + lvSItemList.size()) > 4) {
                                listServices.setLayoutParams(layoutParams);
                            }
                            listServices.setAdapter(ssAdapterServicesItems);
                            llSService.addView(textServices);
                            llSService.addView(listServices);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        llSService.removeAllViews();
                    }
                }
            });

            etServices.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnAdd.setEnabled(s != null && s.toString().trim().length() != 0
                            && etSQuantity.getText().toString().trim().length() != 0
                            && etSAmount.getText().toString().trim().length() != 0);

                    llSService.removeAllViews();
                    lvSServiceList.clear();
                    lvSItemList.clear();
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

            btnAdd.setEnabled(false);
            btnAdd.setOnClickListener(v -> {
                try {

                    llSService.removeAllViews();

                    if (oService == null) return;

                    String Amount, Quantity;

                    HashMap<String, String> lvService = new HashMap<>();
                    lvService.put("Code", oService.get("Code"));
                    lvService.put("Name", oService.get("Name"));
                    lvService.put("PackageType",oService.get("PackageType"));
                    if (lvSServiceList.size() != 0) {
                        float amount = Float.valueOf(etSAmount.getText().toString());
                        JSONArray subServiceItems = new JSONArray();
                        for (int i = 0; i < CustomAdapter.editModelArrayList.size(); i++) {
                            JSONObject sService = new JSONObject();
                            sService.put("Code", CustomAdapter.editModelArrayList.get(i).getCode());
                            sService.put("Quantity", CustomAdapter.editModelArrayList.get(i).getQty());
                            sService.put("Price", CustomAdapter.editModelArrayList.get(i).getPrice());
                            sService.put("Type", CustomAdapter.editModelArrayList.get(i).getType());
                            subServiceItems.put(sService);
                        }
                        lvService.put("Price", String.valueOf(amount));
                        lvService.put("SubServicesItems", String.valueOf(subServiceItems));
                    } else {
                        Amount = etSAmount.getText().toString();
                        lvService.put("Price", Amount);
                    }
                    if (etSQuantity.getText().toString().length() == 0) Quantity = "1";
                    else Quantity = etSQuantity.getText().toString();
                    lvService.put("Quantity", Quantity);
                    ClaimActivity.lvServiceList.add(lvService);

                    alAdapter.notifyDataSetChanged();

                    etServices.setText("");
                    etSAmount.setText("");
                    etSQuantity.setText("");
                    etSName.setText("");

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
    }

    private boolean isIntentReadonly() {
        Intent intent = getIntent();
        return intent.getBooleanExtra(ClaimActivity.EXTRA_READONLY, false);
    }

    private void HideAllDeleteButtons() {
        for (int i = 0; i <= lvServices.getLastVisiblePosition(); i++) {
            Button Delete = lvServices.getChildAt(i).findViewById(R.id.btnDelete);
            Delete.setVisibility(View.GONE);
        }
    }

    private ArrayList<EditModel> populateListServicesItems() {
        ArrayList<EditModel> list = new ArrayList<>();
        for (int i = 0; i < lvSServiceList.size(); i++) {
            EditModel editModel = new EditModel();
            editModel.setCode(lvSServiceList.get(i).get("Code"));
            editModel.setName(lvSServiceList.get(i).get("Name"));
            editModel.setQty(lvSServiceList.get(i).get("Quantity"));
            editModel.setPrice(lvSServiceList.get(i).get("Price"));
            editModel.setQtyMax(lvSServiceList.get(i).get("QtyMax"));
            editModel.setType("S");
            list.add(editModel);
        }
        for (int i = 0; i < lvSItemList.size(); i++) {
            EditModel editModel = new EditModel();
            editModel.setCode(lvSItemList.get(i).get("Code"));
            editModel.setName(lvSItemList.get(i).get("Name"));
            editModel.setQty(lvSItemList.get(i).get("Quantity"));
            editModel.setPrice(lvSItemList.get(i).get("Price"));
            editModel.setQtyMax(lvSItemList.get(i).get("QtyMax"));
            editModel.setType("I");
            list.add(editModel);
        }
        return list;
    }
}
