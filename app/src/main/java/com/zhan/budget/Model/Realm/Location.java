package com.zhan.budget.Model.Realm;

import com.zhan.budget.Model.PieDataCostInterface;

import org.parceler.Parcel;

import io.realm.LocationRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {LocationRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Location.class})
public class Location extends RealmObject implements PieDataCostInterface {

    @PrimaryKey
    private String name;
    private String color;

    @Ignore
    private int amount;

    //When migration to #3
    //Old Locations (ie those with wrong name format) are set to false
   // private boolean isNew;

    public Location(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addAmount(int amountToAdd){
        this.amount += amountToAdd;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
/*
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
*/
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Pie Data Interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public double getPieDataCost(){
        return this.amount;
    }

    @Override
    public String getPieDataName(){
        return this.name;
    }

    @Override
    public String getPieDataColor(){
        return this.color;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property that isnt ignored
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(Location other){
        if(!name.equalsIgnoreCase(other.getName())) return false;
        if(!color.equalsIgnoreCase(other.getColor())) return false;
        //if(isNew != other.isNew()) return false;
        return true;
    }
}
