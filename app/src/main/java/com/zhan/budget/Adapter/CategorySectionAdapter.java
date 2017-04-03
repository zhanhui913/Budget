package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;

import com.zhan.budget.Model.Realm.Category;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by zhanyap on 2017-04-03.
 */

public class CategorySectionAdapter extends SectionedRecyclerViewAdapter {

    //private List<Category> expenseList;
    //private List<Category> incomeList;
    private Fragment fragment;
    private CategorySection.ARRANGEMENT arrangement;

    public CategorySectionAdapter(Fragment fragment, CategorySection.ARRANGEMENT arrangement){
        this.fragment = fragment;
        this.arrangement = arrangement;
    }

    public void setExpenseCategoryList(List<Category> expenseList){
       // this.expenseList = expenseList;

        if(getItemCount() == 0){
            addSection(new CategorySection("Expense", this.fragment, this.arrangement, expenseList));
        }else{
            ((CategorySection)getSectionForPosition(0)).setCategoryList(expenseList);
            notifyDataSetChanged();
        }
    }

    public void setIncomeCategoryList(List<Category> incomeList){
       // this.incomeList = incomeList;

        if(getItemCount() == 1){
            addSection(new CategorySection("Income", this.fragment, this.arrangement, incomeList));
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
