package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import io.realm.AccountRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {AccountRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Account.class})
public class Account extends RealmObject{

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
}
