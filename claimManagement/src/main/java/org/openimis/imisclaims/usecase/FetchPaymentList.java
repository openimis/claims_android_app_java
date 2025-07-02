package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetPaymentListQuery;
import org.openimis.imisclaims.domain.entity.Medication;
import org.openimis.imisclaims.domain.entity.PaymentList;
import org.openimis.imisclaims.domain.entity.Service;
import org.openimis.imisclaims.domain.entity.SubServiceItem;
import org.openimis.imisclaims.network.request.GetPaymentListGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;
import org.openimis.imisclaims.util.IdUtils;

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
        Mapper<GetPaymentListQuery.ServiceserviceSet, SubServiceItem> subServiceMapper = new Mapper<>(this::toSubService);
        Mapper<GetPaymentListQuery.ServicesLinked, SubServiceItem> subItemMapper = new Mapper<>(this::toSubItem);
        return new PaymentList(
                /* healthFacilityCode = */ healthFacility.code(),
                /* services = */ services != null ? Mapper.map(services.details().edges(), dto -> toService(dto,subServiceMapper, subItemMapper)) : Collections.emptyList(),
                /* medications = */ medications != null ? Mapper.map(medications.details().edges(), this::toMedication) : Collections.emptyList()
        );
    }

    @NonNull
    private Service toService(
            @NonNull GetPaymentListQuery.Edge1 edge,
            @NonNull Mapper<GetPaymentListQuery.ServiceserviceSet, SubServiceItem> subServiceMapper,
            @NonNull Mapper<GetPaymentListQuery.ServicesLinked, SubServiceItem> subItemMapper
    ) {
        GetPaymentListQuery.Service service = Objects.requireNonNull(edge.node()).service();
        return new Service(
                /* id = */ String.valueOf(IdUtils.getIdFromGraphQLString(service.id())),
                /* code = */ service.code(),
                /* name = */ service.name(),
                /* price = */ service.price(),
                /* currency = */ "$",
                /* packageType = */ service.packagetype(),
                /* manualPrice = */ service.manualPrice() ? 1 : 0,
                /* subServices = */ subServiceMapper.map(service.serviceserviceSet()),
                /* subItems = */ subItemMapper.map(service.servicesLinked())
        );
    }

    @NonNull
    private Medication toMedication(@NonNull GetPaymentListQuery.Edge2 edge) {
        GetPaymentListQuery.Item1 item = Objects.requireNonNull(edge.node()).item();
        return new Medication(
                /* id = */ String.valueOf(IdUtils.getIdFromGraphQLString(item.id())),
                /* code = */ item.code(),
                /* name = */ item.name(),
                /* price = */ item.price(),
                /* currency = */ "$"
        );
    }

    private SubServiceItem toSubService(@NonNull GetPaymentListQuery.ServiceserviceSet service) {
        return new SubServiceItem(
                /* id = */ String.valueOf(IdUtils.getIdFromGraphQLString(service.service().id())),
                /* quantity = */ service.qtyProvided(),
                /* price = */ service.priceAsked()
        );
    }

    private SubServiceItem toSubItem(@NonNull GetPaymentListQuery.ServicesLinked item) {
        return new SubServiceItem(
                /* id = */ String.valueOf(IdUtils.getIdFromGraphQLString(item.item().id())),
                /* quantity = */ item.qtyProvided(),
                /* price = */ item.priceAsked()
        );
    }
}
