package com.zhan.budget.Model;

/**
 * Created by Zhan on 16-05-24.
 */
public class Attribution {

    private String name, author;
    private String color;
    private boolean isOpenSource;

    public Attribution(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isOpenSource() {
        return isOpenSource;
    }

    public void setOpenSource(boolean openSource) {
        isOpenSource = openSource;
    }
}
