package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.ScheduledTransactionRealmProxy;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {ScheduledTransactionRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {ScheduledTransaction.class})
public class ScheduledTransaction extends RealmObject {

    @PrimaryKey
    private String id;
    private String note;
    private double price;
    private Category category;
    private Account account;
    private String dayType;
    private Location location;
    private Date lastTransactionDate;
    private int repeatUnit;
    private String repeatType;

    public ScheduledTransaction(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public int getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(int repeatUnit) {
        this.repeatUnit = repeatUnit;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public Date getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(Date lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property that isnt ignored
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(ScheduledTransaction other){
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(other.getNote() != null && note != null){
            if(!note.equalsIgnoreCase(other.getNote())) return false;
        }

        if(price != other.getPrice()) return false;
        if(!dayType.equalsIgnoreCase(other.getDayType())) return false;
        if(category != null && other.getCategory() != null && !category.checkEquals(other.getCategory())) return false;
        if(account != null && other.getAccount() != null && !account.checkEquals(other.getAccount())) return false;

        if(other.getLocation() != null && location != null){
            if(!location.checkEquals(other.getLocation())) return false;
        }
        if(repeatUnit != other.getRepeatUnit()) return false;
        if(!repeatType.equalsIgnoreCase(other.getRepeatType())) return false;
        if(lastTransactionDate.getTime() != other.getLastTransactionDate().getTime()) return false;

        return true;
    }
}
