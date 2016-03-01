package com.zhan.budget.Model;

/**
 * Created by zhanyap on 2016-02-09.
 */
public class CategoryIconColor {

    private int icon;
    private String color;
    private boolean isSelected;

    public CategoryIconColor(){}

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
