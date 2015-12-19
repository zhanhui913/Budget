package com.zhan.budget.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhanyap on 15-08-27.
 * Base class that contains pk, name, and description.
 * This class will typically be extended off by other classes.
 */
public class Base implements Parcelable{

    protected int pk;
    protected String name;
    protected String description;

    public Base(){}

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Parcelable
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(pk);
        dest.writeString(name);
        dest.writeString(description);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Creator<Base> CREATOR = new Creator<Base>() {

        @Override
        public Base createFromParcel(Parcel source) {
            return new Base(source);
        }

        @Override
        public Base[] newArray(int size) {
            return new Base[size];
        }
    };

    private Base(Parcel in){
        pk = in.readInt();
        name = in.readString();
        description = in.readString();
    }
}
