package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ClaimAdmin implements Parcelable {

    @NonNull
    private final String lastName;
    @NonNull
    private final String otherNames;
    @NonNull
    private final String claimAdminCode;
    @NonNull
    private final String hfCode;

    public ClaimAdmin(
            @NonNull String lastName,
            @NonNull String otherNames,
            @NonNull String claimAdminCode,
            @NonNull String healthFacilityCode
    ){
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.claimAdminCode = claimAdminCode;
        this.hfCode = healthFacilityCode;
    }

    protected ClaimAdmin(Parcel in) {
        lastName = in.readString();
        otherNames = in.readString();
        claimAdminCode = in.readString();
        hfCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lastName);
        dest.writeString(otherNames);
        dest.writeString(claimAdminCode);
        dest.writeString(hfCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @NonNull
    public String getDisplayName() {
        return lastName + " " + otherNames;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    @NonNull
    public String getOtherNames() {
        return otherNames;
    }

    @NonNull
    public String getClaimAdminCode() {
        return claimAdminCode;
    }

    @NonNull
    public String getHealthFacilityCode() {
        return hfCode;
    }

    public static final Creator<ClaimAdmin> CREATOR = new Creator<>() {
        @Override
        public ClaimAdmin createFromParcel(Parcel in) {
            return new ClaimAdmin(in);
        }

        @Override
        public ClaimAdmin[] newArray(int size) {
            return new ClaimAdmin[size];
        }
    };
}
