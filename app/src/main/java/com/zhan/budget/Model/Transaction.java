package com.zhan.budget.Model;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.TransactionRealmProxy;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {TransactionRealmProxy.class},
    value = Parcel.Serialization.BEAN,
    analyze = {Transaction.class})
public class Transaction extends RealmObject {

    @PrimaryKey
    private String id;

    private String note;

    @Index
    private Date date;

    private float price;
    private Category category;
    private Account account;
    private String dayType;

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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }
}
