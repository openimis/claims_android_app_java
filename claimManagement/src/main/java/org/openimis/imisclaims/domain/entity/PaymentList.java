package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PaymentList implements Parcelable {

    @NonNull
    private final String healthFacilityCode;
    @NonNull
    private final List<Service> services;
    @NonNull
    private final List<Medication> medications;
    @Nullable
    private final String servicesPricelistUuid;
    @Nullable
    private final String itemsPricelistUuid;

    public PaymentList(
            @NonNull String healthFacilityCode,
            @NonNull List<Service> services,
            @NonNull List<Medication> medications,
            @Nullable String servicesPricelistUuid,
            @Nullable String itemsPricelistUuid
    ) {
        this.healthFacilityCode = healthFacilityCode;
        this.services = services;
        this.medications = medications;
        this.servicesPricelistUuid = servicesPricelistUuid;
        this.itemsPricelistUuid = itemsPricelistUuid;
    }

    protected PaymentList(Parcel in) {
        healthFacilityCode = in.readString();
        services = in.createTypedArrayList(Service.CREATOR);
        medications = in.createTypedArrayList(Medication.CREATOR);
        servicesPricelistUuid = in.readString();
        itemsPricelistUuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(healthFacilityCode);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
        dest.writeString(servicesPricelistUuid);
        dest.writeString(itemsPricelistUuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    @NonNull
    public List<Service> getServices() {
        return services;
    }

    @NonNull
    public List<Medication> getMedications() {
        return medications;
    }

    @Nullable
    public String getServicesPricelistUuid(){ return servicesPricelistUuid; }

    @Nullable
    public String getItemsPricelistUuid(){ return itemsPricelistUuid; }

    public static final Creator<PaymentList> CREATOR = new Creator<>() {
        @Override
        public PaymentList createFromParcel(Parcel in) {
            return new PaymentList(in);
        }

        @Override
        public PaymentList[] newArray(int size) {
            return new PaymentList[size];
        }
    };
}
