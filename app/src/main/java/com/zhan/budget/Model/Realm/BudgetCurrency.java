package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

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
    private String currencyCode;

    private String currencyName;
    private boolean isDefault;

    //This will be 1.0f if the default budget currency is the same as this budget currency
    private double rate;

    public BudgetCurrency(){

    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String code) {
        this.currencyCode = code;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property that isnt ignored
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(BudgetCurrency other){
        if(!currencyCode.equalsIgnoreCase(other.getCurrencyCode())) return false;
        if(!currencyName.equalsIgnoreCase(other.getCurrencyName())) return false;
        if(isDefault != (other.isDefault())) return false;
        if(rate != (other.getRate())) return false;
        return true;
    }
}
