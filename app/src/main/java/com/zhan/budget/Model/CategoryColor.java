package com.zhan.budget.Model;

/**
 * Created by zhanyap on 2016-02-09.
 */
public class CategoryColor {

    private int color;
    private boolean isSelected;

    public CategoryColor(){}

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
