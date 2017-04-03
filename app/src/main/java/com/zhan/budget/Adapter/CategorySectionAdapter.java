package com.zhan.budget.Adapter;

import com.zhan.budget.Model.Realm.Category;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by zhanyap on 2017-04-03.
 */

public class CategorySectionAdapter extends SectionedRecyclerViewAdapter {

    //private List<Category> expenseList;
    //private List<Category> incomeList;

    public CategorySectionAdapter(){

    }

    public void setExpenseCategoryList(List<Category> expenseList){
       // this.expenseList = expenseList;

        if(getSectionForPosition(0) == null){
            addSection(new CategorySection("Expense", expenseList));
        }else{
            ((CategorySection)getSectionForPosition(0)).setCategoryList(expenseList);
            notifyDataSetChanged();
        }
    }

    public void setIncomeCategoryList(List<Category> incomeList){
       // this.incomeList = incomeList;

        if(getSectionForPosition(1) == null){
            addSection(new CategorySection("Income", incomeList));
        }else{
            ((CategorySection)getSectionForPosition(1)).setCategoryList(incomeList);
            notifyDataSetChanged();
        }
    }

    public List<Category> getExpenseCategoryList(){
        return ((CategorySection)getSectionForPosition(0)).getCategoryList();
    }

    public List<Category> getIncomeCategoryList(){
        return ((CategorySection)getSectionForPosition(1)).getCategoryList();
    }
}
