package org.openimis.imisclaims;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.general.General;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openimis.imisclaims.ClaimActivity.EndDate_Dialog_ID;
import static org.openimis.imisclaims.ClaimActivity.StartDate_Dialog_ID;


public class SearchClaims extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ProgressDialog pd;

    General _General;
    ToRestApi toRestApi;
    Token tokenl;

    EditText visit_date_from;
    EditText visit_date_to;
    EditText date_processed_from;
    EditText date_processed_to;
    String status_claim = "";

    Button clear;
    Button search;

    static final int StartDate_Dialog_ID1 = 2;
    static final int EndDate_Dialog_ID2 = 3;

    private int year, month, day;
    final Calendar cal = Calendar.getInstance();

    Global global = new Global();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_claims);

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        _General = new General();

        toRestApi = new ToRestApi();
        tokenl = new Token();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.SearchClaims));


        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner_status);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select claim status");
        categories.add(getString(R.string.Entered));
        categories.add(getString(R.string.Checked));
        categories.add(getString(R.string.Processed));
        categories.add(getString(R.string.Valuated));
        categories.add(getString(R.string.Rejected));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);


        clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visit_date_from.setText("");
                visit_date_to.setText("");
                date_processed_from.setText("");
                date_processed_to.setText("");
            }
        });


        visit_date_from = (EditText) findViewById(R.id.visit_date_from);
        visit_date_to = (EditText) findViewById(R.id.visit_date_to);
        date_processed_from = (EditText) findViewById(R.id.date_processed_from);
        date_processed_to = (EditText) findViewById(R.id.date_processed_to);

        visit_date_from.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialog(StartDate_Dialog_ID);
                return false;
            }
        });

        visit_date_to.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialog(EndDate_Dialog_ID);
                return false;
            }
        });

        date_processed_from.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialog(StartDate_Dialog_ID1);
                return false;
            }
        });

        date_processed_to.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialog(EndDate_Dialog_ID2);
                return false;
            }
        });

        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tokenl.getTokenText().length() <= 0){
                    LoginDialogBox();
                }else{
                    JSONObject object = new JSONObject();
                        Global global = new Global();
                    try {
                        //object.put("userName",username.getText().toString());
                        object.put("claim_administrator_code",global.getOfficerCode().toString());
                        if(status_claim.length() != 0){object.put("status_claim",status_claim);}
                        if(visit_date_from.length() != 0){object.put("visit_date_from",visit_date_from.getText());}
                        if(visit_date_to.length() != 0){object.put("visit_date_to",visit_date_to.getText());}
                        if(date_processed_from.length() != 0){object.put("processed_date_from",date_processed_from.getText());}
                        if(date_processed_to.length() != 0){object.put("processed_date_to",date_processed_to.getText());}
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getClaims(object);
                }
            }
        });
    }

    private void getClaims(JSONObject object) {
/*        String claims = "{\n" +
                "  \"error_occured\": false,\n" +
                "  \"claims\": [\n" +
                "    {\n" +
                "      \"health_facility_code\": \"HF02\",\n" +
                "      \"health_facility_name\": \"District1 health Center\",\n" +
                "      \"insurance_number\": \"111111142\",\n" +
                "      \"patient_name\": \"Fuchs Elis\",\n" +
                "      \"main_dg\": \"Shigellosis(BacilliaryDysentry)\",\n" +
                "      \"claim_number\": \"xx1\",\n" +
                "      \"date_claimed\": \"2017-06-26T00:00:00\",\n" +
                "      \"visit_date_from\": \"2017-06-26T00:00:00\",\n" +
                "      \"visit_type\": \"O\",\n" +
                "      \"claim_status\": \"Valuated\",\n" +
                "      \"sec_dg_1\": null,\n" +
                "      \"sec_dg_2\": null,\n" +
                "      \"sec_dg_3\": null,\n" +
                "      \"sec_dg_4\": null,\n" +
                "      \"visit_date_to\": \"2017-06-26T00:00:00\",\n" +
                "      \"claimed\": 1500,\n" +
                "      \"approved\": 500,\n" +
                "      \"adjusted\": 380,\n" +
                "      \"explination\": \"\",\n" +
                "      \"adjustment\": null,\n" +
                "      \"guarantee_number\": \"\",\n" +
                "      \"services\": [\n" +
                "        {\n" +
                "          \"claim_number\": \"xx1\",\n" +
                "          \"service\": \"Urinary lab test\",\n" +
                "          \"service_qty\": null,\n" +
                "          \"service_price\": 1000,\n" +
                "          \"service_adjusted_qty\": null,\n" +
                "          \"service_adjusted_price\": 200,\n" +
                "          \"service_explination\": \"\",\n" +
                "          \"service_justificaion\": null,\n" +
                "          \"service_valuated\": 200,\n" +
                "          \"service_result\": null\n" +
                "        },\n" +
                "        {\n" +
                "          \"claim_number\": \"xx1\",\n" +
                "          \"service\": \"Antenatal examination\",\n" +
                "          \"service_qty\": null,\n" +
                "          \"service_price\": 100,\n" +
                "          \"service_adjusted_qty\": null,\n" +
                "          \"service_adjusted_price\": 100,\n" +
                "          \"service_explination\": \"\",\n" +
                "          \"service_justificaion\": null,\n" +
                "          \"service_valuated\": 100,\n" +
                "          \"service_result\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"items\": [\n" +
                "        {\n" +
                "          \"claim_number\": \"xx1\",\n" +
                "          \"item\": \"ACETYLSALICYLIC ACID (ASPIRIN) TABS 300MG-\",\n" +
                "          \"item_qty\": null,\n" +
                "          \"item_price\": 400,\n" +
                "          \"item_adjusted_qty\": null,\n" +
                "          \"item_adjusted_price\": 200,\n" +
                "          \"item_explination\": \"\",\n" +
                "          \"item_justificaion\": null,\n" +
                "          \"item_valuated\": 80,\n" +
                "          \"item_result\": \"0\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"claim_number\": \"xx1\",\n" +
                "          \"item\": \"ADRENALINE 1ML INJ 1MG/ML\",\n" +
                "          \"item_qty\": 1,\n" +
                "          \"item_price\": 500,\n" +
                "          \"item_adjusted_qty\": 0,\n" +
                "          \"item_adjusted_price\": null,\n" +
                "          \"item_explination\": \"\",\n" +
                "          \"item_justificaion\": null,\n" +
                "          \"item_valuated\": null,\n" +
                "          \"item_result\": \"10\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"claim_number\": \"xx1\",\n" +
                "          \"item\": \"FRUSEMIDE TABS 40 MG\",\n" +
                "          \"item_qty\": 1,\n" +
                "          \"item_price\": 500,\n" +
                "          \"item_adjusted_qty\": 0,\n" +
                "          \"item_adjusted_price\": null,\n" +
                "          \"item_explination\": \"\",\n" +
                "          \"item_justificaion\": null,\n" +
                "          \"item_valuated\": null,\n" +
                "          \"item_result\": \"4\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"health_facility_code\": \"HF02\",\n" +
                "      \"health_facility_name\": \"District1 health Center\",\n" +
                "      \"insurance_number\": \"00100114\",\n" +
                "      \"patient_name\": \"Joseph Alila\",\n" +
                "      \"main_dg\": \"Food Poisoning (Bacterial)\",\n" +
                "      \"claim_number\": \"CLT003\",\n" +
                "      \"date_claimed\": \"2017-07-20T00:00:00\",\n" +
                "      \"visit_date_from\": \"2017-07-20T00:00:00\",\n" +
                "      \"visit_type\": \"E\",\n" +
                "      \"claim_status\": \"Valuated\",\n" +
                "      \"sec_dg_1\": null,\n" +
                "      \"sec_dg_2\": null,\n" +
                "      \"sec_dg_3\": null,\n" +
                "      \"sec_dg_4\": null,\n" +
                "      \"visit_date_to\": \"2017-07-20T00:00:00\",\n" +
                "      \"claimed\": 1600,\n" +
                "      \"approved\": 500,\n" +
                "      \"adjusted\": 500,\n" +
                "      \"explination\": \"\",\n" +
                "      \"adjustment\": null,\n" +
                "      \"guarantee_number\": \"\",\n" +
                "      \"services\": [\n" +
                "        {\n" +
                "          \"claim_number\": \"CLT003\",\n" +
                "          \"service\": \"GP visit\",\n" +
                "          \"service_qty\": null,\n" +
                "          \"service_price\": 100,\n" +
                "          \"service_adjusted_qty\": null,\n" +
                "          \"service_adjusted_price\": 100,\n" +
                "          \"service_explination\": \"\",\n" +
                "          \"service_justificaion\": null,\n" +
                "          \"service_valuated\": 100,\n" +
                "          \"service_result\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"items\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"health_facility_code\": \"HF02\",\n" +
                "      \"health_facility_name\": \"District1 health Center\",\n" +
                "      \"insurance_number\": \"777888981\",\n" +
                "      \"patient_name\": \"Pappen Jane\",\n" +
                "      \"main_dg\": \"Food Poisoning (Bacterial)\",\n" +
                "      \"claim_number\": \"wef03\",\n" +
                "      \"date_claimed\": \"2017-08-31T00:00:00\",\n" +
                "      \"visit_date_from\": \"2017-08-30T00:00:00\",\n" +
                "      \"visit_type\": \"R\",\n" +
                "      \"claim_status\": \"Processed\",\n" +
                "      \"sec_dg_1\": null,\n" +
                "      \"sec_dg_2\": null,\n" +
                "      \"sec_dg_3\": null,\n" +
                "      \"sec_dg_4\": null,\n" +
                "      \"visit_date_to\": \"2017-08-30T00:00:00\",\n" +
                "      \"claimed\": 158,\n" +
                "      \"approved\": 158,\n" +
                "      \"adjusted\": 0,\n" +
                "      \"explination\": \"\",\n" +
                "      \"adjustment\": null,\n" +
                "      \"guarantee_number\": null,\n" +
                "      \"services\": [],\n" +
                "      \"items\": [\n" +
                "        {\n" +
                "          \"claim_number\": \"wef03\",\n" +
                "          \"item\": null,\n" +
                "          \"item_qty\": null,\n" +
                "          \"item_price\": null,\n" +
                "          \"item_adjusted_qty\": null,\n" +
                "          \"item_adjusted_price\": null,\n" +
                "          \"item_explination\": null,\n" +
                "          \"item_justificaion\": null,\n" +
                "          \"item_valuated\": null,\n" +
                "          \"item_result\": null\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"health_facility_code\": \"HF02\",\n" +
                "      \"health_facility_name\": \"District1 health Center\",\n" +
                "      \"insurance_number\": \"00100114\",\n" +
                "      \"patient_name\": \"Joseph Alila\",\n" +
                "      \"main_dg\": \"Food Poisoning (Bacterial)\",\n" +
                "      \"claim_number\": \"wex04\",\n" +
                "      \"date_claimed\": \"2017-08-31T00:00:00\",\n" +
                "      \"visit_date_from\": \"2017-08-30T00:00:00\",\n" +
                "      \"visit_type\": \"R\",\n" +
                "      \"claim_status\": \"Valuated\",\n" +
                "      \"sec_dg_1\": null,\n" +
                "      \"sec_dg_2\": null,\n" +
                "      \"sec_dg_3\": null,\n" +
                "      \"sec_dg_4\": null,\n" +
                "      \"visit_date_to\": \"2017-08-30T00:00:00\",\n" +
                "      \"claimed\": 165,\n" +
                "      \"approved\": 165,\n" +
                "      \"adjusted\": 100,\n" +
                "      \"explination\": \"\",\n" +
                "      \"adjustment\": null,\n" +
                "      \"guarantee_number\": null,\n" +
                "      \"services\": [],\n" +
                "      \"items\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";*/
        getClaimsApi(object);

    }

    private void getClaimsApi(final JSONObject object) {
        String error_occurred = null;
        String error_message = null;
        String content = null;

        final HttpResponse[] resp = {null};
        if(_General.isNetworkAvailable(this)){
            String progress_message = getResources().getString(R.string.getClaims)+"...";
            pd = ProgressDialog.show(this, getResources().getString(R.string.DownLoad), progress_message);
            Thread thread = new Thread() {
                public void run() {

                    String services = null;
                    String error_occurred = null;
                    String error_message = null;
                    String content = null;

                    String functionName = "api/GetClaims";
                    try{
                        HttpResponse response = toRestApi.postToRestApi(object,functionName);
                        resp[0] = response;
                        HttpEntity respEntity = response.getEntity();
                        if (respEntity != null) {
                            final String[] code = {null};
                            // EntityUtils to get the response content
                            try {
                                content = EntityUtils.toString(respEntity);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        int code = response.getStatusLine().getStatusCode();

                        if(code < 400){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                }
                            });
                            JSONObject jsonObject = new JSONObject(content);
                            String data = jsonObject.getString("data");
                            if(data.length() != 0){
                                openClaimReview(content);
                            }else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(SearchClaims.this,resp[0].getStatusLine().getStatusCode() +"-"+getResources().getString(R.string.NoClaim),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }else {
                            pd.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(SearchClaims.this,resp[0].getStatusLine().getStatusCode() +"-"+getResources().getString(R.string.AccessDenied),Toast.LENGTH_LONG).show();
                                    LoginDialogBox();
                                }
                            });
                            Toast.makeText(SearchClaims.this,resp[0].getStatusLine().getStatusCode() +"-"+getResources().getString(R.string.AccessDenied),Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
                        pd.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(SearchClaims.this,resp[0].getStatusLine().getStatusCode() +"-"+getResources().getString(R.string.AccessDenied),Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            };
            thread.start();
        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                }
            });
            ErrorDialogBox(getResources().getString(R.string.CheckInternet));
        }

    }

    public void openClaimReview(String claims){
        Intent intent = new Intent(this, Claims.class);
        intent.putExtra("claims", claims);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        status_claim = (parent.getItemAtPosition(position).toString().equals("Select claim status"))?"":parent.getItemAtPosition(position).toString();

        // Showing selected spinner item

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
        status_claim = "";
    }

    @Override
    protected Dialog onCreateDialog(int id){
        switch(id){

            case StartDate_Dialog_ID:

                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, visit_date_fromPickerListener, year, month, day);

            case EndDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, visit_date_toPickerListner, year, month, day);


            case StartDate_Dialog_ID1:

                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, date_processed_fromPickerListener, year, month, day);

            case EndDate_Dialog_ID2:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this, date_processed_toPickerListner, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener visit_date_fromPickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth, int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;

            /*
            Date d = new Date(year, month, day);
            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "yyyy-MM-dd");
            String vDateFrom = dateFormatter.format(d);

            visit_date_from.setText(vDateFrom);*/
            visit_date_from.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
        }
    };

    private DatePickerDialog.OnDateSetListener visit_date_toPickerListner = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;

