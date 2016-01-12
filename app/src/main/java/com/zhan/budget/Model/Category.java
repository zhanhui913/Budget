package com.zhan.budget.Model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zhan on 15-12-14.
 */
public class Category implements Parcelable{

    private int id;
    private String name;
    private BudgetType type;
    private float budget;
    private float cost;
    private String color;
    private int icon;

    public Category(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public void addCost(float cost){
        this.cost += cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BudgetType getType() {
        return type;
    }

    public void setType(BudgetType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String toString(){
        return "{Id:"+id+", name:"+name+", budget:"+budget+", cost"+cost+", color:"+color+", icon:"+icon+"}";
    }

    public int convertBudgetTypeToInt(BudgetType type){
        return (type == BudgetType.EXPENSE)? -1 : 1;
    }

    public BudgetType convertIntToBudgetType(int value){
        return (value == -1)? BudgetType.EXPENSE : BudgetType.INCOME;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Parcelable
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeFloat(budget);
        dest.writeInt(convertBudgetTypeToInt(type));
        dest.writeFloat(cost);
        dest.writeString(color);
        dest.writeInt(icon);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {

        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    private Category(Parcel in){
        id = in.readInt();
        name = in.readString();
        budget = in.readFloat();
        type = convertIntToBudgetType(in.readInt());
        cost = in.readFloat();
        color = in.readString();
        icon = in.readInt();
    }
}
