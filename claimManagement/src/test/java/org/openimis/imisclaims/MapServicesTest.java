package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.AlertDialog;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}, application = Global.class)
public class MapServicesTest {

    SQLHandler mockSqlHandler;
    Cursor mockCursor;
    MapServices activity;

    @Before
    public void setup() {
        // Création des mocks avec la syntaxe moderne
        mockSqlHandler = mock(SQLHandler.class);
        mockCursor = mock(Cursor.class);

        activity = Robolectric.buildActivity(MapServices.class).create().get();
        activity.sqlHandler = mockSqlHandler;
    }

    @Test
    public void bindServiceList_loadsServicesCorrectly() {
        doReturn(mockCursor).when(activity.sqlHandler).getMapping("S");
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.isAfterLast()).thenReturn(false, true);
        when(mockCursor.getString(0)).thenReturn("SRV001");
        when(mockCursor.getString(1)).thenReturn("Consultation");
        when(mockCursor.getString(2)).thenReturn(null);

        activity.BindItemList();

        assertEquals(1, activity.ServiceList.size());
        HashMap<String, Object> service = activity.ServiceList.get(0);
        assertEquals("SRV001", service.get("Code"));
        assertEquals("Consultation", service.get("Name"));
        assertFalse((Boolean) service.get("isMapped"));
        verify(mockCursor).close();
    }

    @Test
    public void clickingService_togglesIsMapped() {
        HashMap<String, Object> service = new HashMap<>();
        service.put("Code", "SRV001");
        service.put("Name", "Test Service");
        service.put("isMapped", false);
        activity.ServiceList.add(service);

        activity.alAdapter = activity.new ServiceAdapter(activity, activity.ServiceList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});

        activity.lvMapServices.setAdapter(activity.alAdapter);

        activity.lvMapServices.performItemClick(null, 0, 0);

        assertTrue((Boolean) activity.ServiceList.get(0).get("isMapped"));
    }

    @Test
    public void checkAll_setsAllServicesMapped() {
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> service = new HashMap<>();
            service.put("Code", "SRV00" + i);
            service.put("Name", "Service " + i);
            service.put("isMapped", false);
            activity.ServiceList.add(service);
        }

        activity.alAdapter = activity.new ServiceAdapter(activity, activity.ServiceList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});

        activity.CheckUncheckAll(true);

        for (HashMap<String, Object> service : activity.ServiceList) {
            assertTrue((Boolean) service.get("isMapped"));
        }
    }

    @Test
    public void save_returnsCorrectCodes() {
        HashMap<String, Object> service = new HashMap<>();
        service.put("Code", "SRV001");
        service.put("Name", "Test Service");
        service.put("isMapped", false);
        activity.ServiceList.add(service);

        when(mockSqlHandler.InsertMapping(anyString(), anyString(), anyString())).thenReturn(true);

        int result = activity.Save();
        assertEquals(1, result);

        service.put("isMapped", true);
        result = activity.Save();
        assertEquals(0, result);
    }

    @Test
    public void save_returns2_whenInsertFails() {
        HashMap<String, Object> service = new HashMap<>();
        service.put("Code", "SRV001");
        service.put("Name", "Test Service");
        service.put("isMapped", true);
        activity.ServiceList.add(service);

        when(mockSqlHandler.InsertMapping(anyString(), anyString(), anyString())).thenReturn(false);
        doNothing().when(mockSqlHandler).ClearMapping(anyString());

        int result = activity.Save();
        assertEquals(2, result);
    }

    @Test
    public void filter_searchWorksCorrectly() {
        HashMap<String, Object> service1 = new HashMap<>();
        service1.put("Code", "SRV001");
        service1.put("Name", "Consultation");
        service1.put("isMapped", false);
        activity.ServiceList.add(service1);

        HashMap<String, Object> service2 = new HashMap<>();
        service2.put("Code", "SRV002");
        service2.put("Name", "Surgery");
        service2.put("isMapped", false);
        activity.ServiceList.add(service2);

        activity.alAdapter = activity.new ServiceAdapter(activity, activity.ServiceList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});

        activity.alAdapter.getFilter().filter("cons");

        assertEquals(1, activity.ServiceList.size());
        assertEquals("Consultation", activity.ServiceList.get(0).get("Name"));
    }

    @Test
    public void adapter_getView_setsCorrectValues() {
        HashMap<String, Object> service = new HashMap<>();
        service.put("Code", "SRV001");
        service.put("Name", "Test Service");
        service.put("isMapped", true);
        activity.ServiceList.add(service);

        MapServices.ServiceAdapter adapter = activity.new ServiceAdapter(activity, activity.ServiceList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});

        assertNotNull(adapter.getView(0, null, null));
    }

    @Test
    public void showDialog_createsAlertDialog() {
        AlertDialog dialog = activity.ShowDialog("Test message");
        assertNotNull(dialog);
    }

    @Test
    public void uncheckAll_setsAllServicesUnmapped() {
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> service = new HashMap<>();
            service.put("Code", "SRV00" + i);
            service.put("Name", "Service " + i);
            service.put("isMapped", true);
            activity.ServiceList.add(service);
        }

        activity.alAdapter = activity.new ServiceAdapter(activity, activity.ServiceList,
                R.layout.mappinglist,
                new String[]{"Code", "Name", "isMapped"},
                new int[]{R.id.tvMapCode, R.id.tvMapName, R.id.chkMap});

        activity.CheckUncheckAll(false);

        for (HashMap<String, Object> service : activity.ServiceList) {
            assertFalse((Boolean) service.get("isMapped"));
        }
    }

    @Test
    public void bindServiceList_loadsMultipleServicesCorrectly() {
        doReturn(mockCursor).when(activity.sqlHandler).getMapping("S");
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.isAfterLast()).thenReturn(false, false, true);
        when(mockCursor.getString(0)).thenReturn("SRV001", "SRV002");
        when(mockCursor.getString(1)).thenReturn("Consultation", "Surgery");
        when(mockCursor.getString(2)).thenReturn(null, "mapped");

        activity.BindItemList();

        assertEquals(2, activity.ServiceList.size());
        assertFalse((Boolean) activity.ServiceList.get(0).get("isMapped"));
        assertTrue((Boolean) activity.ServiceList.get(1).get("isMapped"));
        verify(mockCursor).close();
    }

    @Test
    public void save_clearsMapping_beforeInserting() {
        HashMap<String, Object> service = new HashMap<>();
        service.put("Code", "SRV001");
        service.put("Name", "Test");
        service.put("isMapped", true);
        activity.ServiceList.add(service);

        when(mockSqlHandler.InsertMapping(anyString(), anyString(), anyString())).thenReturn(true);
        doNothing().when(mockSqlHandler).ClearMapping("S");

        activity.Save();

        verify(mockSqlHandler).ClearMapping("S");
        verify(mockSqlHandler).InsertMapping("SRV001", "Test", "S");
    }
}