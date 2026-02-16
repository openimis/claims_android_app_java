package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.text.SpannableStringBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openimis.imisclaims.tools.StorageManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class ClaimActivityTest {
    @Mock
    Global global;
    
    @Mock
    SQLHandler sqlHandler;
    
    @Mock
    Activity activity;
    
    @Mock
    Resources resources;
    
    @Mock
    StorageManager storageManager;
    
    ClaimActivity claimActivity;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(activity.getResources()).thenReturn(resources);
        when(resources.getString(anyInt())).thenReturn("mockString");
        claimActivity = Robolectric.buildActivity(ClaimActivity.class).create().start().resume().visible().get();
        claimActivity.sqlHandler = sqlHandler;
        claimActivity.global = global;
        when(global.isNetworkAvailable()).thenReturn(true); // or false, it doesn't matter
    }

    @Test
    public void testSaveClaim_ShouldInsertInDatabase() {
        EditText etHF = mock(EditText.class);
        EditText etClaimAdmin = mock(EditText.class);
        EditText etClaimCode = mock(EditText.class);
        EditText etGuaranteeNo = mock(EditText.class);
        EditText etCHFID = mock(EditText.class);
        EditText etStart = mock(EditText.class);
        EditText etEnd = mock(EditText.class);

        AutoCompleteTextView etDiagnosis = mock(AutoCompleteTextView.class);
        AutoCompleteTextView etDiagnosis1 = mock(AutoCompleteTextView.class);
        AutoCompleteTextView etDiagnosis2 = mock(AutoCompleteTextView.class);
        AutoCompleteTextView etDiagnosis3 = mock(AutoCompleteTextView.class);
        AutoCompleteTextView etDiagnosis4 = mock(AutoCompleteTextView.class);

        when(etHF.getText()).thenReturn(new SpannableStringBuilder("HF001"));
        when(etClaimAdmin.getText()).thenReturn(new SpannableStringBuilder("ADMIN001"));
        when(etClaimCode.getText()).thenReturn(new SpannableStringBuilder("CLM001"));
        when(etGuaranteeNo.getText()).thenReturn(new SpannableStringBuilder("GNT001"));
        when(etCHFID.getText()).thenReturn(new SpannableStringBuilder("INS12345"));
        when(etStart.getText()).thenReturn(new SpannableStringBuilder("2025-12-01"));
        when(etEnd.getText()).thenReturn(new SpannableStringBuilder("2025-12-09"));
        when(etDiagnosis.getText()).thenReturn(new SpannableStringBuilder("A01"));
        when(etDiagnosis1.getText()).thenReturn(new SpannableStringBuilder("B01"));
        when(etDiagnosis2.getText()).thenReturn(new SpannableStringBuilder("C01"));
        when(etDiagnosis3.getText()).thenReturn(new SpannableStringBuilder("D01"));
        when(etDiagnosis4.getText()).thenReturn(new SpannableStringBuilder("E01"));

        AutoCompleteTextView etVisitType = mock(AutoCompleteTextView.class);
        AutoCompleteTextView patientCondition = mock(AutoCompleteTextView.class);
        when(etVisitType.getTag()).thenReturn("O");
        when(patientCondition.getTag()).thenReturn("A");

        CheckBox etPreAuth = mock(CheckBox.class);
        when(etPreAuth.isChecked()).thenReturn(false);

        claimActivity.etHealthFacility = etHF;
        claimActivity.etClaimAdmin = etClaimAdmin;
        claimActivity.etClaimCode = etClaimCode;
        claimActivity.etGuaranteeNo = etGuaranteeNo;
        claimActivity.etInsureeNumber = etCHFID;
        claimActivity.etStartDate = etStart;
        claimActivity.etEndDate = etEnd;

        claimActivity.etDiagnosis = etDiagnosis;
        claimActivity.etDiagnosis1 = etDiagnosis1;
        claimActivity.etDiagnosis2 = etDiagnosis2;
        claimActivity.etDiagnosis3 = etDiagnosis3;
        claimActivity.etDiagnosis4 = etDiagnosis4;

        claimActivity.etVisitType = etVisitType;
        claimActivity.etPatientCondition = patientCondition;
        claimActivity.etPreAuthorization = etPreAuth;

        ArrayList<HashMap<String, String>> fakeItems = new ArrayList<>();
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("Code", "ITEM001");
        item1.put("Price", "50");
        item1.put("Quantity", "1");
        fakeItems.add(item1);
        claimActivity.lvItemList = fakeItems;

        ArrayList<HashMap<String, String>> fakeServices = new ArrayList<>();
        HashMap<String, String> svc1 = new HashMap<>();
        svc1.put("Code", "SVC001");
        svc1.put("Price", "40");
        svc1.put("Quantity", "1");
        svc1.put("PackageType", "P");
        svc1.put("SubServicesItems", "[]");
        fakeServices.add(svc1);
        claimActivity.lvServiceList = fakeServices;

        doNothing().when(sqlHandler).saveClaim(any(ContentValues.class), anyList(), anyList());

        boolean result = claimActivity.saveClaim();

        assertTrue(result);
        verify(sqlHandler, times(1))
                .saveClaim(any(ContentValues.class), anyList(), anyList());
    }

}
