package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;
import org.openimis.imisclaims.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PendingClaimGQL {
    @NonNull
    public static List<PendingClaimGQL> fromJson(@NonNull JSONArray array) throws Exception{
        List<PendingClaimGQL> claims = new ArrayList<>();
        for (int i=0; i<array.length();i++){
            JSONObject claim = array.getJSONObject(i);
            JSONArray arrayItems = claim.getJSONArray("items");
            JSONArray arrayServices = claim.getJSONArray("services");
            JSONObject details = claim.getJSONObject("details");
            Log.e("details", details.toString());
            claims.add( new PendingClaimGQL(
                    /* hfCode = */ details.getString("HFCode"),
                    /* claimAdmin */ details.getString("ClaimAdmin"),
                    /* insureeNumber = */ Objects.requireNonNull(details.getString("CHFID")),
                    /* claimNumber = */ Objects.requireNonNull(details.getString("ClaimCode")),
                    /* dateClaimed = */ DateUtils.dateFromString(details.getString("ClaimDate")),
                    /* visitDatefrom = */ DateUtils.dateFromString(details.getString("StartDate")),
                    /* visitDateTo = */ DateUtils.dateFromString(details.getString("EndDate")),
                    /* visitType = */ details.getString("VisitType"),
                    /* mainDg = */ details.getString("ICDCode"),
                    /* icdCode1 = */ details.getString("ICDCode1"),
                    /* icdCode2 = */ details.getString("ICDCode2"),
                    /* icCode3 = */ details.getString("ICDCode3"),
                    /* icdCode4 = */ details.getString("ICDCode4"),
                    /* GuaranteeNumber = */ details.getString("GuaranteeNumber"),
                    /* ReferalHF */ details.getString("ReferalHF"),
                    /* PatientCondition */ details.getString("PatientCondition"),
                    /* PreAuthorization */ details.getString("PreAuthorization"),
                    /* services = */ Service.fromJson(arrayServices),
                    /* items = */ Medication.fromJson(arrayItems)
            ));
        }
        return claims;
    }

    @NonNull
    private final Date claimDate;
    @NonNull
    private final String healthFacilityCode;
    @NonNull
    private final String claimCode;
    @NonNull
    private final String claimAdmin;
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
    private final String icdCode1;
    @Nullable
    private final String icdCode2;
    @Nullable
    private final String icdCode3;
    @Nullable
    private final String icdCode4;
    @NonNull
    private final String visitType;
    @Nullable
    private final String referalHF;
    @Nullable
    private  final String patientCondition;
    @Nullable
    private  final String preAuthorization;
    @NonNull
    private final List<PendingClaimGQL.Service> services;
    @NonNull
    private final List<PendingClaimGQL.Medication> medications;

    public PendingClaimGQL(
            @NonNull String healthFacilityCode,
            @NonNull String claimAdmin,
            @NonNull String chfId,
            @NonNull String claimCode,
            @NonNull Date claimDate,
            @NonNull Date startDate,
            @NonNull Date endDate,
            @NonNull String visitType,
            @NonNull String icdCode,
            @Nullable String icdCode1,
            @Nullable String icdCode2,
            @Nullable String icdCode3,
            @Nullable String icdCode4,
            @Nullable String guaranteeNumber,
            @Nullable String referralHf,
            @Nullable String patientCondition,
            @Nullable String preAuthorisation,
            @NonNull List<PendingClaimGQL.Service> services,
            @NonNull List<PendingClaimGQL.Medication> medications
    ) {
        this.claimDate = claimDate;
        this.claimAdmin = claimAdmin;
        this.healthFacilityCode = healthFacilityCode;
        this.claimCode = claimCode;
        this.guaranteeNumber = guaranteeNumber;
        this.chfId = chfId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.icdCode = icdCode;
        this.icdCode1 = icdCode1;
        this.icdCode2 = icdCode2;
        this.icdCode3 = icdCode3;
        this.icdCode4 = icdCode4;
        this.visitType = visitType;
        this.services = services;
        this.medications = medications;
        this.referalHF = referralHf;
        this.patientCondition = patientCondition;
        this.preAuthorization = preAuthorisation;
    }

    protected PendingClaimGQL(Parcel in) {
        claimDate = new Date(in.readLong());
        claimAdmin = in.readString();
        healthFacilityCode = in.readString();
        claimCode = in.readString();
        guaranteeNumber = in.readString();
        chfId = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        icdCode = in.readString();
        icdCode1 = in.readString();
        icdCode2 = in.readString();
        icdCode3 = in.readString();
        icdCode4 = in.readString();
        visitType = in.readString();
        referalHF = in.readString();
        patientCondition = in.readString();
        preAuthorization = in.readString();
        services = in.createTypedArrayList(PendingClaimGQL.Service.CREATOR);
        medications = in.createTypedArrayList(PendingClaimGQL.Medication.CREATOR);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(claimDate.getTime());
        dest.writeString(healthFacilityCode);
        dest.writeString(claimAdmin);
        dest.writeString(claimCode);
        dest.writeString(guaranteeNumber);
        dest.writeString(chfId);
        dest.writeLong(startDate.getTime());
        dest.writeLong(endDate.getTime());
        dest.writeString(icdCode);
        dest.writeString(icdCode1);
        dest.writeString(icdCode2);
        dest.writeString(icdCode3);
        dest.writeString(icdCode4);
        dest.writeString(visitType);
        dest.writeTypedList(services);
        dest.writeTypedList(medications);
        dest.writeString(referalHF);
        dest.writeString(patientCondition);
        dest.writeString(preAuthorization);
    }

    public int describeContents() {
        return 0;
    }

    @NonNull
    public Date getClaimDate() {
        return claimDate;
    }

    @NonNull
    public String getClaimAdmin(){
        return claimAdmin;
    }

    @NonNull
    public String getHealthFacilityCode() {
        return healthFacilityCode;
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

    @Nullable
    public String getReferalHF(){ return referalHF; }

    @Nullable
    public String getPatientCondition(){ return patientCondition; }

    @Nullable
    public String getPreAuthorization(){ return preAuthorization; }

    @NonNull
    public List<PendingClaimGQL.Service> getServices() {
        return services;
    }

    @NonNull
    public List<PendingClaimGQL.Medication> getMedications() {
        return medications;
    }

    public static final Parcelable.Creator<PendingClaimGQL> CREATOR = new Parcelable.Creator<>() {
        @Override
        public PendingClaimGQL createFromParcel(Parcel in) {
            return new PendingClaimGQL(in);
        }

        @Override
        public PendingClaimGQL[] newArray(int size) {
            return new PendingClaimGQL[size];
        }
    };

    public static class Service implements Parcelable {
        @NonNull
        public static List<PendingClaimGQL.Service> fromJson(@NonNull JSONArray array) throws Exception {
            List<PendingClaimGQL.Service> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject service = array.getJSONObject(i);
                Log.e("service", service.toString());
                JSONArray arrSubServices = new JSONArray();
                JSONArray arrSubItems = new JSONArray();
                if (service.has("SubServicesItems")){
                    JSONArray arrSubServicesItems = service.getJSONArray("SubServicesItems");
                    for (int j=0; j< arrSubServicesItems.length();j++){
                        if(arrSubServicesItems.getJSONObject(j).getString("Type").equals("S")){
                            arrSubServices.put(arrSubServicesItems.getJSONObject(j));
                        }else if(arrSubServicesItems.getJSONObject(j).getString("Type").equals("I")){
                            arrSubItems.put(arrSubServicesItems.getJSONObject(j));
                        }
                    }
                }
                list.add(new PendingClaimGQL.Service(
                        /* id */ service.getString("ServiceId"),
                        /* code */ service.getString("ServiceCode"),
                        /* price */ Double.valueOf(service.getString("ServicePrice")),
                        /* quantityProvide */ service.getString("ServiceQuantity"),
                        /* subservices = */ SubServiceItem.fromJson(arrSubServices),
                        /* subItems = */ SubServiceItem.fromJson(arrSubItems)
                ));
            }

            return list;
        }

        @NonNull
        private final String code;
        @NonNull
        private final String id;
        private final double price;
        private final String quantity;
        private final List<PendingClaimGQL.SubServiceItem> subServices;
        private final List<PendingClaimGQL.SubServiceItem> subItems;

        public Service(
                @NonNull String id,
                @NonNull String code,
                double price,
                String quantity,
                List<PendingClaimGQL.SubServiceItem> subServices,
                List<PendingClaimGQL.SubServiceItem> subItems
        ) {
            this.id = id;
            this.code = code;
            this.price = price;
            this.quantity = quantity;
            this.subServices = subServices;
            this.subItems = subItems;
        }

        protected Service(Parcel in) {
            id = in.readString();
            code = in.readString();
            price = in.readDouble();
            quantity = in.readString();
            subServices = in.createTypedArrayList(SubServiceItem.CREATOR);
            subItems = in.createTypedArrayList(SubServiceItem.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(code);
            dest.writeDouble(price);
            dest.writeString(quantity);
            dest.writeTypedList(subServices);
            dest.writeTypedList(subItems);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getId(){return id;}

        @NonNull
        public String getCode() {
            return code;
        }

        public double getPrice() {
            return price;
        }

        public String getQuantity() {
            return quantity;
        }

        public List<PendingClaimGQL.SubServiceItem> getSubServices(){
            return subServices;
        }

        public List<PendingClaimGQL.SubServiceItem> getSubItems(){
            return subItems;
        }

        public static final Creator<PendingClaimGQL.Service> CREATOR = new Creator<>() {
            @Override
            public PendingClaimGQL.Service createFromParcel(Parcel in) {
                return new PendingClaimGQL.Service(in);
            }

            @Override
            public PendingClaimGQL.Service[] newArray(int size) {
                return new PendingClaimGQL.Service[size];
            }
        };
    }

    public static class Medication implements Parcelable{
        @NonNull
        public static List<PendingClaimGQL.Medication> fromJson(@NonNull JSONArray array) throws Exception {
            List<PendingClaimGQL.Medication> items = new ArrayList<>();
            for( int i = 0; i<array.length(); i++){
                JSONObject item = array.getJSONObject(i);
                items.add(
                        new PendingClaimGQL.Medication(
                                /* id */ item.getString("ItemId"),
                                /* code */ item.getString("ItemCode"),
                                /* price */ Double.valueOf(item.getString("ItemPrice")),
                                /* qty provide = */ item.getString("ItemQuantity")
                                )
                );
            }
            return items;
        }

        @NonNull
        private final String id;
        @NonNull
        private final String code;
        private final double price;
        private final String quantity;

        public Medication(
                @NonNull String id,
                @NonNull String code,
                double price,
                String quantity
        ) {
            this.id = id;
            this.code = code;
            this.price = price;
            this.quantity = quantity;
        }

        protected Medication(Parcel in) {
            id = in.readString();
            code = in.readString();
            price = in.readDouble();
            quantity = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(code);
            dest.writeDouble(price);
            dest.writeString(quantity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getId(){ return id;}

        @NonNull
        public String getCode() {
            return code;
        }

        public double getPrice() {
            return price;
        }

        public String getQuantity() {
            return quantity;
        }

        public static final Creator<PendingClaimGQL.Medication> CREATOR = new Creator<>() {
            @Override
            public PendingClaimGQL.Medication createFromParcel(Parcel in) {
                return new PendingClaimGQL.Medication(in);
            }

            @Override
            public PendingClaimGQL.Medication[] newArray(int size) {
                return new PendingClaimGQL.Medication[size];
            }
        };
    }

    public static class SubServiceItem implements Parcelable{
        @NonNull
        public static List<PendingClaimGQL.SubServiceItem> fromJson(@NonNull JSONArray array) throws Exception{
            List<PendingClaimGQL.SubServiceItem> subServiceItems = new ArrayList<>();
            for( int i = 0; i<array.length(); i++){
                JSONObject subServiceItem = array.getJSONObject(i);
                subServiceItems.add(
                        new PendingClaimGQL.SubServiceItem(
                                /* code = */ subServiceItem.getString("Code"),
                                /* quantity = */ Integer.valueOf(subServiceItem.getString("Quantity")),
                                /* price = */ subServiceItem.getString("Price")
                        )
                );
            }
            return subServiceItems;
        }

        @NonNull
        String code;
        int qty;
        String price;

        public SubServiceItem(
                @NonNull String code,
                int qty,
                String price
        ){
            this.code = code;
            this.qty = qty;
            this.price = price;
        }

        protected SubServiceItem(Parcel in) {
            code = in.readString();
            price = in.readString();
            qty = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
            dest.writeString(price);
            dest.writeInt(qty);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getCode(){ return code;}

        @NonNull
        public int getQty(){ return qty;}

        @NonNull
        public String getPrice(){ return price; }

        public static final Creator<PendingClaimGQL.SubServiceItem> CREATOR = new Creator<>() {
            @Override
            public PendingClaimGQL.SubServiceItem createFromParcel(Parcel in) {
                return new PendingClaimGQL.SubServiceItem(in);
            }

            @Override
            public PendingClaimGQL.SubServiceItem[] newArray(int size) {
                return new PendingClaimGQL.SubServiceItem[size];
            }
        };
    }
}