/*            Date d = new Date(year, month, day);
            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "yyyy-MM-dd");
            String vDateTo = dateFormatter.format(d);

            visit_date_to.setText(vDateTo);*/
            visit_date_to.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
        }
    };


    private DatePickerDialog.OnDateSetListener date_processed_fromPickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth, int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;

/*            Date d = new Date(year, month, day);
            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "yyyy-MM-dd");
            String datePF = dateFormatter.format(d);

            date_processed_from.setText(datePF);*/
            date_processed_from.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
        }
    };

    private DatePickerDialog.OnDateSetListener date_processed_toPickerListner = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;

/*            Date d = new Date(year, month, day);
            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "yyyy-MM-dd");
            String datePT = dateFormatter.format(d);

            date_processed_to.setText(datePT);*/
            date_processed_to.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
        }
    };

    public void ErrorDialogBox(final String message) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.error_message_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView error = (TextView) promptsView.findViewById(R.id.error_message);
        error.setText(message.toString());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void LoginDialogBox() {

        final int[] userid = {0};

        Global global = new Global();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(_General.isNetworkAvailable(SearchClaims.this)){
                                    if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){
                                        pd = ProgressDialog.show(SearchClaims.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                        new Thread() {
                                            public void run() {
/*                                            CallSoap callSoap = new CallSoap();
                                            callSoap.setFunctionName("isValidLogin");
                                            userid[0] = callSoap.isUserLoggedIn(username.getText().toString(),password.getText().toString());*/
                                                JSONObject object = new JSONObject();
                                                try {
                                                    object.put("userName",username.getText().toString());
                                                    object.put("password",password.getText().toString());
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                String functionName = "login";
                                                HttpResponse response = null;
                                                String content = null;
                                                try{
                                                    response = toRestApi.postToRestApi(object,functionName);

                                                    HttpEntity respEntity = response.getEntity();
                                                    if (respEntity != null) {
                                                        final String[] code = {null};
                                                        // EntityUtils to get the response content


                                                        content = EntityUtils.toString(respEntity);

                                                    }
                                                }catch (Exception e){
                                                    final HttpResponse finalResponse = response;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            pd.dismiss();
                                                            //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                            Toast.makeText(SearchClaims.this, finalResponse.getStatusLine().getStatusCode()+"-"+ SearchClaims.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                            LoginDialogBox();
                                                        }
                                                    });
                                                }

                                                if(response.getStatusLine().getStatusCode() == 401){
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            pd.dismiss();
                                                            //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                            Toast.makeText(SearchClaims.this, SearchClaims.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                            LoginDialogBox();
                                                        }
                                                    });

                                                }else{
                                                    JSONObject ob = null;
                                                    String token = null;
                                                    try {
                                                        ob = new JSONObject(content);
                                                        token = ob.getString("access_token");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    tokenl.saveTokenText(token.toString());

                                                    final String finalToken = token;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(finalToken.length() > 0){
                                                                pd.dismiss();
                                                                Toast.makeText(SearchClaims.this, SearchClaims.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();


                                                            }else{
                                                                pd.dismiss();
                                                                //ShowDialog(MainActivity.this.getResources().getString(R.string.LoginFail));
                                                                Toast.makeText(SearchClaims.this, SearchClaims.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                                LoginDialogBox();
                                                            }
                                                        }
                                                    });
                                                }


                                            }
                                        }.start();


                                    }else{
                                        LoginDialogBox();
                                        Toast.makeText(SearchClaims.this, SearchClaims.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    ErrorDialogBox(getResources().getString(R.string.CheckInternet));
                                }



                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

/*    public AlertDialog ShowComfirmationDialog(final JSONObject object) {
        return new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.AreYouSure))
                .setCancelable(false)

                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject l = null;
                        JSONObject object1 = new JSONObject();

                        getClaims(object);
                    }
                })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .show();
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
