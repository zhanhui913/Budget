package com.zhan.budget.Model;


/**
 * Created by Zhan on 15-12-14.
 */

//@Table
public class Category {

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
}
