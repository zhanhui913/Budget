package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.BudgetCurrencyRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhan on 2016-10-11.
 */

@Parcel(implementations = {BudgetCurrencyRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {BudgetCurrency.class})
public class BudgetCurrency extends RealmObject {

    @PrimaryKey
    private String country;

    private String currencyCode;
    private String symbol;
    private String language;
    private boolean isDefault;
    private double rate;
    private Date date;

    public BudgetCurrency(){

    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String code) {
        this.currencyCode = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(BudgetCurrency other){
        if(!currencyCode.equalsIgnoreCase(other.getCurrencyCode())) return false;
        if(!symbol.equalsIgnoreCase(other.getSymbol())) return false;
        if(!country.equalsIgnoreCase(other.getCountry())) return false;
        if(isDefault != (other.isDefault())) return false;
        if(rate != (other.getRate())) return false;
        if(language != (other.getLanguage())) return false;
        return true;
    }
}
