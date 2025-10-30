package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class DiagnosesServicesMedications implements Parcelable {

    @NonNull
    private final String lastUpdated;
    @NonNull
    private final List<Diagnosis> diagnoses;
    @NonNull
    private final List<Service> services;
    @NonNull
    private final List<Medication> medications;

    public DiagnosesServicesMedications(
            @NonNull String lastUpdated,
            @NonNull List<Diagnosis> diagnoses,
            @NonNull List<Service> services,
            @NonNull List<Medication> medications
    ) {
        this.lastUpdated = lastUpdated;
        this.diagnoses = diagnoses;
        this.services = services;
        this.medications = medications;
    }

    protected DiagnosesServicesMedications(Parcel in) {
        lastUpdated = in.readString();
        diagnoses = in.createTypedArrayList(Diagnosis.CREATOR);
        services = in.createTypedArrayList(Service.CREATOR);
        medications = in.createTypedArrayList(Medication.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lastUpdated);
        dest.writeTypedList(diagnoses);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getLastUpdated() {
        return lastUpdated;
    }

    @NonNull
    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    @NonNull
    public List<Service> getServices() {
        return services;
    }

    @NonNull
    public List<Medication> getMedications() {
        return medications;
    }

    public static final Creator<DiagnosesServicesMedications> CREATOR = new Creator<>() {
        @Override
        public DiagnosesServicesMedications createFromParcel(Parcel in) {
            return new DiagnosesServicesMedications(in);
        }

        @Override
        public DiagnosesServicesMedications[] newArray(int size) {
            return new DiagnosesServicesMedications[size];
        }
    };
}
