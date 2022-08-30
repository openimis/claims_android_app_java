package org.openimis.imisclaims;

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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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

import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.util.JsonUtils;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import static org.openimis.imisclaims.BuildConfig.API_BASE_URL;

public class EnquireActivity extends ImisActivity {
    public static final String LOG_TAG = "ENQUIRE";
    public static final int REQUEST_QR_SCAN_CODE = 1;

    private Picasso picasso;

    EditText etCHFID;
    TextView tvCHFID, tvName, tvGender, tvDOB;
    ImageButton btnGo, btnScan;
    ListView lv;
    ImageView iv;
    LinearLayout ll;
    ProgressDialog pd;

    ArrayList<HashMap<String, String>> PolicyList = new ArrayList<>();

    Bitmap theImage;
    String result;
    SQLiteDatabase db;

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

        picasso = new Picasso.Builder(this).build();
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

    @SuppressLint("WrongConstant")
    private String getDataFromDb(String chfid) {
        StringBuilder builder;
        try {
            builder = new StringBuilder("[{");

            db = openOrCreateDatabase(SQLHandler.DB_NAME_DATA, SQLiteDatabase.OPEN_READONLY, null);
            String[] columns = {"CHFID", "Photo", "InsureeName", "DOB", "Gender", "ProductCode", "ProductName", "ExpiryDate", "Status", "DedType", "Ded1", "Ded2", "Ceiling1", "Ceiling2"};
            String[] selectionArgs = {chfid};
            Cursor c = db.query("tblPolicyInquiry", columns, "Trim(CHFID)=?", selectionArgs, null, null, null);

            int i = 0;
            boolean _isHeadingDone = false;

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                for (i = 0; i < 5; i++) {
                    if (!_isHeadingDone) {
                        if (c.getColumnName(i).equalsIgnoreCase("photo")) {
                            byte[] photo = c.getBlob(i);
                            if (photo != null) {
                                ByteArrayInputStream is = new ByteArrayInputStream(photo);
                                theImage = BitmapFactory.decodeStream(is);
                            }
                            continue;
                        }
                        builder.append("\"").append(c.getColumnName(i)).append("\":").append("\"").append(c.getString(i)).append("\",");
                    }
                }
                _isHeadingDone = true;

                if (c.isFirst())
                    builder.append("\"").append("Details").append("\":[{");
                else
                    builder.append("{");

                for (i = 5; i < c.getColumnCount(); i++) {

                    builder.append("\"").append(c.getColumnName(i)).append("\":").append("\"").append(c.getString(i)).append("\"");
                    if (i < c.getColumnCount() - 1)
                        builder.append(",");
                    else {
                        builder.append("}");
                        if (!c.isLast()) builder.append(",");
                    }
                }
            }

            c.close();
            builder.append("]}]");

        } catch (Exception e) {
            Log.e(LOG_TAG, "Parsing offline enquire failed", e);
            builder = new StringBuilder();
        }

