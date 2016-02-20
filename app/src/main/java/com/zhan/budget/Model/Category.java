package com.zhan.budget.Model;

import org.parceler.Parcel;

import io.realm.CategoryRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {CategoryRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Category.class})
public class Category extends RealmObject{

    @PrimaryKey
    private String id;
    private String name;
    private String type;
    private float budget;

    @Ignore
    private float cost;
    private int color;
    private int icon;

    public Category(){

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
