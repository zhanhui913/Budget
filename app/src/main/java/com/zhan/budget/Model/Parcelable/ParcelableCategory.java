package com.zhan.budget.Model.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhan.budget.Model.Category;

/**
 * Created by zhanyap on 2016-01-13.
 */
public class ParcelableCategory implements Parcelable {

    private String id;
    private String name;
    private String type;
    private float budget;

    private float cost;
    private String color;
    private int icon;

    public ParcelableCategory(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public void convertCategoryToParcelable(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType();
        this.budget = category.getBudget();
        this.cost = category.getCost();
        this.color = category.getColor();
        this.icon = category.getIcon();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeFloat(budget);
        dest.writeString(type);
        dest.writeFloat(cost);
        dest.writeString(color);
        dest.writeInt(icon);
    }

    public static final Parcelable.Creator<ParcelableCategory> CREATOR = new Parcelable.Creator<ParcelableCategory>() {

        @Override
        public ParcelableCategory createFromParcel(Parcel source) {
            return new ParcelableCategory(source);
        }

        @Override
        public ParcelableCategory[] newArray(int size) {
            return new ParcelableCategory[size];
        }
    };

    private ParcelableCategory(Parcel in){
        id = in.readString();
        name = in.readString();
        budget = in.readFloat();
        type = in.readString();
        cost = in.readFloat();
        color = in.readString();
        icon = in.readInt();
    }
}
