package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

public class Claim implements Parcelable {

    @NonNull
    private final String uuid;

    @NonNull
    private final String healthFacilityCode;

    @NonNull
    private final String healthFacilityName;

    @Nullable
    private final String insuranceNumber;

    @NonNull
    private final String patientName;

    @NonNull
    private final String claimNumber;

    @Nullable
    private final Date dateClaimed;

    @Nullable
    private final Date visitDateFrom;

    @Nullable
    private final Date visitDateTo;

    @Nullable
    private final String visitType;

    @Nullable
    private final Status status;

    @NonNull
    private final String mainDg;

    @Nullable
    private final String secDg1;

    @Nullable
    private final String secDg2;

    @Nullable
    private final String secDg3;

    @Nullable
    private final String secDg4;

    @Nullable
    private final Double claimed;

    @Nullable
    private final Double approved;

    @Nullable
    private final Double adjusted;

    @Nullable
    private final String explanation;

    @Nullable
    private final String adjustment;

    @Nullable
    private final String guaranteeNumber;

    @NonNull
    private final List<Service> services;

    @NonNull
    private final List<Medication> medications;


    public Claim(
            @NonNull String uuid,
            @NonNull String healthFacilityCode,
            @NonNull String healthFacilityName,
            @Nullable String insuranceNumber,
            @NonNull String patientName,
            @NonNull String claimNumber,
            @Nullable Date dateClaimed,
            @Nullable Date visitDateFrom,
            @Nullable Date visitDateTo,
            @Nullable String visitType,
            @Nullable Status status,
            @NonNull String mainDg,
            @Nullable String secDg1,
            @Nullable String secDg2,
            @Nullable String secDg3,
            @Nullable String secDg4,
            @Nullable Double claimed,
            @Nullable Double approved,
            @Nullable Double adjusted,
            @Nullable String explanation,
            @Nullable String adjustment,
            @Nullable String guaranteeNumber,
            @NonNull List<Service> services,
            @NonNull List<Medication> medications
    ) {
        this.uuid = uuid;
        this.healthFacilityCode = healthFacilityCode;
        this.healthFacilityName = healthFacilityName;
        this.insuranceNumber = insuranceNumber;
        this.patientName = patientName;
        this.claimNumber = claimNumber;
        this.dateClaimed = dateClaimed;
        this.visitDateFrom = visitDateFrom;
        this.visitDateTo = visitDateTo;
        this.visitType = visitType;
        this.status = status;
        this.mainDg = mainDg;
        this.secDg1 = secDg1;
        this.secDg2 = secDg2;
        this.secDg3 = secDg3;
        this.secDg4 = secDg4;
        this.claimed = claimed;
        this.approved = approved;
        this.adjusted = adjusted;
        this.explanation = explanation;
        this.adjustment = adjustment;
        this.guaranteeNumber = guaranteeNumber;
        this.services = services;
        this.medications = medications;
    }

