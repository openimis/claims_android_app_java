package org.openimis.imisclaims;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchClaimsActivity extends ImisActivity {
    private static final String LOG_TAG = "SEARCHCLAIMS";
    ProgressDialog pd;

    ToRestApi toRestApi;

    EditText visitDateFrom;
    EditText visitDateTo;
    EditText dateProcessedFrom;
    EditText dateProcessedTo;
    EditText lastUpdateDate;
    EditText lastUpdateTime;
    EditText insureeNumber;

    Calendar visitDateFromCalendar;
    Calendar visitDateToCalendar;
    Calendar dateProcessedFromCalendar;
    Calendar dateProcessedToCalendar;
    Calendar lastUpdateCalendar;

    Button clear;
    Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_claims);

        toRestApi = new ToRestApi();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SearchClaims));
        }

        List<String> categories = new ArrayList<>();
        categories.add("Select claim status");
        categories.add(getString(R.string.Entered));
        categories.add(getString(R.string.Checked));
        categories.add(getString(R.string.Processed));
        categories.add(getString(R.string.Valuated));
        categories.add(getString(R.string.Rejected));

        Spinner spinner = findViewById(R.id.spinner_status);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        clear = findViewById(R.id.clear);
        clear.setOnClickListener(view -> {
            visitDateFrom.setText("");
            visitDateTo.setText("");
            dateProcessedFrom.setText("");
            dateProcessedTo.setText("");
            spinner.setSelection(0);
        });

        visitDateFrom = findViewById(R.id.visit_date_from);
        visitDateTo = findViewById(R.id.visit_date_to);
        dateProcessedFrom = findViewById(R.id.date_processed_from);
        dateProcessedTo = findViewById(R.id.date_processed_to);
        lastUpdateDate = findViewById(R.id.last_update_date);
        lastUpdateTime = findViewById(R.id.last_update_time);
        insureeNumber = findViewById(R.id.insureeNumber);


        visitDateFromCalendar = Calendar.getInstance();
        visitDateToCalendar = Calendar.getInstance();
        dateProcessedFromCalendar = Calendar.getInstance();
        dateProcessedToCalendar = Calendar.getInstance();
        lastUpdateCalendar = Calendar.getInstance();


        search = findViewById(R.id.search);
        search.setOnClickListener(view -> doLoggedIn(() -> {
            JSONObject object = new JSONObject();
            try {
                object.put("claim_administrator_code", global.getOfficerCode());
                if (spinner.getSelectedItemPosition() != 0) {
                    object.put("status_claim", categories.get(spinner.getSelectedItemPosition()));
                }
                if (visitDateFrom.length() != 0) {
                    object.put("visit_date_from", AppInformation.DateTimeInfo.getDefaultDateFormatter().format(visitDateFromCalendar.getTime()));
                }
                if (visitDateTo.length() != 0) {
                    object.put("visit_date_to", AppInformation.DateTimeInfo.getDefaultDateFormatter().format(visitDateToCalendar.getTime()));
                }
                if (dateProcessedFrom.length() != 0) {
                    object.put("processed_date_from", AppInformation.DateTimeInfo.getDefaultDateFormatter().format(dateProcessedFromCalendar.getTime()));
                }
                if (dateProcessedTo.length() != 0) {
                    object.put("processed_date_to", AppInformation.DateTimeInfo.getDefaultDateFormatter().format(dateProcessedToCalendar.getTime()));
                }
                if (lastUpdateDate.length() != 0) {
                    object.put("last_update_date", AppInformation.DateTimeInfo.getDefaultIsoShortDatetimeFormatter().format(lastUpdateCalendar.getTime()));
                }

                if (insureeNumber.length() != 0) {
                    object.put("insuree_number", insureeNumber.getText());
                }
                getClaims(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));

        visitDateFrom.setOnClickListener(v -> getDatePicker(visitDateFrom, visitDateFromCalendar).show());
        visitDateTo.setOnClickListener(v -> getDatePicker(visitDateTo, visitDateToCalendar).show());
        dateProcessedFrom.setOnClickListener(v -> getDatePicker(dateProcessedFrom, dateProcessedFromCalendar).show());
        dateProcessedTo.setOnClickListener(v -> getDatePicker(dateProcessedTo, dateProcessedToCalendar).show());
        lastUpdateDate.setOnClickListener(v -> getDatePicker(lastUpdateDate, lastUpdateCalendar).show());
        lastUpdateTime.setOnClickListener(v -> getTimePicker(lastUpdateTime, lastUpdateCalendar).show());
    }

    public DatePickerDialog getDatePicker(TextView textView, Calendar calendar) {
        return new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    updateDateLabel(calendar, textView);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public TimePickerDialog getTimePicker(TextView textView, Calendar calendar) {
        return new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0); // Time picker does not support seconds
                    updateTimeLabel(calendar, textView);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
    }

    public void updateDateLabel(Calendar calendar, TextView view) {
        SimpleDateFormat formatter = new SimpleDateFormat(AppInformation.DateTimeInfo.getDateFormat(), Locale.US);
        view.setText(formatter.format(calendar.getTime()));
    }

    public void updateTimeLabel(Calendar calendar, TextView view) {
        SimpleDateFormat formatter = new SimpleDateFormat(AppInformation.DateTimeInfo.getTimeFormat(), Locale.US);
        view.setText(formatter.format(calendar.getTime()));
    }

    private void getClaims(final JSONObject object) {
        pd = ProgressDialog.show(this, getResources().getString(R.string.DownLoad), getResources().getString(R.string.getClaims) + "...");
        new Thread(() -> {
            String functionName = "claim/GetClaims/";
            try {
                HttpResponse response = toRestApi.postToRestApiToken(object, functionName);
                String content = toRestApi.getContent(response);
                int code = response.getStatusLine().getStatusCode();

                if (code == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> pd.dismiss());
                    JSONObject jsonObject = new JSONObject(content);
                    String data = jsonObject.getString("data");
                    if (data.length() != 0) {
                        openClaimReview(content);
                    } else {
                        runOnUiThread(() -> Toast.makeText(getContext(), getResources().getString(R.string.NoClaim), Toast.LENGTH_LONG).show());
                    }
                } else {
                    pd.dismiss();
                    runOnUiThread(() -> Toast.makeText(getContext(), toRestApi.getHttpError(this, code, response.getStatusLine().getReasonPhrase(), null), Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                pd.dismiss();
                Log.e(LOG_TAG, "Error while fetching claims", e);
                runOnUiThread(() -> Toast.makeText(getContext(), getResources().getString(R.string.ErrorOccurred), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    public void openClaimReview(String claims) {
        Intent intent = new Intent(this, Claims.class);
        intent.putExtra("claims", claims);
        startActivity(intent);
    }
}
