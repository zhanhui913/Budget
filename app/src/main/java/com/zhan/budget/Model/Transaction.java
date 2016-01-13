package com.zhan.budget.Model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhan on 15-12-14.
 */
public class Transaction extends RealmObject {

    @PrimaryKey
    private String id;

    private String note;

    @Index
    private Date date;

    private float price;

    private Category category;

    public Transaction(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    /*
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
    */
}
