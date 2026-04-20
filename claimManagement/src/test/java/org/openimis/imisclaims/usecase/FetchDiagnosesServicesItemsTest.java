package org.openimis.imisclaims.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openimis.imisclaims.domain.entity.DiagnosesServicesMedications;
import org.openimis.imisclaims.network.dto.ActivityDefinitionDto;
import org.openimis.imisclaims.network.dto.DiagnosisDto;
import org.openimis.imisclaims.network.dto.IdentifierDto;
import org.openimis.imisclaims.network.dto.MedicationDto;
import org.openimis.imisclaims.network.request.GetActivityDefinitionsRequest;
import org.openimis.imisclaims.network.request.GetDiagnosesRequest;
import org.openimis.imisclaims.network.request.GetMedicationsRequest;
import org.openimis.imisclaims.network.response.PaginatedResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FetchDiagnosesServicesItemsTest {

    @Mock
    private GetActivityDefinitionsRequest getActivityDefinitionsRequest;
    @Mock
    private GetDiagnosesRequest getDiagnosesRequest;
    @Mock
    private GetMedicationsRequest getMedicationsRequest;

    private FetchDiagnosesServicesItems useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new FetchDiagnosesServicesItems(
                getActivityDefinitionsRequest,
                getDiagnosesRequest,
                getMedicationsRequest
        );
    }

    @Test
    public void execute_returnsDiagnosesServicesAndPartialMedications_whenMedicationPageFails() throws Exception {
        when(getDiagnosesRequest.get()).thenReturn(Collections.singletonList(new DiagnosisDto("D1", "Diagnosis 1")));
        when(getActivityDefinitionsRequest.get(anyInt())).thenReturn(new PaginatedResponse<>(
                Collections.singletonList(new ActivityDefinitionDto(
                        "s1",
                        Collections.singletonList(new IdentifierDto("Code", "SVC1")),
                        "Service1",
                        "Service 1",
                        10.0,
                        "XAF",
                        "active",
                        new Date()
                )),
                false
        ));

        when(getMedicationsRequest.get(0)).thenReturn(new PaginatedResponse<>(Collections.singletonList(
                new MedicationDto(
                        "m1",
                        Collections.singletonList(new IdentifierDto("Code", "MED1")),
                        "Medication 1",
                        20.0,
                        "XAF",
                        "active",
                        1.0
                )
        ), true));
        when(getMedicationsRequest.get(1)).thenThrow(new RuntimeException("server error"));
        when(getMedicationsRequest.get(2)).thenReturn(new PaginatedResponse<>(Collections.singletonList(
                new MedicationDto(
                        "m2",
                        Collections.singletonList(new IdentifierDto("Code", "MED2")),
                        "Medication 2",
                        30.0,
                        "XAF",
                        "active",
                        1.0
                )
        ), false));

        DiagnosesServicesMedications result = useCase.execute();

        assertNotNull(result);
        assertEquals(1, result.getDiagnoses().size());
        assertEquals(1, result.getServices().size());
        assertEquals(2, result.getMedications().size());
        assertEquals(Collections.singletonList(2), useCase.getSkippedMedicationPages());
    }

    @Test
    public void execute_exposesSkippedMedicationPages_afterPartialMedicationSync() throws Exception {
        when(getDiagnosesRequest.get()).thenReturn(Collections.singletonList(new DiagnosisDto("D1", "Diagnosis 1")));
        when(getActivityDefinitionsRequest.get(anyInt())).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));
        when(getMedicationsRequest.get(0)).thenThrow(new RuntimeException("page 1 failed"));
        when(getMedicationsRequest.get(1)).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));

        useCase.execute();

        assertEquals(Collections.singletonList(1), useCase.getSkippedMedicationPages());
    }

    @Test
    public void execute_throws_whenDiagnosesRequestFails() throws Exception {
        when(getDiagnosesRequest.get()).thenThrow(new RuntimeException("diagnoses down"));
        when(getMedicationsRequest.get(anyInt())).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));

        assertThrows(RuntimeException.class, () -> useCase.execute());
    }

    @Test
    public void execute_throws_whenServicesRequestFails() throws Exception {
        when(getDiagnosesRequest.get()).thenReturn(Collections.singletonList(new DiagnosisDto("D1", "Diagnosis 1")));
        when(getActivityDefinitionsRequest.get(0)).thenThrow(new RuntimeException("services down"));
        when(getMedicationsRequest.get(anyInt())).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));

        assertThrows(RuntimeException.class, () -> useCase.execute());
    }

    @Test
    public void getSkippedMedicationPages_emptyBeforeExecute_thenUpdatedAfterExecute() throws Exception {
        assertTrue(useCase.getSkippedMedicationPages().isEmpty());

        when(getDiagnosesRequest.get()).thenReturn(Collections.singletonList(new DiagnosisDto("D1", "Diagnosis 1")));
        when(getActivityDefinitionsRequest.get(anyInt())).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));
        when(getMedicationsRequest.get(0)).thenThrow(new RuntimeException("page 1 failed"));
        when(getMedicationsRequest.get(1)).thenReturn(new PaginatedResponse<>(Collections.emptyList(), false));

        useCase.execute();

        assertEquals(Arrays.asList(1), useCase.getSkippedMedicationPages());
    }
}
