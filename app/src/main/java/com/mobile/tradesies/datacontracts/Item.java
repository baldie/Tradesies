package com.mobile.tradesies.datacontracts;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.acl.Owner;

/**
 * Created by David on 4/18/2015.
 */
public class Item implements Parcelable {
    public int Id;
    public String Title;
    public String Description;
    public String Latitude;
    public String Longitude;
    public boolean Favored;
    public boolean IsActive;
    public int OwnerUserId;
    public String OwnerPhotoUrl;
    public int[] Categories;
    public String Distance;
    public ItemPhoto[] Photos;

    public Item(){}

    public Item(Parcel in)
    {
        Id = in.readInt();
        Title = in.readString();
        Description = in.readString();
        Latitude = in.readString();
        Longitude = in.readString();
        Favored = in.readByte() == (byte) 0x01;
        IsActive = in.readByte() == (byte) 0x01;
        OwnerUserId = in.readInt();
        OwnerPhotoUrl = in.readString();
        int size = in.readInt();
        Categories = new int[size];
        in.readIntArray(Categories);
        Distance = in.readString();
        size = in.readInt();
        Photos = new ItemPhoto[size];
        in.readTypedArray(Photos, ItemPhoto.CREATOR);
    }

    public ItemPhoto getPrimaryPhoto(){
        ItemPhoto primary = Photos[0];
        for(int i=0; i < Photos.length; i++) {
        if (Photos[i].IsPrimary)
            primary = Photos[i];
            break;
        }
        return primary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Title);
        dest.writeString(Description);
        dest.writeString(Latitude);
        dest.writeString(Longitude);
        dest.writeByte(Favored ? (byte) 0x00 : (byte) 0x01);
        dest.writeByte(IsActive ? (byte) 0x00 : (byte) 0x01);
        dest.writeInt(OwnerUserId);
        dest.writeString(OwnerPhotoUrl);
        dest.writeInt(Categories == null ? 0 : Categories.length);
        dest.writeIntArray(Categories);
        dest.writeString(Distance);
        dest.writeInt(Photos == null ? 0 : Photos.length);
        dest.writeTypedArray(Photos, flags);
    }

    @Override
    public int hashCode() {
        return (Id + "" + OwnerUserId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item))
            return false;
        if (obj == this)
            return true;

        Item rhs = (Item) obj;
        return Id == rhs.Id && OwnerUserId == rhs.OwnerUserId;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}