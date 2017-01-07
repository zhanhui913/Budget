package com.zhan.budget.Model.Realm;

import com.zhan.budget.Model.PieDataCostInterface;

import org.parceler.Parcel;

import io.realm.AccountRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {AccountRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Account.class})
public class Account extends RealmObject implements PieDataCostInterface{

    @PrimaryKey
    private String id;

    private String name;
    private boolean isDefault;
    private String color;

    @Ignore
    private float cost;

    public Account(){

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
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
        return this.getCost();
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

    public boolean checkEquals(Account other){
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(!name.equalsIgnoreCase(other.getName())) return false;
        if(isDefault != other.isDefault()) return false;
        if(!color.equalsIgnoreCase(other.getColor())) return false;
        return true;
    }
}
