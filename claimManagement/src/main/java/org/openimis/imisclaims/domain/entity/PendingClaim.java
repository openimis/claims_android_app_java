package org.openimis.imisclaims.domain.entity;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imisclaims.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PendingClaim implements Parcelable {
    @NonNull
    public static List<PendingClaim> fromJson(@NonNull JSONArray array) throws Exception {
        List<PendingClaim> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject claim = array.getJSONObject(i);
            JSONObject details = claim.getJSONObject("details");
            list.add(new PendingClaim(
                    /* claimDate = */ DateUtils.dateFromString(details.getString("ClaimDate")),
                    /* healthFacilityCode = */ details.getString("HFCode"),
                    /* claimAdminCode = */ details.getString("ClaimAdmin"),
                    /* claimCode = */ details.getString("ClaimCode"),
                    /* guaranteeNumber = */ details.getString("GuaranteeNumber"),
                    /* chfId = */ details.getString("CHFID"),
                    /* startDate = */ DateUtils.dateFromString(details.getString("StartDate")),
                    /* endDate = */ DateUtils.dateFromString(details.getString("EndDate")),
                    /* icdCode = */ details.getString("ICDCode"),
                    /* comment = */ details.getString("Comment"),
                    /* total = */ details.getString("Total"),
                    /* icdCode1 = */ details.getString("ICDCode1"),
                    /* icdCode2 = */ details.getString("ICDCode2"),
                    /* icdCode3 = */ details.getString("ICDCode3"),
                    /* icdCode4 = */ details.getString("ICDCode4"),
                    /* visitType = */ details.getString("VisitType"),
                    /* services = */ Service.fromJson(claim.getJSONArray("services")),
                    /* medications = */ Medication.fromJson(claim.getJSONArray("items"))
            ));
        }
        return list;
    }

    @NonNull
    private final Date claimDate;
    @NonNull
    private final String healthFacilityCode;
    @Nullable
    private final String claimAdminCode;
    @NonNull
    private final String claimCode;
    @Nullable
    private final String guaranteeNumber;
    @NonNull
    private final String chfId;
    @NonNull
    private final Date startDate;
    @NonNull
    private final Date endDate;
    @NonNull
    private final String icdCode;
    @Nullable
    private final String comment;
    @Nullable
    private final String total;
    @Nullable
    private final String icdCode1;
    @Nullable
    private final String icdCode2;
    @Nullable
    private final String icdCode3;
    @Nullable
    private final String icdCode4;
    @NonNull
    private final String visitType;
    @NonNull
    private final List<Service> services;
    @NonNull
    private final List<Medication> medications;

    public PendingClaim(
            @NonNull Date claimDate,
            @NonNull String healthFacilityCode,
            @Nullable String claimAdminCode,
            @NonNull String claimCode,
            @Nullable String guaranteeNumber,
            @NonNull String chfId,
            @NonNull Date startDate,
            @NonNull Date endDate,
            @NonNull String icdCode,
            @Nullable String comment,
            @Nullable String total,
            @Nullable String icdCode1,
            @Nullable String icdCode2,
            @Nullable String icdCode3,
            @Nullable String icdCode4,
            @NonNull String visitType,
            @NonNull List<Service> services,
            @NonNull List<Medication> medications
    ) {
        this.claimDate = claimDate;
        this.healthFacilityCode = healthFacilityCode;
        this.claimAdminCode = claimAdminCode;
        this.claimCode = claimCode;
        this.guaranteeNumber = guaranteeNumber;
        this.chfId = chfId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.icdCode = icdCode;
        this.comment = comment;
        this.total = total;
        this.icdCode1 = icdCode1;
        this.icdCode2 = icdCode2;
        this.icdCode3 = icdCode3;
        this.icdCode4 = icdCode4;
        this.visitType = visitType;
        this.services = services;
        this.medications = medications;
    }

    protected PendingClaim(Parcel in) {
        claimDate = new Date(in.readLong());
        healthFacilityCode = in.readString();
        claimAdminCode = in.readString();
        claimCode = in.readString();
        guaranteeNumber = in.readString();
        chfId = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        icdCode = in.readString();
        comment = in.readString();
        total = in.readString();
        icdCode1 = in.readString();
        icdCode2 = in.readString();
        icdCode3 = in.readString();
        icdCode4 = in.readString();
        visitType = in.readString();
        services = in.createTypedArrayList(Service.CREATOR);
        medications = in.createTypedArrayList(Medication.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(claimDate.getTime());
        dest.writeString(healthFacilityCode);
        dest.writeString(claimAdminCode);
        dest.writeString(claimCode);
        dest.writeString(guaranteeNumber);
        dest.writeString(chfId);
        dest.writeLong(startDate.getTime());
        dest.writeLong(endDate.getTime());
        dest.writeString(icdCode);
        dest.writeString(comment);
        dest.writeString(total);
        dest.writeString(icdCode1);
        dest.writeString(icdCode2);
        dest.writeString(icdCode3);
        dest.writeString(icdCode4);
        dest.writeString(visitType);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public Date getClaimDate() {
        return claimDate;
    }

    @NonNull
    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    @Nullable
    public String getClaimAdminCode() {
        return claimAdminCode;
    }

    @NonNull
    public String getClaimCode() {
        return claimCode;
    }

    @Nullable
    public String getGuaranteeNumber() {
        return guaranteeNumber;
    }

    @NonNull
    public String getChfId() {
        return chfId;
    }

    @NonNull
    public Date getStartDate() {
        return startDate;
    }

    @NonNull
    public Date getEndDate() {
        return endDate;
    }

    @NonNull
    public String getIcdCode() {
        return icdCode;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @Nullable
    public String getTotal() {
        return total;
    }

    @Nullable
    public String getIcdCode1() {
        return icdCode1;
    }

    @Nullable
    public String getIcdCode2() {
        return icdCode2;
    }

    @Nullable
    public String getIcdCode3() {
        return icdCode3;
    }

    @Nullable
    public String getIcdCode4() {
        return icdCode4;
    }

    @NonNull
    public String getVisitType() {
        return visitType;
    }

    @NonNull
    public List<Service> getServices() {
        return services;
    }

    @NonNull
    public List<Medication> getMedications() {
        return medications;
    }

    public static final Creator<PendingClaim> CREATOR = new Creator<>() {
        @Override
        public PendingClaim createFromParcel(Parcel in) {
            return new PendingClaim(in);
        }

        @Override
        public PendingClaim[] newArray(int size) {
            return new PendingClaim[size];
        }
    };

    public static class Service implements Parcelable {
        @NonNull
        public static List<Service> fromJson(@NonNull JSONArray array) throws Exception {
            List<Service> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject service = array.getJSONObject(i);
                list.add(new Service(
                        /* code = */ service.getString("ServiceCode"),
                        /* price = */ Double.parseDouble(service.getString("ServicePrice")),
                        /* quantity = */ Integer.parseInt(service.getString("ServiceQuantity"))
                ));
            }
            return list;
        }

        @NonNull
        private final String code;
        private final double price;
        private final double quantity;

        public Service(
                @NonNull String code,
                double price,
                double quantity
        ) {
            this.code = code;
            this.price = price;
            this.quantity = quantity;
        }

        protected Service(Parcel in) {
            code = in.readString();
            price = in.readDouble();
            quantity = in.readDouble();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
            dest.writeDouble(price);
            dest.writeDouble(quantity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getCode() {
            return code;
        }

        public double getPrice() {
            return price;
        }

        public double getQuantity() {
            return quantity;
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

    public static class Medication implements Parcelable {
        @NonNull
        public static List<Medication> fromJson(@NonNull JSONArray array) throws Exception {
            List<Medication> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject medication = array.getJSONObject(i);
                list.add(new Medication(
                        /* code = */ medication.getString("ItemCode"),
                        /* price = */ Double.parseDouble(medication.getString("ItemPrice")),
                        /* quantity = */ Double.parseDouble(medication.getString("ItemQuantity"))
                ));
            }
            return list;
        }

        @NonNull
        private final String code;
        private final double price;
        private final double quantity;

        public Medication(
                @NonNull String code,
                double price,
                double quantity
        ) {
            this.code = code;
            this.price = price;
            this.quantity = quantity;
        }

        protected Medication(Parcel in) {
            code = in.readString();
            price = in.readDouble();
            quantity = in.readDouble();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
            dest.writeDouble(price);
            dest.writeDouble(quantity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getCode() {
            return code;
        }

        public double getPrice() {
            return price;
        }

        public double getQuantity() {
            return quantity;
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
}
