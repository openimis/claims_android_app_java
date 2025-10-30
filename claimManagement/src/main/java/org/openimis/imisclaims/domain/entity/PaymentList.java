package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class PaymentList implements Parcelable {

    @NonNull
    private final String healthFacilityCode;
    @NonNull
    private final List<Service> services;
    @NonNull
    private final List<Medication> medications;

    public PaymentList(
            @NonNull String healthFacilityCode,
            @NonNull List<Service> services,
            @NonNull List<Medication> medications
    ) {
        this.healthFacilityCode = healthFacilityCode;
        this.services = services;
        this.medications = medications;
    }

    protected PaymentList(Parcel in) {
        healthFacilityCode = in.readString();
        services = in.createTypedArrayList(Service.CREATOR);
        medications = in.createTypedArrayList(Medication.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(healthFacilityCode);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
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
