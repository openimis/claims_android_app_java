package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Medication implements Parcelable {

    @Nullable
    private String id;
    @NonNull
    private final String code;
    @NonNull
    private final String name;

    private final double price;

    @NonNull
    private final String currency;

    public Medication(
            @Nullable String id,
            @NonNull String code,
            @NonNull String name,
            double price,
            @NonNull String currency
    ){
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.currency = currency;
    }

    public Medication(
            @NonNull String code,
            @NonNull String name,
            double price,
            @NonNull String currency
    ){
        this.code = code;
        this.name = name;
        this.price = price;
        this.currency = currency;
    }

    protected Medication(Parcel in) {
        id = in.readString();
        code = in.readString();
        name = in.readString();
        price = in.readDouble();
        currency = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(currency);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public  String getId(){ return id; }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @NonNull
    public String getCurrency() {
        return currency;
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
