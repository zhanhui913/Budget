package com.zhan.budget.Model;

/**
 * Created by Zhan on 16-05-11.
 */
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
