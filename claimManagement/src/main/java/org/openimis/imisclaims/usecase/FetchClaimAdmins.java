package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.domain.entity.ClaimAdmin;
import org.openimis.imisclaims.network.dto.IdentifierDto;
import org.openimis.imisclaims.network.request.GetPractitionersRequest;
import org.openimis.imisclaims.network.util.PaginatedResponseUtils;

import java.util.List;
import java.util.Objects;

public class FetchClaimAdmins {

    @NonNull
    private final GetPractitionersRequest request;

    public FetchClaimAdmins() {
        this(new GetPractitionersRequest());
    }

    public FetchClaimAdmins(
            @NonNull GetPractitionersRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<ClaimAdmin> execute() throws Exception {
        return PaginatedResponseUtils.downloadAll(
                page -> request.get(page, /* onlyClaimAdmins = */ true),
                dto -> new ClaimAdmin(
                        /* lastName = */ dto.getNames().get(0).getFamily(),
                        /* otherNames = */ String.join(" ", dto.getNames().get(0).getGiven()),
                        /* claimAdminCode = */ IdentifierDto.getCode(dto.getIdentifiers()),
                        /* healthFacilityCode = */ Objects.requireNonNull(dto.getHealthFacilityCode())
                )
        );
    }
}
