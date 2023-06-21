package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Diagnosis implements Parcelable {

    @NonNull
    private final String code;
    @NonNull
    private final String name;

    public Diagnosis(
            @NonNull String code,
            @NonNull String name
    ) {
        this.code = code;
        this.name = name;
    }

    protected Diagnosis(Parcel in) {
        code = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public static final Creator<Diagnosis> CREATOR = new Creator<>() {
        @Override
        public Diagnosis createFromParcel(Parcel in) {
            return new Diagnosis(in);
        }

        @Override
        public Diagnosis[] newArray(int size) {
            return new Diagnosis[size];
        }
    };
}
