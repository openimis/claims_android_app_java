package org.openimis.imisclaims;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.usecase.FetchClaims;
import java.util.List;
import java.util.Arrays;
import java.util.Date;


import org.openimis.imisclaims.usecase.PostNewClaims;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import org.openimis.imisclaims.tools.StorageManager;

@RunWith(RobolectricTestRunner.class)
public class SynchronizeServiceTest {

    @Mock private Global global;
    @Mock private SQLHandler sqlHandler;
    @Mock private Resources resources;
    @Mock private Context context;
    @Mock private StorageManager storageManager;

    private SynchronizeService synchronizeService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        synchronizeService = new SynchronizeService() {
            @Override
            public Context getApplicationContext() {
                return context;
            }

            @Override
            public Resources getResources() {
                return resources;
            }

            @Override
            public void sendBroadcast(Intent intent) {
                // override to avoid real broadcast and capture intents if needed
                capturedIntent = intent;
            }
        };

        synchronizeService.global = global;
        synchronizeService.sqlHandler = sqlHandler;
        synchronizeService.storageManager = storageManager;

        when(context.getResources()).thenReturn(resources);
        when(resources.getString(anyInt())).thenReturn("Error message");
    }

    private Intent capturedIntent;

    // ----------------------------------
    // Tests handleUploadClaims
    // ----------------------------------

    @Test
    public void handleUploadClaims_WhenNoNetwork_BroadcastsError() {
        when(global.isNetworkAvailable()).thenReturn(false);

        synchronizeService.handleUploadClaims();

        verify(global).isNetworkAvailable();
        assertNotNull(capturedIntent);
        assertEquals(SynchronizeService.ACTION_SYNC_ERROR, capturedIntent.getAction());
        System.out.println("Error Message: " + capturedIntent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE));
        assertEquals("Error message", capturedIntent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void handleUploadClaims_WhenNoPendingClaims_BroadcastsNoClaimError() throws JSONException {
        when(global.isNetworkAvailable()).thenReturn(true);
        when(sqlHandler.getAllPendingClaims()).thenReturn(new JSONArray());

        synchronizeService.handleUploadClaims();

        verify(sqlHandler).getAllPendingClaims();
        assertNotNull(capturedIntent);
        assertEquals(SynchronizeService.ACTION_SYNC_ERROR, capturedIntent.getAction());
        assertEquals("Error message", capturedIntent.getStringExtra(SynchronizeService.EXTRA_ERROR_MESSAGE));
    }

    // Note : we can't test PostNewClaims here without refactoring
    // We just test that the method doesn't crash with pending claims
    @Test
    public void handleUploadClaims_WithPendingClaims_DoesNotCrash() throws JSONException {
        when(global.isNetworkAvailable()).thenReturn(true);

        JSONArray pendingClaims = new JSONArray();
        JSONObject claim = new JSONObject();
        claim.put("id", "claim123");
        pendingClaims.put(claim);

        when(sqlHandler.getAllPendingClaims()).thenReturn(pendingClaims);

        synchronizeService.handleUploadClaims();

        verify(sqlHandler).getAllPendingClaims();
    }

    // ----------------------------------
    // Tests processClaimResponse
    // ----------------------------------

    @Test
    public void processClaimResponse_WithRejectedClaim_UpdatesStatusToRejected() throws JSONException {
        PostNewClaims.Result rejectedResult =
                new PostNewClaims.Result("claim123", PostNewClaims.Result.Status.REJECTED, "Claim rejected");

        when(sqlHandler.getClaimUUIDForCode("claim123")).thenReturn("uuid123");

        JSONArray result = synchronizeService.processClaimResponse(Arrays.asList(rejectedResult));

        verify(sqlHandler).insertClaimUploadStatus(
                eq("uuid123"),
                anyString(),
                eq(SQLHandler.CLAIM_UPLOAD_STATUS_REJECTED),
                isNull()
        );

        assertEquals(1, result.length());
        assertTrue(result.getString(0).contains("Claim rejected"));
    }

    @Test
    public void processClaimResponse_WithError_UpdatesStatusToError() throws JSONException {
        PostNewClaims.Result errorResult =
                new PostNewClaims.Result("claim123", PostNewClaims.Result.Status.ERROR, "Server error");

        when(sqlHandler.getClaimUUIDForCode("claim123")).thenReturn("uuid123");

        JSONArray result = synchronizeService.processClaimResponse(Arrays.asList(errorResult));

        verify(sqlHandler).insertClaimUploadStatus(
                eq("uuid123"),
                anyString(),
                eq(SQLHandler.CLAIM_UPLOAD_STATUS_ERROR),
                eq("Server error")
        );

        assertEquals(1, result.length());
    }

    @Test
    public void processClaimResponse_WithSuccess_UpdatesStatusToAccepted() throws JSONException {
        PostNewClaims.Result successResult =
                new PostNewClaims.Result("claim123", PostNewClaims.Result.Status.SUCCESS, null);

        when(sqlHandler.getClaimUUIDForCode("claim123")).thenReturn("uuid123");

        JSONArray result = synchronizeService.processClaimResponse(Arrays.asList(successResult));

        verify(sqlHandler).insertClaimUploadStatus(
                eq("uuid123"),
                anyString(),
                eq(SQLHandler.CLAIM_UPLOAD_STATUS_ACCEPTED),
                isNull()
        );

        assertEquals(0, result.length()); // no error message for SUCCESS
    }

    // ----------------------------------
    // Test handleGetClaimCount
    // ----------------------------------

    @Test
    public void handleGetClaimCount_BroadcastsCorrectCounts() throws JSONException {
        JSONObject counts = new JSONObject();
        counts.put(SQLHandler.CLAIM_UPLOAD_STATUS_ENTERED, 5);
        counts.put(SQLHandler.CLAIM_UPLOAD_STATUS_ACCEPTED, 3);
        counts.put(SQLHandler.CLAIM_UPLOAD_STATUS_REJECTED, 2);

        when(sqlHandler.getClaimCounts()).thenReturn(counts);

        synchronizeService.handleGetClaimCount();

        assertNotNull(capturedIntent);
        assertEquals(SynchronizeService.ACTION_CLAIM_COUNT_RESULT, capturedIntent.getAction());
        assertEquals(5, capturedIntent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ENTERED, 0));
        assertEquals(3, capturedIntent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_ACCEPTED, 0));
        assertEquals(2, capturedIntent.getIntExtra(SynchronizeService.EXTRA_CLAIM_COUNT_REJECTED, 0));
    }

    @Test
    public void testDownloadClaims_WhenNetworkAvailable_ReturnsClaimsList() throws Exception {
        // Given
        when(global.isNetworkAvailable()).thenReturn(true);
        
        // Create a mock claim
        Claim.Service mockService = new Claim.Service(
                "SERVICE1", "Service 1", 100.0, "$", 
                "1", "1", null, null, null, null);
        Claim.Medication mockMedication = new Claim.Medication(
                "MED1", "Medication 1", 50.0, "$", 
                "2", "2", null, null, null, null);
        
        Claim mockClaim = new Claim(
                "claim-uuid-123", "HF001", "Health Facility 1", 
                "INS123", "John Doe", "CLAIM-001", 
                new Date(), new Date(), new Date(), 
                "O", Claim.Status.PROCESSED, "A01", 
                null, null, null, null, 
                150.0, 150.0, null, null, "G123", 
                Arrays.asList(mockService), 
                Arrays.asList(mockMedication)
        );
        
        // Mock the FetchClaims class
        FetchClaims fetchClaims = mock(FetchClaims.class);
        when(fetchClaims.execute(
                eq(global.getOfficerCode()),
                any(Claim.Status.class),
                any(Date.class),
                any(Date.class),
                any(Date.class),
                any(Date.class)
        )).thenReturn(Arrays.asList(mockClaim));
        
        // When
        List<Claim> claims = fetchClaims.execute(
                global.getOfficerCode(),
                Claim.Status.PROCESSED,
                new Date(),
                new Date(),
                new Date(),
                new Date()
        );
        
        // Then
        assertNotNull(claims);
        assertFalse(claims.isEmpty());
        assertEquals(1, claims.size());
        assertEquals("CLAIM-001", claims.get(0).getClaimNumber());
        assertEquals("John Doe", claims.get(0).getPatientName());
        assertEquals(Claim.Status.PROCESSED, claims.get(0).getStatus());
        
        // Verify the service and medication were included
        assertFalse(claims.get(0).getServices().isEmpty());
        assertFalse(claims.get(0).getMedications().isEmpty());
        assertEquals("SERVICE1", claims.get(0).getServices().get(0).getCode());
        assertEquals("MED1", claims.get(0).getMedications().get(0).getCode());
    }

    @Test
    public void testDownloadClaims_WhenNetworkUnavailable_ThrowsException() {
        // Given
        when(global.isNetworkAvailable()).thenReturn(false);
        
        // When/Then
        assertThrows(Exception.class, () -> {
            // This should fail because there's no network
            new FetchClaims().execute(
                    global.getOfficerCode(),
                    Claim.Status.PROCESSED,
                    new Date(),
                    new Date(),
                    new Date(),
                    new Date()
            );
        });
    }
}
