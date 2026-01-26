package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.AlertDialog;
import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}, application = Global.class)
public class MapItemsTest {

    SQLHandler mockSqlHandler;
    Cursor mockCursor;
    MapItems activity;

    @Before
    public void setup() {
        // Création des mocks avec la syntaxe moderne
        mockSqlHandler = mock(SQLHandler.class);
        mockCursor = mock(Cursor.class);

        activity = Robolectric.buildActivity(MapItems.class).create().get();
        activity.sqlHandler = mockSqlHandler;
    }

    @Test
    public void bindItemList_loadsItemsCorrectly() {
        doReturn(mockCursor).when(activity.sqlHandler).getMapping("I");
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.isAfterLast()).thenReturn(false, true);
        when(mockCursor.getString(0)).thenReturn("ITEM001");
        when(mockCursor.getString(1)).thenReturn("Paracetamol");
        when(mockCursor.getString(2)).thenReturn(null);

        activity.BindItemList();

        assertEquals(1, activity.ItemsList.size());
        HashMap<String, Object> item = activity.ItemsList.get(0);
        assertEquals("ITEM001", item.get("Code"));
        assertEquals("Paracetamol", item.get("Name"));
        assertFalse((Boolean) item.get("isMapped"));
        verify(mockCursor).close();
    }

    @Test
    public void clickingItem_togglesIsMapped() {
        HashMap<String, Object> item = new HashMap<>();
        item.put("Code", "ITEM001");
        item.put("Name", "Test");
        item.put("isMapped", false);
        activity.ItemsList.add(item);

        activity.alAdapter = activity.new ItemAdapter(activity, activity.ItemsList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode,R.id.tvMapName,R.id.chkMap});

        activity.lvMapItems.setAdapter(activity.alAdapter);

        activity.lvMapItems.performItemClick(null, 0, 0);

        assertTrue((Boolean) activity.ItemsList.get(0).get("isMapped"));
    }

    @Test
    public void checkAll_setsAllItemsMapped() {
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("Code", "ITEM00" + i);
            item.put("Name", "Item " + i);
            item.put("isMapped", false);
            activity.ItemsList.add(item);
        }

        activity.alAdapter = activity.new ItemAdapter(activity, activity.ItemsList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode,R.id.tvMapName,R.id.chkMap});

        activity.CheckUncheckAll(true);

        for (HashMap<String, Object> item : activity.ItemsList) {
            assertTrue((Boolean)item.get("isMapped"));
        }
    }

    @Test
    public void save_returnsCorrectCodes() {
        HashMap<String,Object> item = new HashMap<>();
        item.put("Code","ITEM001");
        item.put("Name","Test");
        item.put("isMapped",false);
        activity.ItemsList.add(item);

        when(mockSqlHandler.InsertMapping(anyString(), anyString(), anyString())).thenReturn(true);

        int result = activity.Save();
        assertEquals(1, result);

        item.put("isMapped", true);
        result = activity.Save();
        assertEquals(0, result);
    }

    @Test
    public void save_returns2_whenInsertFails() {
        HashMap<String,Object> item = new HashMap<>();
        item.put("Code","ITEM001");
        item.put("Name","Test");
        item.put("isMapped", true);
        activity.ItemsList.add(item);

        when(mockSqlHandler.InsertMapping(anyString(), anyString(), anyString())).thenReturn(false);
        doNothing().when(mockSqlHandler).ClearMapping(anyString());


        int result = activity.Save();
        assertEquals(2, result);
    }

    @Test
    public void filter_searchWorksCorrectly() {
        HashMap<String,Object> item1 = new HashMap<>();
        item1.put("Code","ITEM001");
        item1.put("Name","Paracetamol");
        item1.put("isMapped", false);
        activity.ItemsList.add(item1);

        HashMap<String,Object> item2 = new HashMap<>();
        item2.put("Code","ITEM002");
        item2.put("Name","Aspirin");
        item2.put("isMapped", false);
        activity.ItemsList.add(item2);

        activity.alAdapter = activity.new ItemAdapter(activity, activity.ItemsList,
                R.layout.mappinglist,
                new String[]{"Code","Name","isMapped"},
                new int[]{R.id.tvMapCode,R.id.tvMapName,R.id.chkMap});

        activity.alAdapter.getFilter().filter("para");

        assertEquals(1, activity.ItemsList.size());
        assertEquals("Paracetamol", activity.ItemsList.get(0).get("Name"));
    }

    @Test
    public void adapter_getView_setsCorrectValues() {
        HashMap<String,Object> item = new HashMap<>();
        item.put("Code","ITEM001");
        item.put("Name","Test");
        item.put("isMapped", true);
        activity.ItemsList.add(item);

        MapItems.ItemAdapter adapter = activity.new ItemAdapter(activity, activity.ItemsList,
                R.layout.mappinglist,
                new String[]{"Code","Name","isMapped"},
                new int[]{R.id.tvMapCode,R.id.tvMapName,R.id.chkMap});

        assertNotNull(adapter.getView(0, null, null));
    }

    @Test
    public void showDialog_createsAlertDialog() {
        AlertDialog dialog = activity.ShowDialog("Test message");
        assertNotNull(dialog);
    }
}
