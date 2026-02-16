package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}, application = Global.class)
public class AddServicesTest {

    private AddServices activity;
    private AutoCompleteTextView etServices;
    private EditText etSQuantity;
    private EditText etSAmount;
    private Button btnAdd;
    private SQLHandler mockSqlHandler;

    @Before
    public void setup() {
        mockSqlHandler = mock(SQLHandler.class);

        // Initialize static list BEFORE creating activity
        ClaimActivity.lvServiceList = new java.util.ArrayList<>();

        activity = Robolectric.buildActivity(AddServices.class).create().get();
        activity.sqlHandler = mockSqlHandler;

        etServices = activity.findViewById(R.id.etService);
        etSQuantity = activity.findViewById(R.id.etSQuantity);
        etSAmount = activity.findViewById(R.id.etSAmount);
        btnAdd = activity.findViewById(R.id.btnAdd);
    }

    @Test
    public void onCreate_disablesFieldsWhenReadonly() {
        Intent intent = new Intent();
        intent.putExtra(ClaimActivity.EXTRA_READONLY, true);

        activity = Robolectric.buildActivity(AddServices.class, intent).create().get();

        etServices = activity.findViewById(R.id.etService);
        etSQuantity = activity.findViewById(R.id.etSQuantity);
        etSAmount = activity.findViewById(R.id.etSAmount);
        btnAdd = activity.findViewById(R.id.btnAdd);

        assertFalse(etServices.isEnabled());
        assertFalse(etSQuantity.isEnabled());
        assertFalse(etSAmount.isEnabled());
        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_isDisabledInitially() {
        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_enablesWhenAllFieldsFilled() {
        etServices.setText("SRV001");
        etSQuantity.setText("2");
        etSAmount.setText("5000");

        assertTrue(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenServiceEmpty() {
        etServices.setText("");
        etSQuantity.setText("2");
        etSAmount.setText("5000");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenQuantityEmpty() {
        etServices.setText("SRV001");
        etSQuantity.setText("");
        etSAmount.setText("5000");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenAmountEmpty() {
        etServices.setText("SRV001");
        etSQuantity.setText("2");
        etSAmount.setText("");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_addsServiceToList() {
        activity.oService = new HashMap<>();
        activity.oService.put("Code", "SRV001");
        activity.oService.put("Name", "Consultation");
        activity.oService.put("PackageType", "S");

        etSQuantity.setText("1");
        etSAmount.setText("2000");

        int initialSize = ClaimActivity.lvServiceList.size();
        btnAdd.performClick();

        assertEquals(initialSize + 1, ClaimActivity.lvServiceList.size());

        HashMap<String, String> addedService = ClaimActivity.lvServiceList.get(initialSize);
        assertEquals("SRV001", addedService.get("Code"));
        assertEquals("Consultation", addedService.get("Name"));
        assertEquals("2000", addedService.get("Price"));
        assertEquals("1", addedService.get("Quantity"));
        assertEquals("S", addedService.get("PackageType"));
    }

    @Test
    public void addButton_usesDefaultQuantityWhenEmpty() {
        activity.oService = new HashMap<>();
        activity.oService.put("Code", "SRV001");
        activity.oService.put("Name", "Consultation");
        activity.oService.put("PackageType", "S");

        etSQuantity.setText("");
        etSAmount.setText("2000");

        btnAdd.performClick();

        HashMap<String, String> addedService = ClaimActivity.lvServiceList.get(0);
        assertEquals("1", addedService.get("Quantity"));
    }

    @Test
    public void addButton_clearsFieldsAfterAdding() {
        activity.oService = new HashMap<>();
        activity.oService.put("Code", "SRV001");
        activity.oService.put("Name", "Consultation");
        activity.oService.put("PackageType", "S");

        etServices.setText("TEST");
        etSQuantity.setText("1");
        etSAmount.setText("2000");

        btnAdd.performClick();

        assertEquals("", etServices.getText().toString());
        assertEquals("", etSQuantity.getText().toString());
        assertEquals("", etSAmount.getText().toString());
    }

    @Test
    public void addButton_doesNothingWhenServiceNotSelected() {
        activity.oService = null;

        etSQuantity.setText("1");
        etSAmount.setText("2000");

        int initialSize = ClaimActivity.lvServiceList.size();
        btnAdd.performClick();

        assertEquals(initialSize, ClaimActivity.lvServiceList.size());
    }
}