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
    private String id;

    private String name;
    private String color;

    @Ignore
    private int amount;

    public Location(){

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Pie Data Interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public float getPieDataCost(){
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
    // Realm object check equality in terms of property
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(Location other){
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(!name.equalsIgnoreCase(other.getName())) return false;
        if(!color.equalsIgnoreCase(other.getColor())) return false;
        return true;
    }
}
