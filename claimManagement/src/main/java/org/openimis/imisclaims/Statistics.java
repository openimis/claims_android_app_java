package org.openimis.imisclaims;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.openimis.CallSoap.CallSoap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Statistics extends Activity {

    EditText etFromDate, etToDate;
    ListView lvStats;
    Button btnOpt;
    ProgressDialog pd;
    int year, month, day;
    static final int FromDate_Dialog_ID = 0;
    static final int ToDate_Dialog_ID = 1;
    final Calendar cal = Calendar.getInstance();
    String HFCode, ClaimAdmin;
    ArrayList<HashMap<String,String>>ClaimStats = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        etFromDate = (EditText)findViewById(R.id.etFromDate);
        etToDate = (EditText)findViewById(R.id.etToDate);
        lvStats = (ListView)findViewById(R.id.lvStats);
        btnOpt = (Button)findViewById(R.id.btnOpt);


        btnOpt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openOptionsMenu();
            }
        });

        Intent intent = getIntent();
        if (intent.getExtras() == null){
            HFCode = "";
            ClaimAdmin = "";
        }else{
            HFCode = intent.getExtras().get("HFCode").toString();
            ClaimAdmin = intent.getExtras().get("ClaimAdmin").toString();
        }

        etFromDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialog(FromDate_Dialog_ID);
                return false;
            }
        });

        etToDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialog(ToDate_Dialog_ID);
                return false;
            }
        });

    }


    private DatePickerDialog.OnDateSetListener FromDatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth,int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;

            etFromDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));

            if(etToDate.getText().length()==0){
                etToDate.setText(etFromDate.getText().toString());
            }
        }
    };


    private DatePickerDialog.OnDateSetListener ToDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;

            etToDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));


        }
    };


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case FromDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DATE);

                return new DatePickerDialog(this,FromDatePickerListener,year, month,day);

            case ToDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DATE);

                return new DatePickerDialog(this,ToDatePickerListener,year,month,day);

        }
        return  null;
    }

    private boolean isValidData(){
        if(etFromDate.getText().length() == 0){
            ShowDialog(getResources().getString(R.string.MissingStartDate));
            return false;
        }
        if(etToDate.getText().length()==0){
            ShowDialog(getResources().getString(R.string.MissingEndDate));
            return false;

        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuGetStats:

                if (!isValidData()) return false;

                pd = new ProgressDialog(this);
                pd.setCancelable(true);
                pd = ProgressDialog.show(this,"",getResources().getString(R.string.InProgress));

                new Thread(){
                    public void run(){
                        GetStatistics();
                        pd.dismiss();
                    }

                }.start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void GetStatistics(){

        ClaimStats = new ArrayList<HashMap<String, String>>();

        CallSoap cs = new CallSoap();
        cs.setFunctionName("GetClaimStats");
        Date FromDate, ToDate;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        FromDate = new Date();
        ToDate = new Date();
        try {
            FromDate = dateFormat.parse(String.valueOf(etFromDate.getText()));
            ToDate = dateFormat.parse(String.valueOf(etToDate.getText()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final String stats = cs.GetClaimStats(HFCode, ClaimAdmin, FromDate, ToDate);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                try {
                    JSONArray jsonArray = new JSONArray(stats);
                    if(jsonArray.length() == 0){
                        ShowDialog(getResources().getString(R.string.NoStatFound));
                    }else{

                            JSONObject jsonObject = new JSONObject();
                            jsonObject = jsonArray.getJSONObject(0);

                        HashMap<String,String> data = new HashMap<String, String>();
                        data.put("Label","Entered");
                        data.put("Value", String.valueOf(jsonObject.get("Entered")));
                        ClaimStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label","Submitted");
                        data.put("Value", String.valueOf(jsonObject.get("Submitted")));
                        ClaimStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label","Rejected");
                        data.put("Value", String.valueOf(jsonObject.get("Rejected")));
                        ClaimStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label","Processed");
                        data.put("Value", String.valueOf(jsonObject.get("Processed")));
                        ClaimStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label","Valuated");
                        data.put("Value", String.valueOf(jsonObject.get("Valuated")));
                        ClaimStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label","Remunerated");
                        data.put("Value", String.valueOf(jsonObject.get("Remunerated")));
                        ClaimStats.add(data);

                        ListAdapter adapter = new SimpleAdapter(Statistics.this,
                                ClaimStats,
                                R.layout.lvstats,
                                new String[]{"Label","Value"},
                                new int[]{R.id.tvStatsLabel, R.id.tvStats}
                        );

                        lvStats.setAdapter(adapter);
                        }


                    }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    protected AlertDialog ShowDialog(String msg){
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

}
