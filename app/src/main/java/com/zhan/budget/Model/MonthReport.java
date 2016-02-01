package com.zhan.budget.Model;

import org.parceler.Parcel;

import java.util.Date;

@Parcel(Parcel.Serialization.BEAN)
public class MonthReport {

    private Date month;
    private float costThisMonth;
    private float changeCost;

    private boolean doneCalculation;

    public MonthReport(){

    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }

    public float getCostThisMonth() {
        return costThisMonth;
    }

    public void setCostThisMonth(float costThisMonth) {
        this.costThisMonth = costThisMonth;
    }

    public float getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(float changeCost) {
        this.changeCost = changeCost;
    }

    public void addCostThisMonth(float cost){
        this.costThisMonth += cost;
    }

    public boolean isDoneCalculation() {
        return doneCalculation;
    }

    public void setDoneCalculation(boolean doneCalculation) {
        this.doneCalculation = doneCalculation;
    }
}
