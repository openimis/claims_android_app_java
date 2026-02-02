package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Program implements Parcelable {

    @NonNull
    private final String idProgram;
    @NonNull
    private final String code;
    @NonNull
    private final String nameProgram;

    public Program(
            @NonNull String idProgram,
            @NonNull String code,
            @NonNull String nameProgram
    ) {
        this.idProgram = idProgram;
        this.code = code;
        this.nameProgram = nameProgram;
    }

    protected Program(Parcel in) {
        idProgram = in.readString();
        code = in.readString();
        nameProgram = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idProgram);
        dest.writeString(code);
        dest.writeString(nameProgram);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getIdProgram() {
        return idProgram;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getNameProgram() {
        return nameProgram;
    }

    public static final Parcelable.Creator<Program> CREATOR = new Parcelable.Creator<>() {
        @Override
        public Program createFromParcel(Parcel in) {
            return new Program(in);
        }

        @Override
        public Program[] newArray(int size) {
            return new Program[size];
        }
    };
}