    protected Claim(Parcel in) {
        uuid = in.readString();
        healthFacilityCode = in.readString();
        healthFacilityName = in.readString();
        insuranceNumber = in.readString();
        patientName = in.readString();
        claimNumber = in.readString();
        visitType = in.readString();
        mainDg = in.readString();
        secDg1 = in.readString();
        secDg2 = in.readString();
        secDg3 = in.readString();
        secDg4 = in.readString();
        if (in.readByte() == 0) {
            claimed = null;
        } else {
            claimed = in.readDouble();
        }
        if (in.readByte() == 0) {
            approved = null;
        } else {
            approved = in.readDouble();
        }
        if (in.readByte() == 0) {
            adjusted = null;
        } else {
            adjusted = in.readDouble();
        }
        explanation = in.readString();
        adjustment = in.readString();
        guaranteeNumber = in.readString();
        services = in.createTypedArrayList(Service.CREATOR);
        medications = in.createTypedArrayList(Medication.CREATOR);
        if (in.readByte() == 0) {
            dateClaimed = null;
        } else {
            dateClaimed = new Date(in.readLong());
        }
        if (in.readByte() == 0) {
            visitDateFrom = null;
        } else {
            visitDateFrom = new Date(in.readLong());
        }

        if (in.readByte() == 0) {
            visitDateTo = null;
        } else {
            visitDateTo = new Date(in.readLong());
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = Claim.Status.valueOf(in.readString());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(healthFacilityCode);
        dest.writeString(healthFacilityName);
        dest.writeString(insuranceNumber);
        dest.writeString(patientName);
        dest.writeString(claimNumber);
        dest.writeString(visitType);
        dest.writeString(mainDg);
        dest.writeString(secDg1);
        dest.writeString(secDg2);
        dest.writeString(secDg3);
        dest.writeString(secDg4);
        if (claimed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(claimed);
        }
        if (approved == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(approved);
        }
        if (adjusted == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(adjusted);
        }
        dest.writeString(explanation);
        dest.writeString(adjustment);
        dest.writeString(guaranteeNumber);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
        if (dateClaimed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(dateClaimed.getTime());
        }
        if (visitDateFrom == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(visitDateFrom.getTime());
        }

        if (visitDateTo == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(visitDateTo.getTime());
        }
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(status.name());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    @NonNull
    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    @NonNull
    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    @Nullable
    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    @NonNull
    public String getPatientName() {
        return patientName;
    }

    @NonNull
    public String getClaimNumber() {
        return claimNumber;
    }

    @Nullable
    public Date getDateClaimed() {
        return dateClaimed;
    }

    @Nullable
    public Date getVisitDateFrom() {
        return visitDateFrom;
    }

    @Nullable
    public Date getVisitDateTo() {
        return visitDateTo;
    }

    @Nullable
    public String getVisitType() {
        return visitType;
    }

    @Nullable
    public Status getStatus() {
        return status;
    }

    @NonNull
    public String getMainDg() {
        return mainDg;
    }

    @Nullable
    public String getSecDg1() {
        return secDg1;
    }

    @Nullable
    public String getSecDg2() {
        return secDg2;
    }

    @Nullable
    public String getSecDg3() {
        return secDg3;
    }

    @Nullable
    public String getSecDg4() {
        return secDg4;
    }

    @Nullable
    public Double getClaimed() {
        return claimed;
    }

    @Nullable
    public Double getApproved() {
        return approved;
    }

    @Nullable
    public Double getAdjusted() {
        return adjusted;
    }

    @Nullable
    public String getExplanation() {
        return explanation;
    }

    @Nullable
    public String getAdjustment() {
        return adjustment;
    }

    @Nullable
    public String getGuaranteeNumber() {
        return guaranteeNumber;
    }

    @NonNull
    public List<Service> getServices() {
        return services;
    }

    @NonNull
    public List<Medication> getMedications() {
        return medications;
    }

    public static final Creator<Claim> CREATOR = new Creator<Claim>() {
        @Override
        public Claim createFromParcel(Parcel in) {
            return new Claim(in);
        }

        @Override
        public Claim[] newArray(int size) {
            return new Claim[size];
        }
    };

    public static class Service extends org.openimis.imisclaims.domain.entity.Service {

        @NonNull
        private final String quantityProvided;
        @Nullable
        private final String quantityApproved;
        @Nullable
        private final String priceAdjusted;
        @Nullable
        private final String priceValuated;
        @Nullable
        private final String explanation;
        @Nullable
        private final String justification;

        public Service(
                @NonNull String code,
                @NonNull String name,
                double price,
                @NonNull String currency,
                @NonNull String quantityProvided,
                @Nullable String quantityApproved,
                @Nullable String priceAdjusted,
                @Nullable String priceValuated,
                @Nullable String explanation,
                @Nullable String justification
        ) {
            super(code, name, price, currency);
            this.quantityProvided = quantityProvided;
            this.quantityApproved = quantityApproved;
            this.priceAdjusted = priceAdjusted;
            this.priceValuated = priceValuated;
            this.explanation = explanation;
            this.justification = justification;
        }

        protected Service(Parcel in) {
            this(
                    /* code = */ in.readString(),
                    /* name = */ in.readString(),
                    /* price = */ in.readDouble(),
                    /* currency = */ in.readString(),
                    /* quantityProvided = */ in.readString(),
                    /* quantityApproved = */ in.readString(),
                    /* priceAdjusted = */ in.readString(),
                    /* priceValuated = */ in.readString(),
                    /* explanation = */ in.readString(),
                    /* justification = */ in.readString()
            );
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(quantityProvided);
            dest.writeString(quantityApproved);
            dest.writeString(priceAdjusted);
            dest.writeString(priceValuated);
            dest.writeString(explanation);
            dest.writeString(justification);
        }

        @NonNull
        public String getQuantityProvided() {
            return quantityProvided;
        }

        @Nullable
        public String getQuantityApproved() {
            return quantityApproved;
        }

        @Nullable
        public String getPriceAdjusted() {
            return priceAdjusted;
        }

        @Nullable
        public String getPriceValuated() {
            return priceValuated;
        }

        @Nullable
        public String getExplanation() {
            return explanation;
        }

        @Nullable
        public String getJustification() {
            return justification;
        }

        public static final Creator<Service> CREATOR = new Creator<>() {
            @Override
            public Service createFromParcel(Parcel in) {
                return new Service(in);
            }

            @Override
            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
    }

    public static class Medication extends org.openimis.imisclaims.domain.entity.Medication {

        @NonNull
        private final String quantityProvided;
        @Nullable
        private final String quantityApproved;
        @Nullable
        private final String priceAdjusted;
        @Nullable
        private final String priceValuated;
        @Nullable
        private final String explanation;
        @Nullable
        private final String justification;

        public Medication(
                @NonNull String code,
                @NonNull String name,
                double price,
                @NonNull String currency,
                @NonNull String quantityProvided,
                @Nullable String quantityApproved,
                @Nullable String priceAdjusted,
                @Nullable String priceValuated,
                @Nullable String explanation,
                @Nullable String justification
        ) {
            super(code, name, price, currency);
            this.quantityProvided = quantityProvided;
            this.quantityApproved = quantityApproved;
            this.priceAdjusted = priceAdjusted;
            this.priceValuated = priceValuated;
            this.explanation = explanation;
            this.justification = justification;
        }

        protected Medication(Parcel in) {
            this(
                    /* code = */ in.readString(),
                    /* name = */ in.readString(),
                    /* price = */ in.readDouble(),
                    /* currency = */ in.readString(),
                    /* quantityProvided = */ in.readString(),
                    /* quantityApproved = */ in.readString(),
                    /* priceAdjusted = */ in.readString(),
                    /* priceValuated = */ in.readString(),
                    /* explanation = */ in.readString(),
                    /* justification = */ in.readString()
            );
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(quantityProvided);
            dest.writeString(quantityApproved);
            dest.writeString(priceAdjusted);
            dest.writeString(priceValuated);
            dest.writeString(explanation);
            dest.writeString(justification);
        }

        @NonNull
        public String getQuantityProvided() {
            return quantityProvided;
        }

        @Nullable
        public String getQuantityApproved() {
            return quantityApproved;
        }

        @Nullable
        public String getPriceAdjusted() {
            return priceAdjusted;
        }

        @Nullable
        public String getPriceValuated() {
            return priceValuated;
        }

        @Nullable
        public String getExplanation() {
            return explanation;
        }

        @Nullable
        public String getJustification() {
            return justification;
        }

        public static final Creator<Medication> CREATOR = new Creator<>() {
            @Override
            public Medication createFromParcel(Parcel in) {
                return new Medication(in);
            }

            @Override
            public Medication[] newArray(int size) {
                return new Medication[size];
            }
        };
    }

    public enum Status {
        REJECTED, // 1
        ENTERED, // 2
        CHECKED, // 4
        PROCESSED, // 8
        VALUATED, // 16
    }
}
