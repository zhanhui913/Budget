package com.zhan.budget.Model.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhan.budget.Util.Util;

import java.util.Date;

/**
 * Created by zhanyap on 2016-01-22.
 */
public class ParcelableMonthReport implements Parcelable {

    private Date month;
    private float costThisMonth;
    private float changeCost;

    public ParcelableMonthReport(){

    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }

    public float getCostThisMonth() {
        return costThisMonth;
    }

    public void setCostThisMonth(float costThisMonth) {
        this.costThisMonth = costThisMonth;
    }

    public float getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(float changeCost) {
        this.changeCost = changeCost;
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
        dest.writeString(Util.convertDateToString(month));
        dest.writeFloat(costThisMonth);
        dest.writeFloat(changeCost);
    }

    public static final Parcelable.Creator<ParcelableMonthReport> CREATOR = new Parcelable.Creator<ParcelableMonthReport>() {

        @Override
        public ParcelableMonthReport createFromParcel(Parcel source) {
            return new ParcelableMonthReport(source);
        }

        @Override
        public ParcelableMonthReport[] newArray(int size) {
            return new ParcelableMonthReport[size];
        }
    };

    private ParcelableMonthReport(Parcel in){
        month = Util.convertStringToDate(in.readString());
        costThisMonth = in.readFloat();
        changeCost = in.readFloat();
    }
}
