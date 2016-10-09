package com.mobile.tradesies.datacontracts;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemPhoto implements Parcelable {
    public int PhotoId;
    public boolean IsPrimary;
    public String ImageData;
    public String Url;

    public ItemPhoto(){}

    public ItemPhoto(Parcel in){
        PhotoId = in.readInt();
        IsPrimary = in.readByte() == (byte) 0x01;
        ImageData = in.readString();
        Url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(PhotoId);
        dest.writeByte(IsPrimary ? (byte)0x01 : (byte)0x00);
        dest.writeString(ImageData);
        dest.writeString(Url);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ItemPhoto createFromParcel(Parcel in) {
            return new ItemPhoto(in);
        }

        public ItemPhoto[] newArray(int size) {
            return new ItemPhoto[size];
        }
    };
}