package org.openimis.imisclaims;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ClaimActivity extends ImisActivity {
    private static final int REQUEST_QR_SCAN_CODE = 1;
    static final int StartDate_Dialog_ID = 0;
    static final int EndDate_Dialog_ID = 1;

    SQLiteDatabase db;
    public static String Path;

    final Calendar cal = Calendar.getInstance();

    public static ArrayList<HashMap<String, String>> lvItemList;
    public static ArrayList<HashMap<String, String>> lvServiceList;

    private int year, month, day;
    String FileName;
    File ClaimFile;
    int TotalItemService;

    EditText etStartDate, etEndDate, etHealthFacility, etClaimCode, etCHFID, etClaimAdmin, etGuaranteeNo;
    AutoCompleteTextView etDiagnosis, etDiagnosis1, etDiagnosis2, etDiagnosis3, etDiagnosis4;
    TextView tvItemTotal, tvServiceTotal;
    Button btnPost, btnNew;
    RadioButton rbEmergency, rbReferral, rbOther;
    RadioGroup rgVisitType;
    ImageButton btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);

        sqlHandler.onOpen(db);

        actionBar.setTitle(getResources().getString(R.string.app_name_claim));

        Path = global.getMainDirectory();

        isSDCardAvailable();

        if (!global.isNetworkAvailable()) {
            setTitle(getResources().getString(R.string.app_name_claims) + "-" + getResources().getString(R.string.OfflineMode));
            setTitleColor(getResources().getColor(R.color.Red));
        }

        lvItemList = new ArrayList<>();
        lvServiceList = new ArrayList<>();

        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etEndDate = (EditText) findViewById(R.id.etEndDate);
        etDiagnosis = (AutoCompleteTextView) findViewById(R.id.etDiagnosis);
        btnNew = (Button) findViewById(R.id.btnNew);
        btnPost = (Button) findViewById(R.id.btnPost);
        btnScan = (ImageButton) findViewById(R.id.btnScan);
        etHealthFacility = (EditText) findViewById(R.id.etHealthFacility);
        etClaimAdmin = (EditText) findViewById(R.id.etClaimAdmin);
        etGuaranteeNo = (EditText) findViewById(R.id.etGuaranteeNo);
        etClaimCode = (EditText) findViewById(R.id.etClaimCode);
        etCHFID = (EditText) findViewById(R.id.etCHFID);
        tvItemTotal = (TextView) findViewById(R.id.tvItemTotal);
        tvServiceTotal = (TextView) findViewById(R.id.tvServiceTotal);
        etDiagnosis1 = (AutoCompleteTextView) findViewById(R.id.etDiagnosis1);
        etDiagnosis2 = (AutoCompleteTextView) findViewById(R.id.etDiagnosis2);
        etDiagnosis3 = (AutoCompleteTextView) findViewById(R.id.etDiagnosis3);
        etDiagnosis4 = (AutoCompleteTextView) findViewById(R.id.etDiagnosis4);
        rbEmergency = (RadioButton) findViewById(R.id.rbEmergency);
        rbReferral = (RadioButton) findViewById(R.id.rbReferral);
        rbOther = (RadioButton) findViewById(R.id.rbOther);
        rgVisitType = (RadioGroup) findViewById(R.id.rgVisitType);


        tvItemTotal.setText("0");
        tvServiceTotal.setText("0");

        Intent intent = getIntent();
        String claim = intent.getStringExtra("claims");

        if (claim == null) {
            if (sqlHandler.getAdjustibility("ClaimAdministrator").equals("N")) {
                etClaimAdmin.setVisibility(View.GONE);
            } else {
                if (global.getOfficerCode() != null) {
                    etClaimAdmin.setText(global.getOfficerCode());
                }
            }

            if (sqlHandler.getAdjustibility("GuaranteeNo").equals("N")) {
                etGuaranteeNo.setVisibility(View.GONE);
            }

            //Fetch if Healthfacility code is available
            SharedPreferences spHF = global.getDefaultSharedPreferences();
            String HF = spHF.getString("HF", "");
            if (HF.length() > 0) {
                etHealthFacility.setText(HF);
                etClaimAdmin.requestFocus();
            } else {
                etHealthFacility.requestFocus();
            }
        } else {
            try {
                fillForm(new JSONObject(claim));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        DiseaseAdapter adapter = new DiseaseAdapter(ClaimActivity.this, sqlHandler);
        etDiagnosis.setAdapter(adapter);
        etDiagnosis.setThreshold(1);
        etDiagnosis.setOnItemClickListener(adapter);

        etDiagnosis1.setAdapter(adapter);
        etDiagnosis1.setThreshold(1);
        etDiagnosis1.setOnItemClickListener(adapter);

        etDiagnosis2.setAdapter(adapter);
        etDiagnosis2.setThreshold(1);
        etDiagnosis2.setOnItemClickListener(adapter);

        etDiagnosis3.setAdapter(adapter);
        etDiagnosis3.setThreshold(1);
        etDiagnosis3.setOnItemClickListener(adapter);

        etDiagnosis4.setAdapter(adapter);
        etDiagnosis4.setThreshold(1);
        etDiagnosis4.setOnItemClickListener(adapter);

        etStartDate.setOnTouchListener((v, event) -> {
            showDialog(StartDate_Dialog_ID);
            return false;
        });

        etEndDate.setOnTouchListener((v, event) -> {
            showDialog(EndDate_Dialog_ID);
            return false;
        });


        btnNew.setOnClickListener(v -> {
            if (TotalItemService > 0) {
                ConfirmDialog(getResources().getString(R.string.ConfirmDiscard));
            } else {
                ClearForm();
            }
        });

        btnScan.setOnClickListener(v -> {
            Intent scanIntent = new Intent(this, com.google.zxing.client.android.CaptureActivity.class);
            scanIntent.setAction("com.google.zxing.client.android.SCAN");
            scanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(scanIntent, 1);
        });

        btnPost.setOnClickListener(v -> {
            if (!isValidData()) return;
            WriteJSON();
            WriteXML();
            ClearForm();
            ShowDialog(getResources().getString(R.string.ClaimPosted));
        });
    }

    private void isSDCardAvailable() {
        String status = global.getSDCardStatus();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            //Toast.makeText(this, "SD Card is in read only mode.", Toast.LENGTH_LONG);
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardReadOnly))
                    .setCancelable(false)
                    .setPositiveButton("Force close", (dialog, which) -> finish()).show();

        } else if (!Environment.MEDIA_MOUNTED.equals(status)) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.SDCardMissing))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), (dialog, which) -> finish()).create().show();
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mif = getMenuInflater();
        mif.inflate(R.menu.menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuAddItems:
                Intent AddItems = new Intent(ClaimActivity.this, AddItems.class);
                ClaimActivity.this.startActivity(AddItems);
                return true;
            case R.id.mnuAddServices:
                Intent AddServices = new Intent(ClaimActivity.this, AddServices.class);
                ClaimActivity.this.startActivity(AddServices);
                return true;
            default:
                onBackPressed();
                return true;
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case StartDate_Dialog_ID:

                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, StartdatePickerListener, year, month, day);

            case EndDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, EndDatePickerListner, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener StartdatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth, int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;
            Date date = new Date(year - 1900, month, day);
            etStartDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date));

            if (etEndDate.getText().length() == 0) {
                etEndDate.setText(etStartDate.getText().toString());
            }
        }
    };

    private DatePickerDialog.OnDateSetListener EndDatePickerListner = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;
            Date date = new Date(year - 1900, month, day);
            etEndDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date));
        }
    };


    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();

        int TotalItem = getTotalItem();
        int TotalService = getTotalService();
        TotalItemService = TotalItem + TotalService;

        tvItemTotal.setText(String.valueOf(TotalItem));
        tvServiceTotal.setText(String.valueOf(TotalService));

    }

    private void ClearForm() {
        etClaimCode.setText("");
        etGuaranteeNo.setText("");
        etCHFID.setText("");
        etStartDate.setText("");
        etEndDate.setText("");
        etDiagnosis.setText("");
        lvItemList.clear();
        lvServiceList.clear();
        tvItemTotal.setText("0");
        tvServiceTotal.setText("0");
        TotalItemService = 0;
        etDiagnosis1.setText("");
        etDiagnosis2.setText("");
        etDiagnosis3.setText("");
        etDiagnosis4.setText("");
        rgVisitType.clearCheck();
        etClaimAdmin.requestFocus();
    }

    private void fillForm(JSONObject obj) {
        try {
            String newClaimNumber = getResources().getString(R.string.restoredClaimNoPrefix) + obj.getString("claim_number");
            etClaimCode.setText(newClaimNumber);

            etHealthFacility.setText(obj.getString("health_facility_code"));
            etClaimAdmin.setText(global.getOfficerCode());

            String guaranteeNumber = obj.getString("guarantee_number");
            if (null == guaranteeNumber || "null".equals(guaranteeNumber))
                etGuaranteeNo.setText("");
            else etGuaranteeNo.setText(guaranteeNumber);

            etCHFID.setText(obj.getString("insurance_number"));
            if (!obj.getString("claim_status").equals("Rejected"))
                etCHFID.setText("");

            etStartDate.setText(obj.getString("visit_date_from"));
            etEndDate.setText(obj.getString("visit_date_to"));

            etDiagnosis.setText(sqlHandler.getDiseaseCode(obj.getString("main_dg")));
            etDiagnosis1.setText(sqlHandler.getDiseaseCode(obj.getString("sec_dg_1")));
            etDiagnosis2.setText(sqlHandler.getDiseaseCode(obj.getString("sec_dg_2")));
            etDiagnosis3.setText(sqlHandler.getDiseaseCode(obj.getString("sec_dg_3")));
            etDiagnosis4.setText(sqlHandler.getDiseaseCode(obj.getString("sec_dg_4")));

            switch (obj.getString("visit_type")) {
                case "Emergency":
                    rgVisitType.check(R.id.rbEmergency);
                    break;
                case "Referral":
                    rgVisitType.check(R.id.rbReferral);
                    break;
                case "Other":
                    rgVisitType.check(R.id.rbOther);
                    break;
                default:
                    rgVisitType.clearCheck();
            }

            lvItemList.clear();
            if (obj.has("items")) {
                JSONArray items = obj.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    HashMap<String, String> item = new HashMap<>();
                    JSONObject itemJson = items.getJSONObject(i);

                    item.put("Name", itemJson.getString("item"));
                    item.put("Code", itemJson.getString("item_code"));
                    item.put("Price", itemJson.getString("item_price"));
                    item.put("Quantity", itemJson.getString("item_qty"));

                    lvItemList.add(item);
                }
            }
            tvItemTotal.setText(String.valueOf(lvItemList.size()));

            lvServiceList.clear();
            if (obj.has("services")) {
                JSONArray services = obj.getJSONArray("services");
                for (int i = 0; i < services.length(); i++) {
                    HashMap<String, String> service = new HashMap<>();
                    JSONObject serviceJson = services.getJSONObject(i);

                    service.put("Name", serviceJson.getString("service"));
                    service.put("Code", serviceJson.getString("service_code"));
                    service.put("Price", serviceJson.getString("service_price"));
                    service.put("Quantity", serviceJson.getString("service_qty"));

                    lvServiceList.add(service);
                }
            }
            tvServiceTotal.setText(String.valueOf(lvServiceList.size()));

            TotalItemService = lvItemList.size() + lvServiceList.size();

            etCHFID.requestFocus();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getTotalItem() {
        return lvItemList.size();
    }

    private int getTotalService() {
        return lvServiceList.size();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_QR_SCAN_CODE:
                if (resultCode == RESULT_OK) {
                    String CHFID = data.getStringExtra("SCAN_RESULT");
                    etCHFID.setText(CHFID);
                }
                break;
        }
    }

    private boolean isValidData() {

        if (etHealthFacility.getText().length() == 0) {
            ShowDialog(etHealthFacility, getResources().getString(R.string.MissingHealthFacility));
            return false;
        }
        if (sqlHandler.getAdjustibility("ClaimAdministrator").equals("M")) {
            if (etClaimAdmin.getText().length() == 0) {
                ShowDialog(etClaimAdmin, getResources().getString(R.string.MissingClaimAdmin));
                return false;
            }
        }

        if (etClaimCode.getText().length() == 0) {
            ShowDialog(etClaimCode, getResources().getString(R.string.MissingClaimCode));
            return false;
        }

        if (etCHFID.getText().length() == 0) {
            ShowDialog(etCHFID, getResources().getString(R.string.MissingCHFID));
            return false;
        }

        if (!isValidCHFID()) {
            ShowDialog(etCHFID, getResources().getString(R.string.InvalidCHFID));
            return false;
        }

        if (etStartDate.getText().length() == 0) {
            ShowDialog(etStartDate, getResources().getString(R.string.MissingStartDate));
            return false;
        }


        if (etEndDate.getText().length() == 0) {
            ShowDialog(etEndDate, getResources().getString(R.string.MissingEndDate));
            return false;
        }

        String StartDate;
        String EndDate;
        String CurrentDate1;
        String pattern = "yyyy-MM-dd";

        Date Current_date = null;
        Date Start_date = null;
        Date End_date = null;

        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);

        CurrentDate1 = format.format(new Date());
        StartDate = etStartDate.getText().toString();
        EndDate = etEndDate.getText().toString();

        try {
            Current_date = format.parse(CurrentDate1);
            Start_date = format.parse(StartDate);
            End_date = format.parse(EndDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (End_date.after(Current_date)) {
            ShowDialog(etEndDate, getResources().getString(R.string.AfterCurrentDate));
            return false;
        } else {
            if (Start_date.after(End_date)) {
                ShowDialog(etEndDate, getResources().getString(R.string.BiggerDate));
                return false;
            }
            if (etDiagnosis.getText().length() == 0) {
                ShowDialog(etDiagnosis, getResources().getString(R.string.MissingDisease));
                return false;
            }
            if (rgVisitType.getCheckedRadioButtonId() == -1) {
                ShowDialog(rgVisitType, getResources().getString(R.string.MissingVisitType));
                return false;
            }
            //if(tvTotal.getText().length() == 0) tvTotal.setText("0");
            if (Float.parseFloat(tvItemTotal.getText().toString()) + Float.parseFloat(tvServiceTotal.getText().toString()) == 0) {
                ShowDialog(tvItemTotal, getResources().getString(R.string.MissingClaim));
                return false;
            }
            return true;
        }
    }

    private boolean isValidCHFID() {
        Escape escape = new Escape();
        return escape.CheckCHFID(etCHFID.getText().toString());
    }

    protected AlertDialog ShowDialog(final Object tv, String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, (dialog, which) -> {
                    if (tv instanceof EditText) {
                        EditText temp = (EditText) tv;
                        temp.requestFocus();
                    }
                }).show();
    }

    protected AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok), (dialog, which) -> {
                }).show();
    }

    protected AlertDialog ConfirmDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.Yes), (dialog, which) -> ClearForm())
                .setNegativeButton(getResources().getString(R.string.No), (dialog, which) -> dialog.dismiss()).show();

    }

    private void WriteXML() {
        File MyDir = new File(Path);

        //Create a file name
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        FileName = "Claim_" + etHealthFacility.getText().toString() + "_" + etClaimCode.getText().toString() + "_" + d + ".xml";

        ClaimFile = new File(MyDir, FileName);

        //Get the selected radio button
        int SelectedId;
        SelectedId = rgVisitType.getCheckedRadioButtonId();

        RadioButton Rb;
        Rb = (RadioButton) findViewById(SelectedId);

        try {
            FileOutputStream fos = new FileOutputStream(ClaimFile);

            XmlSerializer serializer = Xml.newSerializer();

            serializer.setOutput(fos, "UTF-8");

            serializer.startDocument(null, true);

            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            //<Claim>
            serializer.startTag(null, "Claim");

            //<Details>
            serializer.startTag(null, "Details");

            //ClaimDate
            serializer.startTag(null, "ClaimDate");
            format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            d = format.format(cal.getTime());
            serializer.text(d);
            serializer.endTag(null, "ClaimDate");

            //HFCOde
            serializer.startTag(null, "HFCode");
            serializer.text(etHealthFacility.getText().toString());
            serializer.endTag(null, "HFCode");

            //Claim Admin
            serializer.startTag(null, "ClaimAdmin");
            serializer.text(etClaimAdmin.getText().toString());
            serializer.endTag(null, "ClaimAdmin");

            //ClaimCode
            serializer.startTag(null, "ClaimCode");
            serializer.text(etClaimCode.getText().toString());
            serializer.endTag(null, "ClaimCode");

            //GuaranteeNo
            serializer.startTag(null, "GuaranteeNo");
            serializer.text(etGuaranteeNo.getText().toString());
            serializer.endTag(null, "GuaranteeNo");

            //CHFID
            serializer.startTag(null, "CHFID");
            serializer.text(etCHFID.getText().toString());
            serializer.endTag(null, "CHFID");

            //StartDate
            serializer.startTag(null, "StartDate");
            serializer.text(etStartDate.getText().toString());
            serializer.endTag(null, "StartDate");

            //EndDate
            serializer.startTag(null, "EndDate");
            serializer.text(etEndDate.getText().toString());
            serializer.endTag(null, "EndDate");

            //ICDCode
            serializer.startTag(null, "ICDCode");
            serializer.text(etDiagnosis.getText().toString());
            serializer.endTag(null, "ICDCode");

            //Comment
            serializer.startTag(null, "Comment");
            serializer.text(" ");
            serializer.endTag(null, "Comment");

            //Total
            serializer.startTag(null, "Total");
            serializer.text(" ");
            serializer.endTag(null, "Total");

            //Diagnosis1
            serializer.startTag(null, "ICDCode1");
            serializer.text(etDiagnosis1.getText().toString());
            serializer.endTag(null, "ICDCode1");

            //Diagnosis2
            serializer.startTag(null, "ICDCode2");
            serializer.text(etDiagnosis2.getText().toString());
            serializer.endTag(null, "ICDCode2");

            //Diagnosis3
            serializer.startTag(null, "ICDCode3");
            serializer.text(etDiagnosis3.getText().toString());
            serializer.endTag(null, "ICDCode3");

            //Diagnosis4
            serializer.startTag(null, "ICDCode4");
            serializer.text(etDiagnosis4.getText().toString());
            serializer.endTag(null, "ICDCode4");

            //VisitType
            serializer.startTag(null, "VisitType");
            serializer.text(Rb.getTag().toString());
            serializer.endTag(null, "VisitType");


            serializer.endTag(null, "Details");
            //</Details>

            //<Items>
            serializer.startTag(null, "Items");

            for (int i = 0; i < lvItemList.size(); i++) {
                //<Item>
                serializer.startTag(null, "Item");

                //Code
                serializer.startTag(null, "ItemCode");
                serializer.text(lvItemList.get(i).get("Code"));
                serializer.endTag(null, "ItemCode");

                //Price
                serializer.startTag(null, "ItemPrice");
                serializer.text(lvItemList.get(i).get("Price"));
                serializer.endTag(null, "ItemPrice");

                //Quantity
                serializer.startTag(null, "ItemQuantity");
                serializer.text(lvItemList.get(i).get("Quantity"));
                serializer.endTag(null, "ItemQuantity");

                serializer.endTag(null, "Item");
                //</Item>
            }

            serializer.endTag(null, "Items");
            //</Items>


            //<Services>
            serializer.startTag(null, "Services");

            for (int i = 0; i < lvServiceList.size(); i++) {

                //<Service>
                serializer.startTag(null, "Service");

                //Code
                serializer.startTag(null, "ServiceCode");
                serializer.text(lvServiceList.get(i).get("Code"));
                serializer.endTag(null, "ServiceCode");

                //Price
                serializer.startTag(null, "ServicePrice");
                serializer.text(lvServiceList.get(i).get("Price"));
                serializer.endTag(null, "ServicePrice");

                //Quantity
                serializer.startTag(null, "ServiceQuantity");
                serializer.text(lvServiceList.get(i).get("Quantity"));
                serializer.endTag(null, "ServiceQuantity");

                //<Service>
                serializer.endTag(null, "Service");
            }

            serializer.endTag(null, "Services");
            //</Services>

            serializer.endTag(null, "Claim");
            //</Claim>

            serializer.endDocument();
            serializer.flush();
            fos.flush();
            fos.close();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void WriteJSON() {
        File MyDir = new File(Path);

        //Create a file name
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        FileName = "ClaimJSON_" + etHealthFacility.getText().toString() + "_" + etClaimCode.getText().toString() + "_" + d + ".txt";
        ClaimFile = new File(MyDir, FileName);

        //Get the selected radio button
        int SelectedId;
        SelectedId = rgVisitType.getCheckedRadioButtonId();

        RadioButton Rb;
        Rb = (RadioButton) findViewById(SelectedId);

        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject ClaimObject = new JSONObject();

            d = format.format(cal.getTime());

            ClaimObject.put("ClaimDate", d);
            ClaimObject.put("HFCode", etHealthFacility.getText().toString());
            ClaimObject.put("ClaimAdmin", etClaimAdmin.getText().toString());
            ClaimObject.put("ClaimCode", etClaimCode.getText().toString());
            ClaimObject.put("GuaranteeNo", etGuaranteeNo.getText().toString());
            ClaimObject.put("CHFID", etCHFID.getText().toString());
            ClaimObject.put("StartDate", etStartDate.getText().toString());
            ClaimObject.put("EndDate", etEndDate.getText().toString());
            ClaimObject.put("ICDCode", etDiagnosis.getText().toString());
            ClaimObject.put("Comment", "");
            ClaimObject.put("Total", "");
            ClaimObject.put("ICDCode1", etDiagnosis1.getText().toString());
            ClaimObject.put("ICDCode2", etDiagnosis2.getText().toString());
            ClaimObject.put("ICDCode3", etDiagnosis3.getText().toString());
            ClaimObject.put("ICDCode4", etDiagnosis4.getText().toString());
            ClaimObject.put("VisitType", Rb.getTag().toString());

            jsonObject.put("details", ClaimObject);

            JSONArray ItemsArray = new JSONArray();
            for (int i = 0; i < lvItemList.size(); i++) {
                JSONObject ItemObject = new JSONObject();
                ItemObject.put("ItemCode", lvItemList.get(i).get("Code"));
                ItemObject.put("ItemPrice", lvItemList.get(i).get("Price"));
                ItemObject.put("ItemQuantity", lvItemList.get(i).get("Quantity"));

                ItemsArray.put(ItemObject);
            }
            jsonObject.put("items", ItemsArray);


            JSONArray ServicesArray = new JSONArray();
            for (int i = 0; i < lvServiceList.size(); i++) {
                JSONObject ServiceObject = new JSONObject();
                ServiceObject.put("ServiceCode", lvServiceList.get(i).get("Code"));
                ServiceObject.put("ServicePrice", lvServiceList.get(i).get("Price"));
                ServiceObject.put("ServiceQuantity", lvServiceList.get(i).get("Quantity"));

                ServicesArray.put(ServiceObject);
            }
            jsonObject.put("services", ServicesArray);

            try {
                FileOutputStream fOut = new FileOutputStream(ClaimFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(jsonObject.toString());
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IllegalStateException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences HF = global.getDefaultSharedPreferences();
        SharedPreferences.Editor editor = HF.edit();
        editor.putString("HF", etHealthFacility.getText().toString());
        editor.apply();
    }
}
