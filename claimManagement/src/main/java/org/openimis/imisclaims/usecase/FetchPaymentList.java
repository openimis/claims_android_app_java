package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.GetPaymentListQuery;
import org.openimis.imisclaims.domain.entity.Medication;
import org.openimis.imisclaims.domain.entity.PaymentList;
import org.openimis.imisclaims.domain.entity.Service;
import org.openimis.imisclaims.network.request.GetPaymentListGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.Collections;
import java.util.Objects;

public class FetchPaymentList {

    @NonNull
    private final GetPaymentListGraphQLRequest request;

    public FetchPaymentList() {
        this(new GetPaymentListGraphQLRequest());
    }

    public FetchPaymentList(
            @NonNull GetPaymentListGraphQLRequest request
    ) {
        this.request = request;
    }

    @NonNull
    @WorkerThread
    public PaymentList execute(@NonNull String claimAdministratorCode) throws Exception {
        GetPaymentListQuery.Node node = request.get(claimAdministratorCode);
        GetPaymentListQuery.HealthFacility healthFacility = Objects.requireNonNull(node.healthFacility());
        GetPaymentListQuery.ServicesPricelist services = healthFacility.servicesPricelist();
        GetPaymentListQuery.ItemsPricelist medications = healthFacility.itemsPricelist();
        return new PaymentList(
                /* healthFacilityCode = */ healthFacility.code(),
                /* services = */ services != null ? Mapper.map(services.details().edges(), this::toService) : Collections.emptyList(),
                /* medications = */ medications != null ? Mapper.map(medications.details().edges(), this::toMedication) : Collections.emptyList()
        );
    }

    @NonNull
    private Service toService(@NonNull GetPaymentListQuery.Edge1 edge) {
        GetPaymentListQuery.Service service = Objects.requireNonNull(edge.node()).service();
        return new Service(
                /* code = */ service.code(),
                /* name = */ service.name(),
                /* price = */ service.price(),
                /* currency = */ BuildConfig.CURRENCY
        );
    }

    @NonNull
    private Medication toMedication(@NonNull GetPaymentListQuery.Edge2 edge) {
        GetPaymentListQuery.Item item = Objects.requireNonNull(edge.node()).item();
        return new Medication(
                /* code = */ item.code(),
                /* name = */ item.name(),
                /* price = */ item.price(),
                /* currency = */ BuildConfig.CURRENCY
        );
    }
}
