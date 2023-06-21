package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.domain.entity.DiagnosesServicesMedications;
import org.openimis.imisclaims.domain.entity.Diagnosis;
import org.openimis.imisclaims.domain.entity.Medication;
import org.openimis.imisclaims.domain.entity.Service;
import org.openimis.imisclaims.network.dto.ActivityDefinitionDto;
import org.openimis.imisclaims.network.dto.DiagnosisDto;
import org.openimis.imisclaims.network.dto.IdentifierDto;
import org.openimis.imisclaims.network.dto.MedicationDto;
import org.openimis.imisclaims.network.request.GetActivityDefinitionsRequest;
import org.openimis.imisclaims.network.request.GetDiagnosesRequest;
import org.openimis.imisclaims.network.request.GetMedicationsRequest;
import org.openimis.imisclaims.network.util.Mapper;
import org.openimis.imisclaims.network.util.PaginatedResponseUtils;
import org.openimis.imisclaims.util.DateUtils;

import java.util.Date;

public class FetchDiagnosesServicesItems {

    @NonNull
    private final GetActivityDefinitionsRequest getActivityDefinitionsRequest;
    @NonNull
    private final GetDiagnosesRequest getDiagnosesRequest;
    @NonNull
    private final GetMedicationsRequest getMedicationsRequest;

    public FetchDiagnosesServicesItems() {
        this(
                new GetActivityDefinitionsRequest(),
                new GetDiagnosesRequest(),
                new GetMedicationsRequest()
        );
    }

    public FetchDiagnosesServicesItems(
            @NonNull GetActivityDefinitionsRequest getActivityDefinitionsRequest,
            @NonNull GetDiagnosesRequest getDiagnosesRequest,
            @NonNull GetMedicationsRequest getMedicationsRequest
    ) {
        this.getActivityDefinitionsRequest = getActivityDefinitionsRequest;
        this.getDiagnosesRequest = getDiagnosesRequest;
        this.getMedicationsRequest = getMedicationsRequest;
    }

    @NonNull
    @WorkerThread
    public DiagnosesServicesMedications execute() throws Exception {
        // previous code was passing sometimes a `last_updated_date` but it was either empty or
        // `new Date(0)`. I'm still returning the last updated date in case it's one day used
        // again.¯\_(ツ)_/¯
        return new DiagnosesServicesMedications(
                /* lastUpdated = */  DateUtils.toDateString(new Date()),
                /* diagnoses = */ Mapper.map(getDiagnosesRequest.get(), this::toDiagnosis),
                /* services = */ PaginatedResponseUtils.downloadAll(
                getActivityDefinitionsRequest::get,
                this::toService
        ),
                /* medications = */ PaginatedResponseUtils.downloadAll(
                getMedicationsRequest::get,
                this::toMedication
        )
        );
    }

    @NonNull
    private Diagnosis toDiagnosis(@NonNull DiagnosisDto dto) {
        return new Diagnosis(
                /* code = */ dto.getCode(),
                /* name = */ dto.getDisplay()
        );
    }

    @NonNull
    private Service toService(@NonNull ActivityDefinitionDto dto) {
        return new Service(
                /* code = */ IdentifierDto.getCode(dto.getIdentifiers()),
                /* name = */ dto.getTitle(),
                /* price = */ dto.getPrice(),
                /* currency = */ dto.getCurrency()
        );
    }
    @NonNull
    private Medication toMedication(@NonNull MedicationDto dto) {
        return new Medication(
                /* code = */ IdentifierDto.getCode(dto.getIdentifiers()),
                /* name = */ dto.getTitle(),
                /* price = */ dto.getPrice(),
                /* currency = */ dto.getCurrency()
        );
    }
}
