package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class Service implements Parcelable {

    @NonNull
    private String id;
    @NonNull
    private final String code;
    @NonNull
    private final String name;

    private final double price;

    @NonNull
    private final String currency;

    @Nullable
    private List<SubServiceItem> subServices;
    @Nullable
    private List<SubServiceItem> subItems;
    @Nullable
    private String packageType;

    public Service(
            @NonNull String id,
            @NonNull String code,
            @NonNull String name,
            double price,
            @NonNull String currency,
            @Nullable String packageType,
            @Nullable List<SubServiceItem> subServices,
            @Nullable List<SubServiceItem> subItems
    ){
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.packageType = packageType;
        this.subServices = subServices;
        this.subItems = subItems;
    }

    public Service(
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

    protected Service(Parcel in) {
        id = in.readString();
        code = in.readString();
        name = in.readString();
        price = in.readDouble();
        currency = in.readString();
        packageType = in.readString();
        subServices = in.createTypedArrayList(SubServiceItem.CREATOR);
        subItems = in.createTypedArrayList(SubServiceItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(currency);
        dest.writeString(packageType);
        dest.writeTypedList(subServices);
        dest.writeTypedList(subItems);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getId(){ return id; }

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

    @NonNull
    public  String getPackageType(){ return packageType; }
    @Nullable
    public List<SubServiceItem> getSubServices() {
        return subServices;
    }
    @Nullable
    public List<SubServiceItem> getSubItems() {
        return subItems;
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
