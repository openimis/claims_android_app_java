package org.openimis.imisclaims.domain.entity;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
public class HealthFacility implements Parcelable {
    @NonNull
    private final String id;
    @Nullable
    private final String code;
    @Nullable
    private final String name;
    public HealthFacility (
            @NonNull String id,
            @Nullable String code,
            @Nullable String name
    ){
        this.id = id;
        this.code = code;
        this.name = name;
    }
    protected HealthFacility (Parcel in){
        id = in.readString();
        code = in.readString();
        name = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(id);
        dest.writeString(code);
        dest.writeString(name);
    }
    @NonNull
    public String getId (){
        return id;
    }
    @Nullable
    public String getCode(){
        return code;
    }
    @Nullable
    public String getName(){
        return name;
    }
    public static final Creator<HealthFacility> CREATOR = new Creator<>() {
        @Override
        public HealthFacility createFromParcel(Parcel in) {
            return new HealthFacility(in);
        }
        @Override
        public HealthFacility[] newArray(int size) {
            return new HealthFacility[size];
        }
    };
}