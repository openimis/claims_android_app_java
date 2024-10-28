package org.openimis.imisclaims.domain.entity;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
public class SubServiceItem implements Parcelable{
    @NonNull
    private final String id;
    @NonNull
    private final int qty;
    @NonNull
    private final String price;
    public SubServiceItem(
            @NonNull String id,
            @NonNull int qty,
            @NonNull String price
    ) {
        this.id = id;
        this.qty = qty;
        this.price = price;
    }
    protected SubServiceItem(Parcel in) {
        id = in.readString();
        qty = in.readInt();
        price = in.readString();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(qty);
        dest.writeString(price);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @NonNull
    public String getId() {
        return id;
    }
    @NonNull
    public int getQty() {
        return qty;
    }
    @NonNull
    public String getPrice() {
        return price;
    }
    public static final Parcelable.Creator<SubServiceItem> CREATOR = new Parcelable.Creator<>() {
        @Override
        public SubServiceItem createFromParcel(Parcel in) {
            return new SubServiceItem(in);
        }
        @Override
        public SubServiceItem[] newArray(int size) {
            return new SubServiceItem[size];
        }
    };
}