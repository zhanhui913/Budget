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

    public Category(){

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
