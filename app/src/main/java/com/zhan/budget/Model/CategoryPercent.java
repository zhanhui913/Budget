package com.zhan.budget.Model;

import com.zhan.budget.Model.Realm.Category;

/**
 * Created by Zhan on 16-02-17.
 */
public class CategoryPercent {

    private Category category;
    private float percent;

    public CategoryPercent(){

    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}