        return builder.toString();
    }

    private void getInsureeInfo() {
        runOnUiThread(this::ClearForm);
        String chfid = etCHFID.getText().toString();
        result = "";

        if (global.isNetworkAvailable()) {
            try {
                ToRestApi rest = new ToRestApi();
                HttpResponse response = rest.getFromRestApiToken("insuree/" + chfid + "/enquire");
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject obj = new JSONObject(rest.getContent(response));
                    JSONArray arr = new JSONArray();
                    arr.put(obj);
                    result = arr.toString();
                    runOnUiThread(this::renderResult);
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() -> showDialog(getResources().getString(R.string.RecordNotFound)));
                } else {
                    runOnUiThread(() -> showDialog(rest.getHttpError(this, responseCode)));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Fetching online enquire failed", e);
                runOnUiThread(() -> showDialog(getResources().getString(R.string.UnknownError)));
            }
        } else {
            //TODO: yet to be done
            result = getDataFromDb(chfid);
            runOnUiThread(this::renderResult);
        }
    }

    public void renderResult() {
        try {
            JSONArray jsonArray = new JSONArray(result);

            if (jsonArray.length() == 0) {
                showDialog(getResources().getString(R.string.RecordNotFound));
                return;
            }

            ll.setVisibility(View.VISIBLE);

            int i = 0;
            for (i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!etCHFID.getText().toString().trim().equals(jsonObject.getString("chfid").trim()))
                    continue;

                tvCHFID.setText(jsonObject.getString("chfid"));
                tvName.setText(jsonObject.getString("insureeName"));
                tvDOB.setText(jsonObject.getString("dob"));//Adjust
                tvGender.setText(jsonObject.getString("gender"));

                if (global.isNetworkAvailable()) {
                    if (JsonUtils.isStringEmpty(jsonObject, "photoBase64", true)) {
                        try {
                            byte[] imageBytes = Base64.decode(jsonObject.getString("photoBase64").getBytes(), Base64.DEFAULT);
                            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            iv.setImageBitmap(image);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error while processing Base64 image", e);
                            iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                        }
                    } else if (JsonUtils.isStringEmpty(jsonObject, "photoPath", true)) {
                        String photo_url_str = API_BASE_URL + jsonObject.getString("photoPath");
                        iv.setImageResource(R.drawable.person);
                        picasso.load(photo_url_str)
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person)
                                .into(iv);
                    } else {
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                    }
                } else {
                    if (theImage != null) {
                        iv.setImageBitmap(theImage);
                    } else {
                        byte[] photo = jsonObject.getString("photoPath").getBytes();
                        ByteArrayInputStream is = new ByteArrayInputStream(photo);
                        theImage = BitmapFactory.decodeStream(is);
                        if (theImage != null) {
                            iv.setImageBitmap(theImage);
                        } else {
                            iv.setImageResource(R.drawable.person);
                        }
                    }
                }

                jsonArray = new JSONArray(jsonObject.getString("details"));

                for (i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    HashMap<String, String> Policy = new HashMap<>();
                    jsonObject = jsonArray.getJSONObject(i);

                    double iDedType = Double.parseDouble(JsonUtils.getStringOrDefault(jsonObject, "dedType", "0", true));

                    String Ded = "", Ded1 = "", Ded2 = "";
                    String Ceiling = "", Ceiling1 = "", Ceiling2 = "";

                    String jDed1, jDed2, jCeiling1, jCeiling2;

                    jDed1 = JsonUtils.getStringOrDefault(jsonObject, "ded1", "", true);
                    jDed2 = JsonUtils.getStringOrDefault(jsonObject, "ded2", "", true);
                    jCeiling1 = JsonUtils.getStringOrDefault(jsonObject, "ceiling1", "", true);
                    jCeiling2 = JsonUtils.getStringOrDefault(jsonObject, "ceiling2", "", true);


                    //Get the type

                    if (iDedType == 1 | iDedType == 2 | iDedType == 3) {
                        if (!jDed1.equals("")) Ded1 = jsonObject.getString("ded1");
                        if (!jCeiling1.equals("")) Ceiling1 = jsonObject.getString("ceiling1");

                        if (!Ded1.equals("")) Ded = "Deduction: " + Ded1;
                        if (!Ceiling1.equals("")) Ceiling = "Ceiling: " + Ceiling1;

                    } else if (iDedType == 1.1 | iDedType == 2.1 | iDedType == 3.1) {
                        if (jDed1.length() > 0) Ded1 = " IP:" + jsonObject.getString("ded1");
                        if (jDed2.length() > 0) Ded2 = " OP:" + jsonObject.getString("ded2");
                        if (jCeiling1.length() > 0)
                            Ceiling1 = " IP:" + jsonObject.getString("ceiling1");
                        if (jCeiling2.length() > 0)
                            Ceiling2 = " OP:" + jsonObject.getString("ceiling2");

                        if (!(Ded1 + Ded2).equals("")) Ded = "Deduction: " + Ded1 + Ded2;
                        if (!(Ceiling1 + Ceiling2).equals(""))
                            Ceiling = "Ceiling: " + Ceiling1 + Ceiling2;

                    }

                    Policy.put("Heading", jsonObject.getString("productCode"));
                    Policy.put("Heading1", JsonUtils.getStringOrDefault(jsonObject, "expiryDate", "", true) + "  " + jsonObject.getString("status"));
                    Policy.put("SubItem1", jsonObject.getString("productName"));
                    Policy.put("SubItem2", Ded);
                    Policy.put("SubItem3", Ceiling);
                    String TotalAdmissionsLeft;
                    String TotalVisitsLeft;
                    String TotalConsultationsLeft;
                    String TotalSurgeriesLeft;
                    String TotalDeliveriesLeft;
                    String TotalAntenatalLeft;
                    String ConsultationAmountLeft;
                    String SurgeryAmountLeft;
                    String HospitalizationAmountLeft;
                    String AntenatalAmountLeft;
                    String DeliveryAmountLeft;

                    TotalAdmissionsLeft = buildEnquireValue(jsonObject, "totalAdmissionsLeft", R.string.totalAdmissionsLeft);
                    TotalVisitsLeft = buildEnquireValue(jsonObject, "totalVisitsLeft", R.string.totalVisitsLeft);
                    TotalConsultationsLeft = buildEnquireValue(jsonObject, "totalConsultationsLeft", R.string.totalConsultationsLeft);
                    TotalSurgeriesLeft = buildEnquireValue(jsonObject, "totalSurgeriesLeft", R.string.totalSurgeriesLeft);
                    TotalDeliveriesLeft = buildEnquireValue(jsonObject, "totalDelivieriesLeft", R.string.totalDeliveriesLeft);
                    TotalAntenatalLeft = buildEnquireValue(jsonObject, "totalAntenatalLeft", R.string.totalAntenatalLeft);
                    ConsultationAmountLeft = buildEnquireValue(jsonObject, "consultationAmountLeft", R.string.consultationAmountLeft);
                    SurgeryAmountLeft = buildEnquireValue(jsonObject, "surgeryAmountLeft", R.string.surgeryAmountLeft);
                    HospitalizationAmountLeft = buildEnquireValue(jsonObject, "hospitalizationAmountLeft", R.string.hospitalizationAmountLeft);
                    AntenatalAmountLeft = buildEnquireValue(jsonObject, "antenatalAmountLeft", R.string.antenatalAmountLeft);
                    DeliveryAmountLeft = buildEnquireValue(jsonObject, "deliveryAmountLeft", R.string.deliveryAmountLeft);

                    if (!getSpecificControl("TotalAdmissionsLeft").equals("N")) {
                        Policy.put("SubItem4", TotalAdmissionsLeft);
                    }
                    if (!getSpecificControl("TotalVisitsLeft").equals("N")) {
                        Policy.put("SubItem5", TotalVisitsLeft);
                    }
                    if (!getSpecificControl("TotalConsultationsLeft").equals("N")) {
                        Policy.put("SubItem6", TotalConsultationsLeft);
                    }
                    if (!getSpecificControl("TotalSurgeriesLeft").equals("N")) {
                        Policy.put("SubItem7", TotalSurgeriesLeft);
                    }
                    if (!getSpecificControl("TotalDelivieriesLeft").equals("N")) {
                        Policy.put("SubItem8", TotalDeliveriesLeft);
                    }
                    if (!getSpecificControl("TotalAntenatalLeft").equals("N")) {
                        Policy.put("SubItem9", TotalAntenatalLeft);
                    }
                    if (!getSpecificControl("ConsultationAmountLeft").equals("N")) {
                        Policy.put("SubItem10", ConsultationAmountLeft);
                    }
                    if (!getSpecificControl("AntenatalAmountLeft").equals("N")) {
                        Policy.put("SubItem13", AntenatalAmountLeft);
                    }
                    if (!getSpecificControl("SurgeryAmountLeft").equals("N")) {
                        Policy.put("SubItem11", SurgeryAmountLeft);
                    }
                    if (!getSpecificControl("HospitalizationAmountLeft").equals("N")) {
                        Policy.put("SubItem12", HospitalizationAmountLeft);
                    }
                    if (!getSpecificControl("DeliveryAmountLeft").equals("N")) {
                        Policy.put("SubItem14", DeliveryAmountLeft);
                    }

                    PolicyList.add(Policy);
                    etCHFID.setText("");
                    //break;
                }
            }
            ListAdapter adapter = new SimpleAdapter(EnquireActivity.this,
                    PolicyList, R.layout.policylist,
                    new String[]{"Heading", "Heading1", "SubItem1", "SubItem2", "SubItem3", "SubItem4", "SubItem5", "SubItem6", "SubItem7", "SubItem8", "SubItem9", "SubItem10", "SubItem11", "SubItem12", "SubItem13", "SubItem14"},
                    new int[]{R.id.tvHeading, R.id.tvHeading1, R.id.tvSubItem1, R.id.tvSubItem2, R.id.tvSubItem3, R.id.tvSubItem4, R.id.tvSubItem5, R.id.tvSubItem6, R.id.tvSubItem7, R.id.tvSubItem8, R.id.tvSubItem9, R.id.tvSubItem10, R.id.tvSubItem11, R.id.tvSubItem12, R.id.tvSubItem13, R.id.tvSubItem14}
            );

            lv.setAdapter(adapter);
        } catch (JSONException e) {
            Log.e("Error", "JSON related error when parsing enquiry response", e);
            result = "";
        } catch (Exception e) {
            Log.e("Error", "Unknown error when parsing enquiry response", e);
            result = "";
        }
    }

    protected String buildEnquireValue(JSONObject jsonObject, String jsonKey, int labelId) throws JSONException {
        boolean ignore = jsonObject.getString(jsonKey).equalsIgnoreCase("null");
        if (ignore) {
            return "";
        } else {
            String label = getResources().getString(labelId);
            return label + ": " + jsonObject.getString(jsonKey);
        }
    }

    private void ClearForm() {
        tvCHFID.setText(getResources().getString(R.string.CHFID));
        tvName.setText(getResources().getString(R.string.InsureeName));
        tvDOB.setText(getResources().getString(R.string.DOB));
        tvGender.setText(getResources().getString(R.string.Gender));
        iv.setImageResource(R.drawable.noimage);
        ll.setVisibility(View.INVISIBLE);
        PolicyList.clear();
        lv.setAdapter(null);
    }

    private String getSpecificControl(String FieldName) {
        SQLHandler sqlHandler = new SQLHandler(this);
        sqlHandler.onOpen(db);
        String control = sqlHandler.getAdjustability(FieldName);
        sqlHandler.close();
        return control;
    }
}
