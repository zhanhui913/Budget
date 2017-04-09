package com.zhan.budget.Model.Realm;

import com.zhan.budget.Model.PieDataCostInterface;

import org.parceler.Parcel;

import io.realm.CategoryRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {CategoryRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Category.class})
public class Category extends RealmObject implements PieDataCostInterface{

    @PrimaryKey
    private String id;
    private String name;
    private String type;
    private double budget;
    private String color;
    private String icon;
    private int index;
    private boolean isText;

    @Ignore
    private double cost;

    @Ignore
    private float percent;

    @Ignore
    private boolean isSelected;

    public Category(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isText() {
        return isText;
    }

    public void setText(boolean text) {
        isText = text;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Pie Data Interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public double getPieDataCost(){
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

    public boolean checkEquals(Category other){
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(!name.equalsIgnoreCase(other.getName())) return false;
        if(!type.equalsIgnoreCase(other.getType())) return false;
        if(budget != other.getBudget()) return false;
        if(!color.equalsIgnoreCase(other.getColor())) return false;
        if(!icon.equalsIgnoreCase(other.getIcon())) return false;
        if(index != other.getIndex()) return false;
        if(isText != other.isText()) return false;
        return true;
    }
}
