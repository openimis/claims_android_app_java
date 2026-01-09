package org.openimis.imisclaims;
import org.openimis.imisclaims.domain.entity.Claim;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.ContentValues;
import android.text.SpannableStringBuilder;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ClaimActivityTest {

    ClaimActivity activity;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        activity = Robolectric.buildActivity(ClaimActivity.class)
                .create()
                .start()
                .resume()
                .get();

        // Inject mocks
        activity.sqlHandler = mock(SQLHandler.class);
        activity.global = mock(Global.class);

        when(activity.global.isNetworkAvailable()).thenReturn(true);
        when(activity.sqlHandler.getAdjustability(anyString())).thenReturn("M");

        // initialize static lists
        ClaimActivity.lvItemList = new ArrayList<>();
        ClaimActivity.lvServiceList = new ArrayList<>();

        // set mandatory fields
        activity.etHealthFacility.setText("HF001");
        activity.etClaimAdmin.setText("ADMIN001");
        activity.etClaimCode.setText("CLM001");
        activity.etInsureeNumber.setText("CHF12345");
        activity.etStartDate.setText("2025-01-01");
        activity.etEndDate.setText("2025-01-02");
        activity.etDiagnosis.setText("A01");

        activity.etVisitType.setText("Other");
        activity.etVisitType.setTag("O");

        activity.etPatientCondition.setText("Healed");
        activity.etPatientCondition.setTag("H");

        activity.tvItemTotal.setText("1");
        activity.tvServiceTotal.setText("0");

        // add fake items and services to avoid MissingClaim
        ArrayList<HashMap<String, String>> fakeItems = new ArrayList<>();
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("Code", "ITEM001");
        item1.put("Price", "50");
        item1.put("Quantity", "1");
        fakeItems.add(item1);
        ClaimActivity.lvItemList = fakeItems;

        ArrayList<HashMap<String, String>> fakeServices = new ArrayList<>();
        HashMap<String, String> svc1 = new HashMap<>();
        svc1.put("Code", "SVC001");
        svc1.put("Price", "40");
        svc1.put("Quantity", "1");
        svc1.put("PackageType", "P");
        svc1.put("SubServicesItems", "[]");
        fakeServices.add(svc1);
        ClaimActivity.lvServiceList = fakeServices;
    }

    /* =====================================================
       isValidData()
       ===================================================== */

    @Test
    public void isValidData_AllValid_ReturnsTrue() {
        boolean result = activity.isValidData();
        assertTrue(result);
    }

    @Test
    public void isValidData_MissingHealthFacility_ReturnsFalse() {
        activity.etHealthFacility.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingClaimAdmin_WhenMandatory_ReturnsFalse() {
        when(activity.sqlHandler.getAdjustability("ClaimAdministrator"))
                .thenReturn("M");

        activity.etClaimAdmin.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingClaimCode_ReturnsFalse() {
        activity.etClaimCode.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingInsureeNumber_ReturnsFalse() {
        activity.etInsureeNumber.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_InvalidInsureeNumber_ReturnsFalse() {
        activity.etInsureeNumber.setText("");
        Escape escape = mock(Escape.class);
        when(escape.CheckCHFID(anyString())).thenReturn(false); // Mock Escape.CheckCHFID to return false
        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingStartDate_ReturnsFalse() {
        activity.etStartDate.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingEndDate_ReturnsFalse() {
        activity.etEndDate.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingDiagnosis_ReturnsFalse() {
        activity.etDiagnosis.setText("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_MissingVisitType_ReturnsFalse() {
        activity.etVisitType.setText("");
        activity.etVisitType.setTag("");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    @Test
    public void isValidData_NoItemsAndServices_ReturnsFalse() {
        ClaimActivity.lvItemList.clear();
        ClaimActivity.lvServiceList.clear();
        activity.tvItemTotal.setText("0");
        activity.tvServiceTotal.setText("0");

        boolean result = activity.isValidData();

        assertFalse(result);
    }

    /* =====================================================
       saveClaim()
       ===================================================== */

    @Test
    public void saveClaim_ValidData_CallsSqlHandlerAndReturnsTrue() {
        doNothing().when(activity.sqlHandler)
                .saveClaim(any(ContentValues.class), anyList(), anyList());

        boolean result = activity.saveClaim();

        assertTrue(result);
        verify(activity.sqlHandler, times(1))
                .saveClaim(any(ContentValues.class), anyList(), anyList());
    }

    @Test
    public void saveClaim_ContentValuesContainExpectedData() {
        doNothing().when(activity.sqlHandler)
                .saveClaim(any(ContentValues.class), anyList(), anyList());

        activity.saveClaim();

        verify(activity.sqlHandler).saveClaim(
                argThat(cv ->
                        cv.getAsString("HFCode").equals("HF001") &&
                        cv.getAsString("ClaimCode").equals("CLM001") &&
                        cv.getAsString("VisitType").equals("O") &&
                        cv.getAsInteger("PreAuthorization") == 0
                ),
                anyList(),
                anyList()
        );
    }

    /* =====================================================
   fillClaimFromRestore()
   ===================================================== */

    @Test
    public void fillClaimFromRestore_FillsAllFieldsCorrectly() throws Exception {
        // Build a complete claim
        Claim.Medication med = new Claim.Medication(
                "MED1", "Paracetamol", 50.0, "USD", "2", null, null, null, null, null
        );

        Claim.Service svc = new Claim.Service(
                "SVC1", "Consultation", 100.0, "USD", "1", null, null, null, null, null
        );

        Claim claim = new Claim(
                "UUID123",
                "HF001",
                "Health Facility",
                "INS001",
                "John Doe",
                "CLM123",
                new Date(),
                new Date(),
                new Date(),
                "E",
                Claim.Status.ENTERED,
                "A01",
                "B02",
                null,
                null,
                null,
                150.0,
                150.0,
                null,
                null,
                "G001",
                List.of(svc),
                List.of(med)
        );

        // Mock global for the method
        when(activity.global.getOfficerCode()).thenReturn("ADMIN001");
        when(activity.global.getOfficerHealthFacility()).thenReturn("HF001");

        // Call the private method using reflection
        java.lang.reflect.Method method = ClaimActivity.class
                .getDeclaredMethod("fillClaimFromRestore", Claim.class);
        method.setAccessible(true);
        method.invoke(activity, claim);

        // Verify that UI fields were filled correctly
        assertEquals("@CLM123", activity.etClaimCode.getText().toString()); // @ is the mocks resource string to replace "Restored"
        assertEquals("ADMIN001", activity.etClaimAdmin.getText().toString());
        assertEquals("HF001", activity.etHealthFacility.getText().toString());
        assertEquals("G001", activity.etGuaranteeNo.getText().toString());
        assertEquals("", activity.etInsureeNumber.getText().toString()); // because Status != REJECTED
        assertEquals("", activity.etDiagnosis.getText().toString());

        // Verify that the lists were populated
        assertEquals(1, ClaimActivity.lvItemList.size());
        assertEquals(1, ClaimActivity.lvServiceList.size());
        assertEquals(2, activity.TotalItemService); // 1 item + 1 service
    }

}
