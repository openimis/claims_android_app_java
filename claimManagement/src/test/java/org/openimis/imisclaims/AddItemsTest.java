package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.database.Cursor;
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
public class AddItemsTest {

    private AddItems activity;
    private AutoCompleteTextView etItems;
    private EditText etQuantity;
    private EditText etAmount;
    private Button btnAdd;
    private SQLHandler mockSqlHandler;

    @Before
    public void setup() {
        mockSqlHandler = mock(SQLHandler.class);

        // Initialize static list BEFORE creating activity
        ClaimActivity.lvItemList = new java.util.ArrayList<>();

        activity = Robolectric.buildActivity(AddItems.class).create().get();
        activity.sqlHandler = mockSqlHandler;

        etItems = activity.findViewById(R.id.etItems);
        etQuantity = activity.findViewById(R.id.etQuantity);
        etAmount = activity.findViewById(R.id.etAmount);
        btnAdd = activity.findViewById(R.id.btnAdd);
    }

    @Test
    public void onCreate_disablesFieldsWhenReadonly() {
        Intent intent = new Intent();
        intent.putExtra(ClaimActivity.EXTRA_READONLY, true);

        activity = Robolectric.buildActivity(AddItems.class, intent).create().get();

        etItems = activity.findViewById(R.id.etItems);
        etQuantity = activity.findViewById(R.id.etQuantity);
        etAmount = activity.findViewById(R.id.etAmount);
        btnAdd = activity.findViewById(R.id.btnAdd);

        assertFalse(etItems.isEnabled());
        assertFalse(etQuantity.isEnabled());
        assertFalse(etAmount.isEnabled());
        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_isDisabledInitially() {
        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_enablesWhenAllFieldsFilled() {
        etItems.setText("TEST001");
        etQuantity.setText("5");
        etAmount.setText("100");

        assertTrue(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenItemEmpty() {
        etItems.setText("");
        etQuantity.setText("5");
        etAmount.setText("100");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenQuantityEmpty() {
        etItems.setText("TEST001");
        etQuantity.setText("");
        etAmount.setText("100");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_disablesWhenAmountEmpty() {
        etItems.setText("TEST001");
        etQuantity.setText("5");
        etAmount.setText("");

        assertFalse(btnAdd.isEnabled());
    }

    @Test
    public void addButton_addsItemToList() {
        activity.oItem = new HashMap<>();
        activity.oItem.put("Code", "ITEM001");
        activity.oItem.put("Name", "Paracetamol");

        etQuantity.setText("10");
        etAmount.setText("500");

        int initialSize = ClaimActivity.lvItemList.size();
        btnAdd.performClick();

        assertEquals(initialSize + 1, ClaimActivity.lvItemList.size());

        HashMap<String, String> addedItem = ClaimActivity.lvItemList.get(initialSize);
        assertEquals("ITEM001", addedItem.get("Code"));
        assertEquals("Paracetamol", addedItem.get("Name"));
        assertEquals("500", addedItem.get("Price"));
        assertEquals("10", addedItem.get("Quantity"));
    }

    @Test
    public void addButton_usesDefaultQuantityWhenEmpty() {
        activity.oItem = new HashMap<>();
        activity.oItem.put("Code", "ITEM001");
        activity.oItem.put("Name", "Paracetamol");

        etQuantity.setText("");
        etAmount.setText("500");

        btnAdd.performClick();

        HashMap<String, String> addedItem = ClaimActivity.lvItemList.get(0);
        assertEquals("1", addedItem.get("Quantity"));
    }

    @Test
    public void addButton_clearsFieldsAfterAdding() {
        activity.oItem = new HashMap<>();
        activity.oItem.put("Code", "ITEM001");
        activity.oItem.put("Name", "Paracetamol");

        etItems.setText("TEST");
        etQuantity.setText("10");
        etAmount.setText("500");

        btnAdd.performClick();

        assertEquals("", etItems.getText().toString());
        assertEquals("", etQuantity.getText().toString());
        assertEquals("", etAmount.getText().toString());
    }

    @Test
    public void addButton_doesNothingWhenItemNotSelected() {
        activity.oItem = null;

        etQuantity.setText("10");
        etAmount.setText("500");

        int initialSize = ClaimActivity.lvItemList.size();
        btnAdd.performClick();

        assertEquals(initialSize, ClaimActivity.lvItemList.size());
    }
}