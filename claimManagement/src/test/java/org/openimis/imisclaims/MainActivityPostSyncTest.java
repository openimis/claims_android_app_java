package org.openimis.imisclaims;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
public class MainActivityPostSyncTest {

    private TestMainActivity activity;

    @Before
    public void setup() {
        activity = new TestMainActivity();
    }

    @Test
    public void downloadAllData_showsPartialMedicationDialog_whenSkippedMedicationPagesNotEmpty() {
        activity.handlePostMasterDataSync("OFFICER", Arrays.asList(2, 4));

        assertTrue(activity.dialogShown);
        assertNotNull(activity.lastDialogMessage);
        assertTrue(activity.lastDialogMessage.contains("partial medications"));
        assertTrue(activity.lastDialogMessage.contains("[2, 4]"));
    }

    @Test
    public void downloadAllData_doesNotAutoCallDownloadPriceList_beforeDialogConfirmation_whenSkippedPagesExist() {
        activity.handlePostMasterDataSync("OFFICER", Collections.singletonList(2));

        assertTrue(activity.dialogShown);
        assertFalse(activity.downloadCalled);
    }

    @Test
    public void downloadAllData_callsDownloadPriceListDirectly_whenNoSkippedPages_andOfficerCodePresent() {
        activity.handlePostMasterDataSync("OFFICER", Collections.emptyList());

        assertTrue(activity.downloadCalled);
        assertEquals("OFFICER", activity.downloadCalledWith);
        assertFalse(activity.dialogShown);
    }

    @Test
    public void downloadAllData_afterDialogConfirmation_callsDownloadPriceList_whenOfficerCodePresent() {
        activity.handlePostMasterDataSync("OFFICER", Collections.singletonList(3));

        assertFalse(activity.downloadCalled);
        activity.lastOkCallback.onClick(null, 0);

        assertTrue(activity.downloadCalled);
        assertEquals("OFFICER", activity.downloadCalledWith);
    }

    public static class TestMainActivity extends MainActivity {
        boolean dialogShown = false;
        boolean downloadCalled = false;
        String downloadCalledWith;
        String lastDialogMessage;
        DialogInterface.OnClickListener lastOkCallback;

        @Override
        protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback) {
            this.dialogShown = true;
            this.lastDialogMessage = msg;
            this.lastOkCallback = okCallback;
            return null;
        }

        @Override
        protected void DownLoadServicesItemsPriceList(String claimAdministratorCode) {
            this.downloadCalled = true;
            this.downloadCalledWith = claimAdministratorCode;
        }
    }
}
