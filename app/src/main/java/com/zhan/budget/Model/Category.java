package com.zhan.budget.Model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zhan on 15-12-14.
 */

//@Table
public class Category implements Parcelable{

    //@PrimaryKey
    private int id;

    //@Column(indexed = true)
    private String name;

    //@Column
    private BudgetType type;

    private float budget;

    private float cost;

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
        dest.writeFloat(cost);
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
        cost = in.readFloat();
    }
}
