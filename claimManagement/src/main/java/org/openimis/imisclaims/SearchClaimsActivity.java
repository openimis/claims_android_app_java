package org.openimis.imisclaims;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.usecase.FetchClaims;
import org.openimis.imisclaims.util.TextViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SearchClaimsActivity extends ImisActivity {
    private static final String LOG_TAG = "SEARCHCLAIMS";

    private static final String SPINNER_POSITION = "SPINNER_POSITION";
    private static final String VISIT_FROM = "VISIT_FROM";
    private static final String VISIT_TO = "VISIT_TO";
    private static final String PROCESSED_FROM = "PROCESSED_FROM";
    private static final String PROCESSED_TO = "PROCESSED_TO";

    private final Calendar visitDateFromCalendar = Calendar.getInstance();
    private final Calendar visitDateToCalendar = Calendar.getInstance();
    private final Calendar dateProcessedFromCalendar = Calendar.getInstance();
    private final Calendar dateProcessedToCalendar = Calendar.getInstance();

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_claims);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SearchClaims));
        }

        spinner = findViewById(R.id.spinner_status);
        List<String> categories = new ArrayList<>();
        categories.add("Select claim status");
        categories.add(getString(R.string.Entered));
        categories.add(getString(R.string.Checked));
        categories.add(getString(R.string.Processed));
        categories.add(getString(R.string.Valuated));
        categories.add(getString(R.string.Rejected));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        EditText visitDateFrom = findViewById(R.id.visit_date_from);
        EditText visitDateTo = findViewById(R.id.visit_date_to);
        EditText dateProcessedFrom = findViewById(R.id.date_processed_from);
        EditText dateProcessedTo = findViewById(R.id.date_processed_to);

        if (savedInstanceState != null) {
            spinner.setSelection(savedInstanceState.getInt(SPINNER_POSITION));
            visitDateFromCalendar.setTimeInMillis(savedInstanceState.getLong(VISIT_FROM));
            TextViewUtils.setDate(visitDateFrom, visitDateFromCalendar.getTime());
            visitDateToCalendar.setTimeInMillis(savedInstanceState.getLong(VISIT_TO));
            TextViewUtils.setDate(visitDateTo, visitDateToCalendar.getTime());
            dateProcessedFromCalendar.setTimeInMillis(savedInstanceState.getLong(PROCESSED_FROM));
            TextViewUtils.setDate(dateProcessedFrom, dateProcessedFromCalendar.getTime());
            dateProcessedToCalendar.setTimeInMillis(savedInstanceState.getLong(PROCESSED_TO));
            TextViewUtils.setDate(dateProcessedTo, dateProcessedToCalendar.getTime());
        }

        findViewById(R.id.clear).setOnClickListener(view -> {
            visitDateFrom.setText("");
            visitDateTo.setText("");
            dateProcessedFrom.setText("");
            dateProcessedTo.setText("");
            spinner.setSelection(0);
        });

        findViewById(R.id.search).setOnClickListener(view ->
                doLoggedIn(() -> getClaims(
                                global.getOfficerCode(),
                                getStatus(spinner),
                                getDate(visitDateFrom, visitDateFromCalendar),
                                getDate(visitDateTo, visitDateToCalendar),
                                getDate(dateProcessedFrom, dateProcessedFromCalendar),
                                getDate(dateProcessedTo, dateProcessedToCalendar)
                        )
                ));

        visitDateFrom.setOnClickListener(v -> getDatePicker(visitDateFrom, visitDateFromCalendar).show());
        visitDateTo.setOnClickListener(v -> getDatePicker(visitDateTo, visitDateToCalendar).show());
        dateProcessedFrom.setOnClickListener(v -> getDatePicker(dateProcessedFrom, dateProcessedFromCalendar).show());
        dateProcessedTo.setOnClickListener(v -> getDatePicker(dateProcessedTo, dateProcessedToCalendar).show());
    }

    public DatePickerDialog getDatePicker(TextView textView, Calendar calendar) {
        return new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    TextViewUtils.setDate(textView, calendar.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    @Nullable
    private Claim.Status getStatus(Spinner spinner) {
        switch (spinner.getSelectedItemPosition()) {
            case 1:
                return Claim.Status.ENTERED;
            case 2:
                return Claim.Status.CHECKED;
            case 3:
                return Claim.Status.PROCESSED;
            case 4:
                return Claim.Status.VALUATED;
            case 5:
                return Claim.Status.REJECTED;
            default:
                return null;
        }
    }

    @Nullable
    private Date getDate(@NonNull EditText editText, @NonNull Calendar calendar) {
        if (editText.length() != 0) {
            return calendar.getTime();
        }
        return null;
    }

    private void getClaims(
            @Nullable String claimAdministratorCode,
            @Nullable Claim.Status status,
            @Nullable Date visitDateFrom,
            @Nullable Date visitDateTo,
            @Nullable Date processedDateFrom,
            @Nullable Date processedDateTo
    ) {
        ProgressDialog pd = ProgressDialog.show(this, getResources().getString(R.string.DownLoad), getResources().getString(R.string.getClaims) + "...");
        new Thread(() -> {

            try {
                List<Claim> claims = new FetchClaims().execute(
                        claimAdministratorCode, status, visitDateFrom,
                        visitDateTo, processedDateFrom, processedDateTo
                );
                pd.dismiss();
                if (!claims.isEmpty()) {
                    openClaimReview(claims);
                } else {
                    runOnUiThread(() -> Toast.makeText(getContext(), getResources().getString(R.string.NoClaim), Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                pd.dismiss();
                Log.e(LOG_TAG, "Error while fetching claims", e);
                runOnUiThread(() -> Toast.makeText(getContext(), getResources().getString(R.string.ErrorOccurred) + ": " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    public void openClaimReview(List<Claim> claims) {
        startActivity(Claims.newIntent(this, claims));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(SPINNER_POSITION, spinner.getSelectedItemPosition());
        outState.putLong(VISIT_FROM, visitDateFromCalendar.getTimeInMillis());
        outState.putLong(VISIT_TO, visitDateToCalendar.getTimeInMillis());
        outState.putLong(PROCESSED_FROM, dateProcessedFromCalendar.getTimeInMillis());
        outState.putLong(PROCESSED_TO, dateProcessedToCalendar.getTimeInMillis());
    }
}
