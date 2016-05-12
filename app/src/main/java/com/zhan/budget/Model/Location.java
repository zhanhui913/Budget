package com.zhan.budget.Model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Location {

    private String name;
    private int amount;

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
}
