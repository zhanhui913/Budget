package com.zhan.budget.Model;

import com.zhan.budget.Model.Realm.Category;

import org.parceler.Parcel;

import java.util.Date;

@Parcel(Parcel.Serialization.BEAN)
public class MonthReport {

    private Date month;
    private double costThisMonth;
    private double incomeThisMonth;

    private boolean doneCalculation;

    private Category firstCategory, secondCategory, thirdCategory;

    public MonthReport(){

    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }

    public double getCostThisMonth() {
        return costThisMonth;
    }

    public void setCostThisMonth(double costThisMonth) {
        this.costThisMonth = costThisMonth;
    }

    public double getIncomeThisMonth() {
        return incomeThisMonth;
    }

    public void setIncomeThisMonth(double changeCost) {
        this.incomeThisMonth = changeCost;
    }

    public void addCostThisMonth(double cost){
        this.costThisMonth += cost;
    }

    public void addIncomeThisMonth(double income){
        this.incomeThisMonth += income;
    }

    public boolean isDoneCalculation() {
        return doneCalculation;
    }

    public void setDoneCalculation(boolean doneCalculation) {
        this.doneCalculation = doneCalculation;
    }

    public Category getFirstCategory() {
        return firstCategory;
    }

    public void setFirstCategory(Category firstCategory) {
        this.firstCategory = firstCategory;
    }

    public Category getSecondCategory() {
        return secondCategory;
    }

    public void setSecondCategory(Category secondCategory) {
        this.secondCategory = secondCategory;
    }

    public Category getThirdCategory() {
        return thirdCategory;
    }

    public void setThirdCategory(Category thirdCategory) {
        this.thirdCategory = thirdCategory;
    }
}
