package org.openimis.imisclaims.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ModuleConfiguration {

    @Nullable
    private String id;

    @NonNull
    private final String name;

    @NonNull
    private final String config;

    public ModuleConfiguration(
            @Nullable String id,
            @NonNull String name,
            @NonNull String config
    ){
        this.id = id;
        this.name = name;
        this.config = config;
    }

    protected ModuleConfiguration(Parcel in) {
        id = in.readString();
        name = in.readString();
        config = in.readString();
    }

    public void ModuleConfiguration(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(config);
    }

    public int describeContents() {
        return 0;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getConfig() {
        return config;
    }

    public static final Parcelable.Creator<ModuleConfiguration> CREATOR = new Parcelable.Creator<>() {
        @Override
        public ModuleConfiguration createFromParcel(Parcel in) {
            return new ModuleConfiguration(in);
        }

        @Override
        public ModuleConfiguration[] newArray(int size) {
            return new ModuleConfiguration[size];
        }
    };
}
