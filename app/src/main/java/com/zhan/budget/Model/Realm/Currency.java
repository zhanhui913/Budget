package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import io.realm.CurrencyRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhan on 2016-10-11.
 */

@Parcel(implementations = {CurrencyRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Currency.class})
public class Currency extends RealmObject {

    @PrimaryKey
    private String id;

    private String name;
    private String symbol;
    private String country;


    public Currency(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(Currency other){
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(!name.equalsIgnoreCase(other.getName())) return false;
        if(!symbol.equalsIgnoreCase(other.getSymbol())) return false;
        if(!country.equalsIgnoreCase(other.getCountry())) return false;
        return true;
    }
}
