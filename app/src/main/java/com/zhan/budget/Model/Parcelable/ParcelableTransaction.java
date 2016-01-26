package com.zhan.budget.Model.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhan.budget.Model.Transaction;
import com.zhan.budget.Util.Util;

import java.util.Date;

/**
 * Created by zhanyap on 2016-01-13.
 */
public class ParcelableTransaction implements Parcelable {

    private String id;
    private String note;
    private Date date;
    private float price;
    private ParcelableCategory category;
    private ParcelableAccount account;

    public ParcelableTransaction(){

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

    public ParcelableCategory getCategory() {
        return category;
    }

    public void setCategory(ParcelableCategory category) {
        this.category = category;
    }

    public ParcelableAccount getAccount() {
        return account;
    }

    public void setAccount(ParcelableAccount account) {
        this.account = account;
    }

    public void convertTransactionToParcelable(Transaction transaction){
        this.id = transaction.getId();
        this.date = transaction.getDate();
        this.note = transaction.getNote();
        this.price = transaction.getPrice();

        ParcelableAccount pa = new ParcelableAccount();
        pa.convertAccountToParcelable(transaction.getAccount());
        this.account = pa;

        ParcelableCategory pc = new ParcelableCategory();
        pc.convertCategoryToParcelable(transaction.getCategory());
        this.category = pc;
    }

    public Transaction convertParcelableToTransaction(){
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setNote(this.note);
        transaction.setPrice(this.price);
        transaction.setDate(this.date);
        transaction.setAccount(this.account.convertParcelableToAccount());
        transaction.setCategory(this.category.convertParcelableToCategory());
        return transaction;
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
        dest.writeString(id);
        dest.writeString(note);
        dest.writeString(Util.convertDateToString(date));
        dest.writeFloat(price);

        //Add inner class
        category.writeToParcel(dest, flags);
        account.writeToParcel(dest, flags);
    }

    public static final Creator<ParcelableTransaction> CREATOR = new Creator<ParcelableTransaction>() {

        @Override
        public ParcelableTransaction createFromParcel(Parcel source) {
            return new ParcelableTransaction(source);
        }

        @Override
        public ParcelableTransaction[] newArray(int size) {
            return new ParcelableTransaction[size];
        }
    };

    private ParcelableTransaction(Parcel in){
        id = in.readString();
        note = in.readString();
        date = Util.convertStringToDate(in.readString());
        price = in.readFloat();

        category = ParcelableCategory.CREATOR.createFromParcel(in);
        account = ParcelableAccount.CREATOR.createFromParcel(in);
    }
}
