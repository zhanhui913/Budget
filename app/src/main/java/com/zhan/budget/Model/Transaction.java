package com.zhan.budget.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhan.budget.Util.Util;

import java.util.Date;

/**
 * Created by Zhan on 15-12-14.
 */
public class Transaction implements Parcelable{

    //@PrimaryKey
    private int id;

    //@Column
    //@Nullable
    private String note;

    //@Column
    private Date date;

    //@Column
    private float price;

    //@Column
    private Category category;

    public Transaction(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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
        dest.writeInt(id);
        dest.writeString(note);
        dest.writeString(Util.convertDateToString(date));
        dest.writeFloat(price);

        //Add inner class
        category.writeToParcel(dest, flags);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {

        @Override
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    private Transaction(Parcel in){
        id = in.readInt();
        note = in.readString();
        date = Util.convertStringToDate(in.readString());
        price = in.readFloat();

        category = Category.CREATOR.createFromParcel(in);
    }
}
