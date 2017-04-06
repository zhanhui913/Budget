package com.zhan.budget.Model.Realm;

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

    private double price;
    private Category category;
    private Account account;
    private String dayType;
    private Location location;
    private String scheduledTransactionId;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getScheduledTransactionId() {
        return scheduledTransactionId;
    }

    public void setScheduledTransactionId(String scheduledTransactionId) {
        this.scheduledTransactionId = scheduledTransactionId;
    }

////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property that isnt ignored
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(Transaction other){
        if(!id.equalsIgnoreCase(other.getId())) return false;

        if(other.getNote() != null && note != null){
            if(!note.equalsIgnoreCase(other.getNote())) return false;
        }

        if(date.getTime() != other.getDate().getTime()) return false;
        if(price != other.getPrice()) return false;
        if(!dayType.equalsIgnoreCase(other.getDayType())) return false;
        if(category != null && other.getCategory() != null && !category.checkEquals(other.getCategory())) return false;
        if(account != null && other.getAccount() != null && !account.checkEquals(other.getAccount())) return false;

        if(other.getLocation() != null && location != null){
            if(!location.checkEquals(other.getLocation())) return false;
        }

        if(!scheduledTransactionId.equalsIgnoreCase(other.getScheduledTransactionId())){
            return false;
        }

        return true;
    }
}
