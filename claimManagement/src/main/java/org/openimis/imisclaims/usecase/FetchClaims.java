package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetClaimsQuery;
import org.openimis.imisclaims.domain.entity.Claim;
import org.openimis.imisclaims.network.request.GetClaimsGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FetchClaims {

    @NonNull
    private final GetClaimsGraphQLRequest request;

    public FetchClaims() {
        this(new GetClaimsGraphQLRequest());
    }

    public FetchClaims(
            @NonNull GetClaimsGraphQLRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<Claim> execute(
            @Nullable String claimAdministratorCode,
            @Nullable Claim.Status status,
            @Nullable Date visitDateFrom,
            @Nullable Date visitDateTo,
            @Nullable Date processedDateFrom,
            @Nullable Date processedDateTo
    ) throws Exception {
        Mapper<GetClaimsQuery.Service, Claim.Service> serviceMapper = new Mapper<>(this::toService);
        Mapper<GetClaimsQuery.Item, Claim.Medication> medicationMapper = new Mapper<>(this::toMedication);
        return Mapper.map(request.get(
                claimAdministratorCode,
                statusAsInt(status),
                visitDateFrom,
                visitDateTo,
                processedDateFrom,
                processedDateTo
        ).edges(), dto -> toClaim(dto, serviceMapper, medicationMapper));
    }

    @Nullable
    private Integer statusAsInt(@Nullable Claim.Status status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case REJECTED:
                return 1;
            case ENTERED:
                return 2;
            case CHECKED:
                return 4;
            case PROCESSED:
                return 8;
            case VALUATED:
                return 16;
        }

        return null;
    }

    @Nullable
    private Claim.Status intAsStatus(@Nullable Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 1:
                return Claim.Status.REJECTED;
            case 2:
                return Claim.Status.ENTERED;
            case 4:
                return Claim.Status.CHECKED;
            case 8:
                return Claim.Status.PROCESSED;
            case 16:
                return Claim.Status.VALUATED;
            default:
                return null;
        }
    }

    private Claim toClaim(
            @NonNull GetClaimsQuery.Edge dto,
            @NonNull Mapper<GetClaimsQuery.Service, Claim.Service> serviceMapper,
            @NonNull Mapper<GetClaimsQuery.Item, Claim.Medication> medicationMapper
    ) {
        GetClaimsQuery.Node node = Objects.requireNonNull(dto.node());
        return new Claim(
                /* uuid = */ node.uuid(),
                /* healthFacilityCode = */ node.healthFacility().code(),
                /* healthFacilityName = */ node.healthFacility().name(),
                /* insuranceNumber = */ node.insuree().chfId(),
                /* patientName = */ node.insuree().lastName() + " " + node.insuree().otherNames(),
                /* claimNumber = */ node.code(),
                /* dateClaimed = */ node.dateClaimed(),
                /* visitDateFrom = */ node.dateFrom(),
                /* visitDateTo = */ node.dateTo(),
                /* visitType = */ mapVisitType(node.visitType()),
                /* status = */ intAsStatus(node.status()),
                /* mainDg = */ node.icd().name(),
                /* secDg1 = */ node.icd1() != null ? node.icd1().name() : null,
                /* secDg2 = */ node.icd2() != null ? node.icd2().name() : null,
                /* secDg3 = */ node.icd3() != null ? node.icd3().name() : null,
                /* secDg4 = */ node.icd4() != null ? node.icd4().name() : null,
                /* claimed = */ node.claimed(),
                /* approved = */ node.approved(),
                /* explanation = */ node.explanation(),
                /* adjustment = */ node.adjustment(),
                /* guaranteeNumber = */ node.guaranteeId(),
                /* services = */ serviceMapper.map(node.services()),
                /* medications = */ medicationMapper.map(node.items())
        );
    }

    @Nullable
    private String mapVisitType(@Nullable String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "E":
                return "Emergency";
            case "R":
                return "Referral";
            case "O":
                return "Other";
            default:
                return type;
        }
    }

    private Claim.Service toService(@NonNull GetClaimsQuery.Service service) {
        return new Claim.Service(
                /* code = */ service.service().code(),
                /* name = */ service.service().name(),
                /* price = */ service.service().price(),
                /* currency = */ "$",
                /* quantityProvided = */ service.qtyProvided().toString(),
                /* quantityApproved = */ service.qtyApproved() != null ? service.qtyApproved().toString() : null,
                /* priceAdjusted = */ service.priceAdjusted() != null ? service.priceAdjusted().toString() : null,
                /* priceValuated = */ service.priceValuated() != null ? service.priceValuated().toString() : null,
                /* explanation = */ service.explanation(),
                /* justification = */ service.justification()
        );
    }

    private Claim.Medication toMedication(@NonNull GetClaimsQuery.Item item) {
        return new Claim.Medication(
                /* code = */ item.item().code(),
                /* name = */ item.item().name(),
                /* price = */ item.item().price(),
                /* currency = */ "$",
                /* quantityProvided = */ item.qtyProvided().toString(),
                /* quantityApproved = */ item.qtyApproved() != null ? item.qtyApproved().toString() : null,
                /* priceAdjusted = */ item.priceAdjusted() != null ? item.priceAdjusted().toString() : null,
                /* priceValuated = */ item.priceValuated() != null ? item.priceValuated().toString() : null,
                /* explanation = */ item.explanation(),
                /* justification = */ item.justification()
        );
    }
}
