package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.widget.EditText;
import android.widget.Spinner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.usecase.FetchClaims;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}, application = Global.class)
public class SearchClaimsActivityTest {

    private SearchClaimsActivity activity;
    private Spinner spinner;
    private EditText visitDateFrom;
    private EditText dateProcessedFrom;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(SearchClaimsActivity.class)
                .create()
                .get();

        spinner = activity.findViewById(R.id.spinner_status);
        visitDateFrom = activity.findViewById(R.id.visit_date_from);
        dateProcessedFrom = activity.findViewById(R.id.date_processed_from);
    }

    @Test
    public void getStatus_returnsCorrectStatusForAllPositions() {
        assertNull(activity.getStatus(spinner)); // Position 0 - no selection

        spinner.setSelection(1);
        assertEquals(Claim.Status.ENTERED, activity.getStatus(spinner));

        spinner.setSelection(2);
        assertEquals(Claim.Status.CHECKED, activity.getStatus(spinner));

        spinner.setSelection(3);
        assertEquals(Claim.Status.PROCESSED, activity.getStatus(spinner));

        spinner.setSelection(4);
        assertEquals(Claim.Status.VALUATED, activity.getStatus(spinner));

        spinner.setSelection(5);
        assertEquals(Claim.Status.REJECTED, activity.getStatus(spinner));
    }

    @Test
    public void getDate_returnsNullWhenEmpty_andDateWhenFilled() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JANUARY, 15);

        // Empty field returns null
        visitDateFrom.setText("");
        assertNull(activity.getDate(visitDateFrom, calendar));

        // Filled field returns date
        visitDateFrom.setText("15/01/2024");
        assertNotNull(activity.getDate(visitDateFrom, calendar));
        assertEquals(calendar.getTime(), activity.getDate(visitDateFrom, calendar));
    }

    @Test
    public void clearButton_resetsAllFields() {
        spinner.setSelection(2);
        visitDateFrom.setText("01/01/2024");
        dateProcessedFrom.setText("01/01/2024");

        activity.findViewById(R.id.clear).performClick();

        assertEquals(0, spinner.getSelectedItemPosition());
        assertEquals("", visitDateFrom.getText().toString());
        assertEquals("", dateProcessedFrom.getText().toString());
    }

}