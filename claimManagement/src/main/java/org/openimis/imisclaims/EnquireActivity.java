package org.openimis.imisclaims;

import static org.openimis.imisclaims.BuildConfig.API_BASE_URL;
import static org.openimis.imisclaims.BuildConfig.REST_API_PREFIX;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.openimis.imisclaims.domain.entity.Insuree;
import org.openimis.imisclaims.domain.entity.Policy;
import org.openimis.imisclaims.network.exception.HttpException;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.usecase.FetchInsureeInquire;
import org.openimis.imisclaims.util.DateUtils;
import org.openimis.imisclaims.util.TextViewUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnquireActivity extends ImisActivity {
    private static final String LOG_TAG = "ENQUIRE";
    private static final int REQUEST_QR_SCAN_CODE = 1;
    EditText etCHFID;
    TextView tvCHFID, tvName, tvGender, tvDOB;
    ImageButton btnGo, btnScan;
    ListView lv;
    ImageView iv;
    LinearLayout ll;
    ProgressDialog pd;

    private boolean ZoomOut = false;
    private int orgHeight, orgWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_enquire);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.app_name_enquire));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        isSDCardAvailable();

        //Check if network available
        if (!global.isNetworkAvailable()) {
            setTitle(getResources().getString(R.string.app_name_claims) + "-" + getResources().getString(R.string.OfflineMode));
            setTitleColor(getResources().getColor(R.color.Red));
        }

        etCHFID = findViewById(R.id.etCHFID);
        tvCHFID = findViewById(R.id.tvCHFID);
        tvName = findViewById(R.id.tvName);
        tvDOB = findViewById(R.id.tvDOB);
        tvGender = findViewById(R.id.tvGender);
        iv = findViewById(R.id.imageView);
        btnGo = findViewById(R.id.btnGo);
        btnScan = findViewById(R.id.btnScan);
        lv = findViewById(R.id.listView1);
        ll = findViewById(R.id.llListView);

        iv.setOnClickListener(v -> {
            if (ZoomOut) {
                iv.setLayoutParams(new LinearLayout.LayoutParams(orgWidth, orgHeight));
                iv.setAdjustViewBounds(true);
                ZoomOut = false;
            } else {
                orgWidth = iv.getWidth();
                orgHeight = iv.getHeight();
                iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ZoomOut = true;
            }
        });

        btnGo.setOnClickListener(v -> {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            ClearForm();
            Escape escape = new Escape();
            if (!escape.CheckCHFID(etCHFID.getText().toString())) {
                ShowDialog(tvCHFID, getResources().getString(R.string.MissingCHFID));
                return;
            }

            pd = ProgressDialog.show(EnquireActivity.this, "", getResources().getString(R.string.GetingInsuuree));
            new Thread(() -> {
                getInsureeInfo();
                pd.dismiss();
            }).start();
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.google.zxing.client.android.CaptureActivity.class);
            intent.setAction("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, REQUEST_QR_SCAN_CODE);
            ClearForm();
        });

        etCHFID.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                ClearForm();
                Escape escape = new Escape();
                if (!escape.CheckCHFID(etCHFID.getText().toString())) return false;

                pd = ProgressDialog.show(EnquireActivity.this, "", getResources().getString(R.string.GetingInsuuree));
                new Thread(() -> {
                    getInsureeInfo();
                    pd.dismiss();
                }).start();
            }
            return false;
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_QR_SCAN_CODE:
                if (resultCode == RESULT_OK) {
                    String CHFID = data.getStringExtra("SCAN_RESULT");
                    etCHFID.setText(CHFID);

                    Escape escape = new Escape();
                    if (!escape.CheckCHFID(etCHFID.getText().toString())) return;

                    pd = ProgressDialog.show(EnquireActivity.this, "", getResources().getString(R.string.GetingInsuuree));
                    new Thread(() -> {
                        getInsureeInfo();
                        pd.dismiss();
                    }).start();
                }
                break;
        }
    }

    private void isSDCardAvailable() {
        String status = ((Global) getApplicationContext()).getSDCardStatus();
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

    protected AlertDialog ShowDialog(final TextView tv, String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, (dialog, which) -> tv.requestFocus()).show();
    }

    @SuppressLint({"WrongConstant", "Range"})
    @Nullable
    private Insuree getDataFromDb(String chfid) {
        try {
            SQLiteDatabase db = openOrCreateDatabase(SQLHandler.DB_NAME_DATA, SQLiteDatabase.OPEN_READONLY, null);
            String[] columns = {"CHFID", "Photo", "InsureeName", "DOB", "Gender", "ProductCode", "ProductName", "ExpiryDate", "Status", "DedType", "Ded1", "Ded2", "Ceiling1", "Ceiling2"};
            String[] selectionArgs = {chfid};
            Cursor c = db.query("tblPolicyInquiry", columns, "Trim(CHFID)=?", selectionArgs, null, null, null);
            String name = null;
            Date dateOfBirth = null;
            String gender = null;
            byte[] photo = null;
            List<Policy> policies = new ArrayList<>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                if (c.isFirst()) {
                    name = c.getString(c.getColumnIndex("InsureeName"));
                    String dateOfBirthString = c.getString(c.getColumnIndex("DOB"));
                    if (dateOfBirthString != null) {
                        dateOfBirth = DateUtils.dateFromString(dateOfBirthString);
                    }
                    gender = c.getString(c.getColumnIndex("Gender"));
                    photo = c.getBlob(c.getColumnIndex("Photo"));
                }
                String expiryDate = c.getString(c.getColumnIndex("ExpiryDate"));
                String status = c.getString(c.getColumnIndex("Status"));
                String deductibleType = c.getString(c.getColumnIndex("DedType"));
                String deductibleIp = c.getString(c.getColumnIndex("Ded1"));
                String deductibleOp = c.getString(c.getColumnIndex("Ded2"));
                String ceilingIp = c.getString(c.getColumnIndex("Ceiling1"));
                String ceilingOp = c.getString(c.getColumnIndex("Ceiling2"));
                policies.add(new Policy(
                        /* code = */ c.getString(c.getColumnIndex("ProductCode")),
                        /* name = */ c.getString(c.getColumnIndex("ProductName")),
                        /* value = */ null,
                        /* expiryDate = */ expiryDate != null ? DateUtils.dateFromString(expiryDate) : null,
                        /* status = */ status != null ? Policy.Status.valueOf(status) : null,
                        /* deductibleType = */ deductibleType != null ? Double.parseDouble(deductibleType) : null,
                        /* deductibleIp = */ deductibleIp != null ? Double.parseDouble(deductibleIp) : null,
                        /* deductibleOp = */ deductibleOp != null ? Double.parseDouble(deductibleOp) : null,
                        /* ceilingIp = */ ceilingIp != null ? Double.parseDouble(ceilingIp) : null,
                        /* ceilingOp = */ ceilingOp != null ? Double.parseDouble(ceilingOp) : null,
                        /* antenatalAmountLeft = */ null,
                        /* consultationAmountLeft = */ null,
                        /* deliveryAmountLeft = */ null,
                        /* hospitalizationAmountLeft = */ null,
                        /* surgeryAmountLeft = */ null,
                        /* totalAdmissionsLeft = */ null,
                        /* totalAntenatalLeft = */ null,
                        /* totalConsultationsLeft = */ null,
                        /* totalDeliveriesLeft = */ null,
                        /* totalSurgeriesLeft = */ null,
                        /* totalVisitsLeft = */ null
                ));
            }
            c.close();
            db.close();
            return new Insuree(
                    /* chfId = */ chfid,
                    /* name = */ Objects.requireNonNull(name),
                    /* dateOfBirth = */ Objects.requireNonNull(dateOfBirth),
                    /* gender = */ gender,
                    /* photoPath = */ null,
                    /* photo = */ photo,
                    /* policies = */ policies
            );
        } catch (Exception e) {
            Log.e(LOG_TAG, "Parsing offline enquire failed", e);
            return null;
        }

    }

    @WorkerThread
    private void getInsureeInfo() {
        runOnUiThread(this::ClearForm);
        String chfid = etCHFID.getText().toString();
        if (global.isNetworkAvailable()) {
            try {
                Insuree insuree = new FetchInsureeInquire().execute(chfid);
                runOnUiThread(() -> renderResult(insuree));
            } catch (HttpException e) {
                if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() -> showDialog(getResources().getString(R.string.RecordNotFound)));
                } else {
                    runOnUiThread(() -> showDialog(e.getMessage()));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Fetching online enquire failed", e);
                runOnUiThread(() -> showDialog(getResources().getString(R.string.UnknownError)));
            }
        } else {
            //TODO: yet to be done
            runOnUiThread(() -> renderResult(getDataFromDb(chfid)));
        }
    }

    public void renderResult(@Nullable Insuree insuree) {
        if (insuree == null) {
            showDialog(getResources().getString(R.string.RecordNotFound));
            return;
        }

        ll.setVisibility(View.VISIBLE);

        if (!etCHFID.getText().toString().trim().equals(insuree.getChfId()))
            return;

        tvCHFID.setText(insuree.getChfId());
        tvName.setText(insuree.getName());
        TextViewUtils.setDate(tvDOB, insuree.getDateOfBirth());
        tvGender.setText(insuree.getGender());

        byte[] imageBytes = insuree.getPhoto();
        if (imageBytes != null) {
            try {
                Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                iv.setImageBitmap(image);
            } catch (Throwable e) {
                Log.e(LOG_TAG, "Error while processing Base64 image", e);
                iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
            }
        } else if (insuree.getPhotoPath() != null && global.isNetworkAvailable()) {
            iv.setImageResource(R.drawable.person);
            new Picasso.Builder(this).build()
                    .load(API_BASE_URL + REST_API_PREFIX + insuree.getPhotoPath())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(iv);
        } else {
            iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
        }

        ArrayList<Map<String, String>> PolicyList = new ArrayList<>();
        for (Policy policy : insuree.getPolicies()) {
            HashMap<String, String> policyMap = new HashMap<>();
            double iDedType = policy.getDeductibleType() != null ? policy.getDeductibleType() : 0;

            String Ded = "", Ded1 = "", Ded2 = "";
            String Ceiling = "", Ceiling1 = "", Ceiling2 = "";


            //Get the type

            if (iDedType == 1 | iDedType == 2 | iDedType == 3) {
                if (policy.getDeductibleIp() != null) {
                    Ded1 = String.valueOf(policy.getDeductibleIp());
                    Ded = "Deduction: " + Ded1;
                }
                if (policy.getCeilingIp() != null) {
                    Ceiling1 = String.valueOf(policy.getCeilingIp());
                    Ceiling = "Ceiling: " + Ceiling1;
                }
            } else if (iDedType == 1.1 | iDedType == 2.1 | iDedType == 3.1) {
                if (policy.getDeductibleIp() != null) {
                    Ded1 = " IP:" + policy.getDeductibleIp();
                }
                if (policy.getDeductibleOp() != null) {
                    Ded2 = " OP:" + policy.getDeductibleOp();
                }
                if (policy.getCeilingIp() != null) {
                    Ceiling1 = " IP:" + policy.getCeilingIp();
                }
                if (policy.getCeilingIp() != null) {
                    Ceiling2 = " OP:" + policy.getCeilingOp();
                }

                if (!(Ded1 + Ded2).equals("")) {
                    Ded = "Deduction: " + Ded1 + Ded2;
                }
                if (!(Ceiling1 + Ceiling2).equals("")) {
                    Ceiling = "Ceiling: " + Ceiling1 + Ceiling2;
                }
            }

            String expiryDate = policy.getExpiryDate() != null ?
                    DateUtils.toDateString(policy.getExpiryDate()) : null;
            String status = policy.getStatus().name();
            String heading1;
            if (expiryDate != null) {
                heading1 = expiryDate + " " + status;
            } else {
                heading1 = status;
            }
            policyMap.put("Heading", policy.getCode());
            policyMap.put("Heading1", heading1);
            policyMap.put("SubItem1", policy.getName());
            policyMap.put("SubItem2", Ded);
            policyMap.put("SubItem3", Ceiling);

            SQLHandler sqlHandler = new SQLHandler(this);
            if (!sqlHandler.getAdjustability("TotalAdmissionsLeft").equals("N")) {
                policyMap.put("SubItem4", buildEnquireValue(policy.getTotalAdmissionsLeft(), R.string.totalAdmissionsLeft));
            }
            if (!sqlHandler.getAdjustability("TotalVisitsLeft").equals("N")) {
                policyMap.put("SubItem5", buildEnquireValue(policy.getTotalVisitsLeft(), R.string.totalVisitsLeft));
            }
            if (!sqlHandler.getAdjustability("TotalConsultationsLeft").equals("N")) {
                policyMap.put("SubItem6", buildEnquireValue(policy.getTotalConsultationsLeft(), R.string.totalConsultationsLeft));
            }
            if (!sqlHandler.getAdjustability("TotalSurgeriesLeft").equals("N")) {
                policyMap.put("SubItem7", buildEnquireValue(policy.getTotalSurgeriesLeft(), R.string.totalSurgeriesLeft));
            }
            if (!sqlHandler.getAdjustability("TotalDelivieriesLeft").equals("N")) {
                policyMap.put("SubItem8", buildEnquireValue(policy.getTotalDeliveriesLeft(), R.string.totalDeliveriesLeft));
            }
            if (!sqlHandler.getAdjustability("TotalAntenatalLeft").equals("N")) {
                policyMap.put("SubItem9", buildEnquireValue(policy.getTotalAntenatalLeft(), R.string.totalAntenatalLeft));
            }
            if (!sqlHandler.getAdjustability("ConsultationAmountLeft").equals("N")) {
                policyMap.put("SubItem10", buildEnquireValue(policy.getConsultationAmountLeft(), R.string.consultationAmountLeft));
            }
            if (!sqlHandler.getAdjustability("AntenatalAmountLeft").equals("N")) {
                policyMap.put("SubItem13", buildEnquireValue(policy.getAntenatalAmountLeft(), R.string.antenatalAmountLeft));
            }
            if (!sqlHandler.getAdjustability("SurgeryAmountLeft").equals("N")) {
                policyMap.put("SubItem11", buildEnquireValue(policy.getSurgeryAmountLeft(), R.string.surgeryAmountLeft));
            }
            if (!sqlHandler.getAdjustability("HospitalizationAmountLeft").equals("N")) {
                policyMap.put("SubItem12", buildEnquireValue(policy.getHospitalizationAmountLeft(), R.string.hospitalizationAmountLeft));
            }
            if (!sqlHandler.getAdjustability("DeliveryAmountLeft").equals("N")) {
                policyMap.put("SubItem14", buildEnquireValue(policy.getDeliveryAmountLeft(), R.string.deliveryAmountLeft));
            }
            sqlHandler.close();

            PolicyList.add(policyMap);
            etCHFID.setText("");
            //break;
        }

        ListAdapter adapter = new SimpleAdapter(EnquireActivity.this,
                PolicyList, R.layout.policylist,
                new String[]{"Heading", "Heading1", "SubItem1", "SubItem2", "SubItem3", "SubItem4", "SubItem5", "SubItem6", "SubItem7", "SubItem8", "SubItem9", "SubItem10", "SubItem11", "SubItem12", "SubItem13", "SubItem14"},
                new int[]{R.id.tvHeading, R.id.tvHeading1, R.id.tvSubItem1, R.id.tvSubItem2, R.id.tvSubItem3, R.id.tvSubItem4, R.id.tvSubItem5, R.id.tvSubItem6, R.id.tvSubItem7, R.id.tvSubItem8, R.id.tvSubItem9, R.id.tvSubItem10, R.id.tvSubItem11, R.id.tvSubItem12, R.id.tvSubItem13, R.id.tvSubItem14}
        );

        lv.setAdapter(adapter);
    }

    protected String buildEnquireValue(@Nullable Number value, @StringRes int labelId) {
        if (value == null) {
            return "";
        } else {
            String label = getResources().getString(labelId);
            return label + ": " + value;
        }
    }

    private void ClearForm() {
        tvCHFID.setText(getResources().getString(R.string.CHFID));
        tvName.setText(getResources().getString(R.string.InsureeName));
        tvDOB.setText(getResources().getString(R.string.DOB));
        tvGender.setText(getResources().getString(R.string.Gender));
        iv.setImageResource(R.drawable.noimage);
        ll.setVisibility(View.INVISIBLE);
        lv.setAdapter(null);
    }
}
