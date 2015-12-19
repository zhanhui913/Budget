package com.zhan.budget.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhanyap on 15-09-01.
 * MetaMission class that contains pk, name, description, and the path of the json for the Mission.
 */
public class MetaMission extends Base{

    private String jsonPath;

    public MetaMission(){
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Parcelable
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pk);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(jsonPath);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<MetaMission> CREATOR = new Parcelable.Creator<MetaMission>() {

        @Override
        public MetaMission createFromParcel(Parcel source) {
            return new MetaMission(source);
        }

        @Override
        public MetaMission[] newArray(int size) {
            return new MetaMission[size];
        }
    };

    private MetaMission(Parcel in){
        pk = in.readInt();
        name = in.readString();
        description = in.readString();
        jsonPath = in.readString();
    }
}
