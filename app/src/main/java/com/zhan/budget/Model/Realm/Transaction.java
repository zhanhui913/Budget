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

    private float price;
    private Category category;
    private Account account;
    private String dayType;
    private Location location;
    private BudgetCurrency currency;

    //exchange rate for currency at the time (Cannot rely on BudgetCurrency's rate as that property is the current one)
    //This will be 1.0f if the default budget currency is the same as this currency
    private double rate;

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public BudgetCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(BudgetCurrency currency) {
        this.currency = currency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property
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

        if(other.getCurrency() != null && currency != null){
            if(!currency.checkEquals(other.getCurrency())) return false;
        }else if(other.getCurrency() == null || currency == null){
            return false;
        }

        if(rate != other.getRate()) return false;

        return true;
    }
}
