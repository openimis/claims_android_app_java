package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ListAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openimis.imisclaims.domain.entity.Insuree;
import org.openimis.imisclaims.domain.entity.Policy;
import org.openimis.imisclaims.usecase.FetchInsureeInquire;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class EnquireActivityTest {

    EnquireActivity activity;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        activity = Robolectric.buildActivity(EnquireActivity.class)
                .create()
                .start()
                .resume()
                .get();

        // Inject mocked global
        activity.global = mock(Global.class);
        when(activity.global.isNetworkAvailable()).thenReturn(true);

        // Default CHFID
        activity.etCHFID.setText("CHF123");
    }

    /* =====================================================
       buildEnquireValue()
       ===================================================== */

    @Test
    public void buildEnquireValue_WhenNull_ReturnsEmptyString() {
        String result = activity.buildEnquireValue(null, R.string.totalVisitsLeft);
        assertEquals("", result);
    }

    @Test
    public void buildEnquireValue_WhenValueProvided_ReturnsFormattedString() {
        String result = activity.buildEnquireValue(5, R.string.totalVisitsLeft);
        assertEquals("TotalVisitsLeft: 5", result);
    }

    /* =====================================================
       renderResult()
       ===================================================== */

    @Test
    public void renderResult_WhenInsureeIsNull_ShowsNotFoundDialog() {
        EnquireActivity spy = spy(activity);

        spy.renderResult(null);

        verify(spy, atLeastOnce()).showDialog(
                activity.getResources().getString(R.string.RecordNotFound)
        );
    }

    @Test
    public void renderResult_WhenCHFIDMismatch_DoesNothing() {
        Insuree insuree = mock(Insuree.class);
        when(insuree.getChfId()).thenReturn("DIFFERENT");

        activity.renderResult(insuree);

        assertEquals(
                activity.getResources().getString(R.string.CHFID),
                activity.tvCHFID.getText().toString()
        );
    }

    @Test
    public void renderResult_WhenValidInsuree_PopulatesUI() {
        Insuree insuree = mock(Insuree.class);

        when(insuree.getChfId()).thenReturn("CHF123");
        when(insuree.getName()).thenReturn("John Doe");
        when(insuree.getGender()).thenReturn("M");
        when(insuree.getDateOfBirth()).thenReturn(new Date());
        when(insuree.getPhoto()).thenReturn(null);
        when(insuree.getPhotoPath()).thenReturn(null);
        when(insuree.getPolicies()).thenReturn(Collections.emptyList());

        activity.renderResult(insuree);

        assertEquals("CHF123", activity.tvCHFID.getText().toString());
        assertEquals("John Doe", activity.tvName.getText().toString());
        assertEquals("M", activity.tvGender.getText().toString());
        assertEquals(View.VISIBLE, activity.ll.getVisibility());
    }

    @Test
    public void renderResult_WhenPhotoBytesPresent_DisplaysBitmap() {
        Insuree insuree = mock(Insuree.class);

        byte[] fakePhoto = new byte[10];

        when(insuree.getChfId()).thenReturn("CHF123");
        when(insuree.getName()).thenReturn("John Doe");
        when(insuree.getGender()).thenReturn("M");
        when(insuree.getDateOfBirth()).thenReturn(new Date());
        when(insuree.getPhoto()).thenReturn(fakePhoto);
        when(insuree.getPolicies()).thenReturn(Collections.emptyList());

        activity.renderResult(insuree);

        assertNotNull(activity.iv.getDrawable());
    }

    /* =====================================================
       renderResult() with policies
       ===================================================== */

    @Test
    public void renderResult_WithPolicies_PopulatesListView() {
        Policy policy = mock(Policy.class);

        when(policy.getCode()).thenReturn("PROD1");
        when(policy.getName()).thenReturn("Basic Cover");
        when(policy.getStatus()).thenReturn(Policy.Status.ACTIVE);
        when(policy.getExpiryDate()).thenReturn(new Date());
        when(policy.getDeductibleType()).thenReturn(1.0);
        when(policy.getDeductibleIp()).thenReturn(10.0);
        when(policy.getCeilingIp()).thenReturn(100.0);

        List<Policy> policies = new ArrayList<>();
        policies.add(policy);

        Insuree insuree = mock(Insuree.class);
        when(insuree.getChfId()).thenReturn("CHF123");
        when(insuree.getName()).thenReturn("John Doe");
        when(insuree.getGender()).thenReturn("M");
        when(insuree.getDateOfBirth()).thenReturn(new Date());
        when(insuree.getPhoto()).thenReturn(null);
        when(insuree.getPolicies()).thenReturn(policies);

        activity.renderResult(insuree);

        ListAdapter adapter = activity.lv.getAdapter();
        assertNotNull(adapter);
        assertEquals(1, adapter.getCount());
    }

    /* =====================================================
       getInsureeInfo()
       ===================================================== */

    @Test
    public void getInsureeInfo_WhenOffline_UsesLocalDb() {
        EnquireActivity spy = spy(activity);

        when(spy.global.isNetworkAvailable()).thenReturn(false);
        doReturn(null).when(spy).getDataFromDb(anyString());

        spy.getInsureeInfo();

        verify(spy).getDataFromDb("CHF123");
    }

    @Test
    public void getInsureeInfo_WhenOnline_RecordNotFound_ShowsDialog() throws Exception {
        EnquireActivity spy = spy(activity);

        when(spy.global.isNetworkAvailable()).thenReturn(true);
        
        when(spy.createFetchInsureeInquire(anyString()))
                .thenThrow(new org.openimis.imisclaims.network.exception.HttpException(
                        404, "Not Found", null, null
                ));

        spy.getInsureeInfo();

        verify(spy, atLeastOnce()).showDialog(
                activity.getResources().getString(R.string.RecordNotFound)
        );
    }
}
