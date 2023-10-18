package org.openimis.imisclaims;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.util.DateUtils;
import org.openimis.imisclaims.util.TextViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ClaimActivity extends ImisActivity {
    private static final String LOG_TAG = "CLAIM";
    private static final int REQUEST_SCAN_QR_CODE = 1;
    static final int StartDate_Dialog_ID = 0;
    static final int EndDate_Dialog_ID = 1;

    final Calendar cal = Calendar.getInstance();

    public static ArrayList<HashMap<String, String>> lvItemList;
    public static ArrayList<HashMap<String, String>> lvServiceList;
    private static final String EXTRA_CLAIM_DATA = "claim";
    private static final String EXTRA_CLAIM_UUID = "claimUUID";
    public static final String EXTRA_READONLY = "readonly";

    public static Intent newIntent(@NonNull Context context, @NonNull Claim claim) {
        return new Intent(context, ClaimActivity.class).putExtra(EXTRA_CLAIM_DATA, claim);
    }

    public static Intent newIntent(@NonNull Context context, @NonNull String claimUUID, boolean readOnly) {
        return new Intent(context, ClaimActivity.class)
                .putExtra(EXTRA_CLAIM_UUID, claimUUID)
                .putExtra(EXTRA_READONLY, readOnly);
    }


    private int year, month, day;
    int TotalItemService;

    EditText etStartDate, etEndDate, etClaimCode, etHealthFacility, etInsureeNumber, etClaimAdmin, etGuaranteeNo;
    AutoCompleteTextView etDiagnosis, etDiagnosis1, etDiagnosis2, etDiagnosis3, etDiagnosis4;
    TextView tvItemTotal, tvServiceTotal;
    Button btnPost, btnNew;
    RadioGroup rgVisitType;
    RadioButton rbEmergency, rbReferral, rbOther;
    ImageButton btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        actionBar.setTitle(getResources().getString(R.string.app_name_claim));

        if (!global.isNetworkAvailable()) {
            setTitle(getResources().getString(R.string.app_name_claims) + "-" + getResources().getString(R.string.OfflineMode));
            setTitleColor(getResources().getColor(R.color.Red));
        }

        lvItemList = new ArrayList<>();
        lvServiceList = new ArrayList<>();

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        btnNew = findViewById(R.id.btnNew);
        btnPost = findViewById(R.id.btnPost);
        btnScan = findViewById(R.id.btnScan);
        etHealthFacility = findViewById(R.id.etHealthFacility);
        etClaimAdmin = findViewById(R.id.etClaimAdmin);
        etGuaranteeNo = findViewById(R.id.etGuaranteeNo);
        etClaimCode = findViewById(R.id.etClaimCode);
        etInsureeNumber = findViewById(R.id.etCHFID);
        tvItemTotal = findViewById(R.id.tvItemTotal);
        tvServiceTotal = findViewById(R.id.tvServiceTotal);
        etDiagnosis1 = findViewById(R.id.etDiagnosis1);
        etDiagnosis2 = findViewById(R.id.etDiagnosis2);
        etDiagnosis3 = findViewById(R.id.etDiagnosis3);
        etDiagnosis4 = findViewById(R.id.etDiagnosis4);
        rgVisitType = findViewById(R.id.rgVisitType);
        rbEmergency = findViewById(R.id.rbEmergency);
        rbReferral = findViewById(R.id.rbReferral);
        rbOther = findViewById(R.id.rbOther);


        tvItemTotal.setText("0");
        tvServiceTotal.setText("0");

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

        findViewById(R.id.ivAddItem).setOnClickListener(v -> addItem());
        findViewById(R.id.ivAddService).setOnClickListener(v -> addService());

        btnScan.setOnClickListener(v -> {
            Intent scanIntent = new Intent(this, com.google.zxing.client.android.CaptureActivity.class);
            scanIntent.setAction("com.google.zxing.client.android.SCAN");
            scanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(scanIntent, REQUEST_SCAN_QR_CODE);
        });

        btnPost.setOnClickListener(v -> {
            progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.Processing));
            runOnNewThread(
                    () -> isValidData() && saveClaim(),
                    () -> runOnUiThread(() -> {
                        ClearForm();
                        progressDialog.dismiss();
                        showDialog(getResources().getString(R.string.ClaimPosted), ((dialog, which) -> {
                            Intent intent = getIntent();
                            if (intent.hasExtra(EXTRA_CLAIM_UUID)) {
                                finish();
                            }
                        }));
                    }),
                    () -> progressDialog.dismiss(),
                    500
            );
        });

        if (sqlHandler.getAdjustability("GuaranteeNo").equals("N")) {
            etGuaranteeNo.setVisibility(View.GONE);
        }
        if (sqlHandler.getAdjustability("ClaimAdministrator").equals("N")) {
            etClaimAdmin.setVisibility(View.GONE);
        }

        // hfCode and adminCode not editable
        disableView(etHealthFacility);
        disableView(etClaimAdmin);

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_CLAIM_DATA)) {
            fillClaimFromRestore(intent.getParcelableExtra(EXTRA_CLAIM_DATA));
            btnNew.setVisibility(View.INVISIBLE);
        } else if (intent.hasExtra(EXTRA_CLAIM_UUID)) {
            fillClaimFromDatabase(intent.getStringExtra(EXTRA_CLAIM_UUID));

            if (isIntentReadonly()) {
                disableForm();
                btnNew.setText(R.string.ArchiveClaim);
                btnNew.setOnClickListener(v -> confirmArchive());
            } else {
                btnNew.setText(R.string.DeleteClaim);
                btnNew.setOnClickListener(v -> confirmDelete());
            }
        } else {
            if (global.getOfficerCode() != null) {
                etClaimAdmin.setText(global.getOfficerCode());
                etHealthFacility.setText(global.getOfficerHealthFacility());
            }
            btnNew.setOnClickListener(v -> {
                if (TotalItemService > 0) {
                    confirmNewDialog(getResources().getString(R.string.ConfirmDiscard));
                } else {
                    ClearForm();
                }
            });
        }
    }

    private boolean isIntentReadonly() {
        Intent intent = getIntent();
        return intent.getBooleanExtra(EXTRA_READONLY, false);
    }

    private void confirmDelete() {
        showDialog(getResources().getString(R.string.ConfirmDeleteClaim), (dialog, which) -> {
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.Processing), getResources().getString(R.string.DeleteClaim));
            Intent intent = getIntent();
            if (intent.hasExtra(EXTRA_CLAIM_UUID)) {
                runOnNewThread(() -> sqlHandler.deleteClaim(intent.getStringExtra(EXTRA_CLAIM_UUID)), () -> {
                    progressDialog.dismiss();
                    runOnUiThread(this::finish);
                }, 500);
            } else {
                Log.e(LOG_TAG, "Delete claim invoked, but no claim UUID");
            }
        });
    }

    private void confirmArchive() {
        showDialog(getResources().getString(R.string.ConfirmArchiveClaim), (dialog, which) -> {
            progressDialog = ProgressDialog.show(this, getResources().getString(R.string.Processing), getResources().getString(R.string.ArchiveClaim));
            Intent intent = getIntent();
            if (intent.hasExtra(EXTRA_CLAIM_UUID)) {
                runOnNewThread(() -> {
                    String date = AppInformation.DateTimeInfo.getDefaultIsoDatetimeFormatter().format(new Date());
                    sqlHandler.insertClaimUploadStatus(intent.getStringExtra(EXTRA_CLAIM_UUID), date, SQLHandler.CLAIM_UPLOAD_STATUS_ARCHIVED, null);
                }, () -> {
                    progressDialog.dismiss();
                    runOnUiThread(this::finish);
                }, 500);
            } else {
                Log.e(LOG_TAG, "Archive claim invoked, but no claim UUID");
            }
        });
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
                addItem();
                return true;
            case R.id.mnuAddServices:
                addService();
                return true;
            default:
                onBackPressed();
                return true;
        }
    }

    private void addItem() {
        Intent addItemsIntent = new Intent(ClaimActivity.this, AddItems.class);
        addItemsIntent.putExtra(EXTRA_READONLY, isIntentReadonly());
        ClaimActivity.this.startActivity(addItemsIntent);
    }

    private  void addService() {
        Intent addServicesIntent = new Intent(this, AddServices.class);
        addServicesIntent.putExtra(EXTRA_READONLY, isIntentReadonly());
        ClaimActivity.this.startActivity(addServicesIntent);
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

    private final DatePickerDialog.OnDateSetListener StartdatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth, int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;
            Date date = new Date(year - 1900, month, day);
            TextViewUtils.setDate(etStartDate, date);

            if (etEndDate.getText().length() == 0) {
                etEndDate.setText(etStartDate.getText().toString());
            }
        }
    };

    private final DatePickerDialog.OnDateSetListener EndDatePickerListner = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;
            Date date = new Date(year - 1900, month, day);
            TextViewUtils.setDate(etEndDate, date);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        int TotalItem = getTotalItem();
        int TotalService = getTotalService();
        TotalItemService = TotalItem + TotalService;

        tvItemTotal.setText(String.valueOf(TotalItem));
        tvServiceTotal.setText(String.valueOf(TotalService));
    }

    private void ClearForm() {
        etClaimCode.setText("");
        etGuaranteeNo.setText("");
        etInsureeNumber.setText("");
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
        etClaimCode.requestFocus();
    }

    private void disableForm() {
        disableView(etClaimCode);
        disableView(etGuaranteeNo);
        disableView(etInsureeNumber);
        disableView(etStartDate);
        disableView(etEndDate);
        disableView(etDiagnosis);
        disableView(etDiagnosis1);
        disableView(etDiagnosis2);
        disableView(etDiagnosis3);
        disableView(etDiagnosis4);
        disableView(rgVisitType);
        disableView(etClaimCode);
        disableView(btnPost);
        disableView(rbEmergency);
        disableView(rbReferral);
        disableView(rbOther);
    }

    private void fillClaimFromRestore(Claim claim) {
        String newClaimNumber = getResources().getString(R.string.restoredClaimNoPrefix) + claim.getClaimNumber();
        etClaimCode.setText(newClaimNumber);

        if (etClaimAdmin.getVisibility() != View.GONE) {
            etClaimAdmin.setText(global.getOfficerCode());
        }
        etHealthFacility.setText(global.getOfficerHealthFacility());

        if (etGuaranteeNo.getVisibility() != View.GONE) {
            String guaranteeNumber = claim.getGuaranteeNumber();
            if ("".equals(guaranteeNumber) || "null".equals(guaranteeNumber))
                etGuaranteeNo.setText("");
            else etGuaranteeNo.setText(guaranteeNumber);
        }

        etInsureeNumber.setText(claim.getInsuranceNumber());
        if (Claim.Status.REJECTED != claim.getStatus()) {
            etInsureeNumber.setText("");
        }

        TextViewUtils.setDate(etStartDate, claim.getVisitDateFrom());
        TextViewUtils.setDate(etEndDate, claim.getVisitDateTo());

        etDiagnosis.setText(sqlHandler.getDiseaseCode(claim.getMainDg()));
        etDiagnosis1.setText(sqlHandler.getDiseaseCode(claim.getSecDg1()));
        etDiagnosis2.setText(sqlHandler.getDiseaseCode(claim.getSecDg2()));
        etDiagnosis3.setText(sqlHandler.getDiseaseCode(claim.getSecDg3()));
        etDiagnosis4.setText(sqlHandler.getDiseaseCode(claim.getSecDg4()));

        switch (claim.getVisitType() != null ? claim.getVisitType() : "") {
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
        for (Claim.Medication medication : claim.getMedications()) {
            HashMap<String, String> item = new HashMap<>();
            item.put("Name", medication.getName());
            item.put("Code", medication.getCode());
            item.put("Price", String.valueOf(medication.getPrice()));
            item.put("Quantity", medication.getQuantity());
            lvItemList.add(item);
        }

        tvItemTotal.setText(String.valueOf(lvItemList.size()));

        lvServiceList.clear();
        for (Claim.Service service : claim.getServices()) {
            HashMap<String, String> item = new HashMap<>();
            item.put("Name", service.getName());
            item.put("Code", service.getCode());
            item.put("Price", String.valueOf(service.getPrice()));
            item.put("Quantity", service.getQuantity());
            lvServiceList.add(item);
        }
        tvServiceTotal.setText(String.valueOf(lvServiceList.size()));

        TotalItemService = lvItemList.size() + lvServiceList.size();

        etInsureeNumber.requestFocus();

    }

    private void fillClaimFromDatabase(String claimUUID) {
        new Thread(() -> {
            JSONObject claimObject = sqlHandler.getClaim(claimUUID);
            if (claimObject == null) {
                showDialog(getResources().getString(R.string.ClaimNotFound), (dialog, which) -> finish());
            } else {
                runOnUiThread(() -> {
                    try {
                        JSONObject claimDetails = claimObject.getJSONObject("details");

                        etClaimCode.setText(claimDetails.getString("ClaimCode"));
                        if (etClaimAdmin.getVisibility() != View.GONE) {
                            etClaimAdmin.setText(claimDetails.getString("ClaimAdmin"));
                        }
                        etHealthFacility.setText(claimDetails.getString("HFCode"));

                        if (etGuaranteeNo.getVisibility() != View.GONE) {
                            etGuaranteeNo.setText(claimDetails.getString("GuaranteeNumber"));
                        }

                        etInsureeNumber.setText(claimDetails.getString("InsureeNumber"));
                        etStartDate.setText(claimDetails.getString("StartDate"));
                        etEndDate.setText(claimDetails.getString("EndDate"));

                        etDiagnosis.setText(claimDetails.getString("ICDCode"));
                        etDiagnosis1.setText(claimDetails.getString("ICDCode1"));
                        etDiagnosis2.setText(claimDetails.getString("ICDCode2"));
                        etDiagnosis3.setText(claimDetails.getString("ICDCode3"));
                        etDiagnosis4.setText(claimDetails.getString("ICDCode4"));

                        switch (claimDetails.getString("VisitType")) {
                            case "E":
                                rgVisitType.check(R.id.rbEmergency);
                                break;
                            case "R":
                                rgVisitType.check(R.id.rbReferral);
                                break;
                            case "O":
                                rgVisitType.check(R.id.rbOther);
                                break;
                            default:
                                rgVisitType.clearCheck();
                        }

                        lvItemList.clear();
                        if (claimObject.has("items")) {
                            JSONArray items = claimObject.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                HashMap<String, String> item = new HashMap<>();
                                JSONObject itemJson = items.getJSONObject(i);

                                item.put("Name", sqlHandler.getReferenceName(itemJson.getString("ItemCode")));
                                item.put("Code", itemJson.getString("ItemCode"));
                                item.put("Price", itemJson.getString("ItemPrice"));
                                item.put("Quantity", itemJson.getString("ItemQuantity"));

                                lvItemList.add(item);
                            }
                        }
                        tvItemTotal.setText(String.valueOf(lvItemList.size()));

                        lvServiceList.clear();
                        if (claimObject.has("services")) {
                            JSONArray services = claimObject.getJSONArray("services");
                            for (int i = 0; i < services.length(); i++) {
                                HashMap<String, String> service = new HashMap<>();
                                JSONObject serviceJson = services.getJSONObject(i);

                                service.put("Name", sqlHandler.getReferenceName(serviceJson.getString("ServiceCode")));
                                service.put("Code", serviceJson.getString("ServiceCode"));
                                service.put("Price", serviceJson.getString("ServicePrice"));
                                service.put("Quantity", serviceJson.getString("ServiceQuantity"));

                                lvServiceList.add(service);
                            }
                        }
                        tvServiceTotal.setText(String.valueOf(lvServiceList.size()));

                        TotalItemService = lvItemList.size() + lvServiceList.size();
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, String.format("Error while parsing claim (%s)", claimUUID));
                    }
                });
            }
        }).start();
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
            case REQUEST_SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    String CHFID = data.getStringExtra("SCAN_RESULT");
                    etInsureeNumber.setText(CHFID);
                }
                break;
        }
    }

    private boolean isValidData() {

        if (etHealthFacility.getText().length() == 0) {
            showValidationDialog(etHealthFacility, getResources().getString(R.string.MissingHealthFacility));
            return false;
        }

        if (sqlHandler.getAdjustability("ClaimAdministrator").equals("M") && etClaimAdmin.getText().length() == 0) {
            showValidationDialog(etClaimAdmin, getResources().getString(R.string.MissingClaimAdmin));
            return false;
        }

        if (etClaimCode.getText().length() == 0) {
            showValidationDialog(etClaimCode, getResources().getString(R.string.MissingClaimCode));
            return false;
        }

        if (etInsureeNumber.getText().length() == 0) {
            showValidationDialog(etInsureeNumber, getResources().getString(R.string.MissingCHFID));
            return false;
        }

        if (!isValidInsureeNumber()) {
            showValidationDialog(etInsureeNumber, getResources().getString(R.string.InvalidCHFID));
            return false;
        }

        if (etStartDate.getText().length() == 0) {
            showValidationDialog(etStartDate, getResources().getString(R.string.MissingStartDate));
            return false;
        }

        if (etEndDate.getText().length() == 0) {
            showValidationDialog(etEndDate, getResources().getString(R.string.MissingEndDate));
            return false;
        }

        try {
            String StartDate = etStartDate.getText().toString();
            String EndDate = etEndDate.getText().toString();

            Date Current_date = new Date();
            Date Start_date = DateUtils.dateFromString(StartDate);
            Date End_date = DateUtils.dateFromString(EndDate);

            if (End_date.after(Current_date)) {
                showValidationDialog(etEndDate, getResources().getString(R.string.AfterCurrentDate));
                return false;
            }

            if (Start_date.after(End_date)) {
                showValidationDialog(etEndDate, getResources().getString(R.string.BiggerDate));
                return false;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while parsing dates", e);
        }

        if (etDiagnosis.getText().length() == 0) {
            showValidationDialog(etDiagnosis, getResources().getString(R.string.MissingDisease));
            return false;
        }

        if (rgVisitType.getCheckedRadioButtonId() == -1) {
            showValidationDialog(rgVisitType, getResources().getString(R.string.MissingVisitType));
            return false;
        }

        if (Float.parseFloat(tvItemTotal.getText().toString()) + Float.parseFloat(tvServiceTotal.getText().toString()) == 0) {
            showValidationDialog(tvItemTotal, getResources().getString(R.string.MissingClaim));
            return false;
        }

        return true;
    }

    private boolean isValidInsureeNumber() {
        Escape escape = new Escape();
        return escape.CheckCHFID(etInsureeNumber.getText().toString());
    }

    protected void showValidationDialog(View view, String msg) {
        runOnUiThread(() -> showDialog(msg, (dialog, which) -> {
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                editText.requestFocus();
            }
        }));
    }

    protected void confirmNewDialog(String msg) {
        runOnUiThread(() -> showDialog(msg, (dialog, which) -> ClearForm(), (dialog, which) -> dialog.dismiss()));
    }

    private boolean saveClaim() {
        Intent intent = getIntent();
        String claimUUID;
        if (intent.hasExtra(EXTRA_CLAIM_UUID)) {
            claimUUID = intent.getStringExtra(EXTRA_CLAIM_UUID);
        } else {
            claimUUID = UUID.randomUUID().toString();
        }

        String claimDate = DateUtils.toDateString(new Date());

        int SelectedId;
        SelectedId = rgVisitType.getCheckedRadioButtonId();
        RadioButton selectedTypeButton;
        selectedTypeButton = findViewById(SelectedId);
        String visitType = selectedTypeButton.getTag().toString();

        ContentValues claimCV = new ContentValues();

        claimCV.put("ClaimUUID", claimUUID);
        claimCV.put("ClaimDate", claimDate);
        claimCV.put("HFCode", etHealthFacility.getText().toString());
        claimCV.put("ClaimAdmin", etClaimAdmin.getText().toString());
        claimCV.put("ClaimCode", etClaimCode.getText().toString());
        claimCV.put("GuaranteeNumber", etGuaranteeNo.getText().toString());
        claimCV.put("InsureeNumber", etInsureeNumber.getText().toString());
        claimCV.put("StartDate", etStartDate.getText().toString());
        claimCV.put("EndDate", etEndDate.getText().toString());
        claimCV.put("ICDCode", etDiagnosis.getText().toString());
        claimCV.put("Comment", "");
        claimCV.put("Total", "");
        claimCV.put("ICDCode1", etDiagnosis1.getText().toString());
        claimCV.put("ICDCode2", etDiagnosis2.getText().toString());
        claimCV.put("ICDCode3", etDiagnosis3.getText().toString());
        claimCV.put("ICDCode4", etDiagnosis4.getText().toString());
        claimCV.put("VisitType", visitType);

        ArrayList<ContentValues> claimItemCVs = new ArrayList<>(lvItemList.size());
        for (int i = 0; i < lvItemList.size(); i++) {
            ContentValues claimItemCV = new ContentValues();

            claimItemCV.put("ClaimUUID", claimUUID);
            claimItemCV.put("ItemCode", lvItemList.get(i).get("Code"));
            claimItemCV.put("ItemPrice", lvItemList.get(i).get("Price"));
            claimItemCV.put("ItemQuantity", lvItemList.get(i).get("Quantity"));

            claimItemCVs.add(claimItemCV);
        }

        ArrayList<ContentValues> claimServiceCVs = new ArrayList<>(lvServiceList.size());
        for (int i = 0; i < lvServiceList.size(); i++) {
            ContentValues claimServiceCV = new ContentValues();

            claimServiceCV.put("ClaimUUID", claimUUID);
            claimServiceCV.put("ServiceCode", lvServiceList.get(i).get("Code"));
            claimServiceCV.put("ServicePrice", lvServiceList.get(i).get("Price"));
            claimServiceCV.put("ServiceQuantity", lvServiceList.get(i).get("Quantity"));

            claimServiceCVs.add(claimServiceCV);
        }

        sqlHandler.saveClaim(claimCV, claimItemCVs, claimServiceCVs);
        return true;
    }
}
