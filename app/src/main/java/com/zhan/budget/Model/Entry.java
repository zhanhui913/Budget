package com.zhan.budget.Model;

import android.support.annotation.Nullable;


import java.util.Date;

/**
 * Created by Zhan on 15-12-14.
 */
public class Entry {

    //@PrimaryKey
    private int id;

    //@Column
    private Date date;

    //@Column
    private float price;

    //@Column
    //@Nullable
    private String note;

    //@Column
    private Category category;

    public Entry(){

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
