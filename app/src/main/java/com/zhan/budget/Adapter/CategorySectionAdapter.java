package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;

import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by zhanyap on 2017-04-03.
 */

public class CategorySectionAdapter extends SectionedRecyclerViewAdapter {

    private Fragment fragment;
    private CategorySection.ARRANGEMENT arrangement;
    private String expenseTAG;
    private String incomeTAG;

    public CategorySectionAdapter(Fragment fragment, CategorySection.ARRANGEMENT arrangement){
        this.fragment = fragment;
        this.arrangement = arrangement;

        expenseTAG = addSection(new CategorySection(BudgetType.EXPENSE.toString(), this.fragment, this.arrangement, new ArrayList<Category>()));
        incomeTAG = addSection(new CategorySection(BudgetType.INCOME.toString(), this.fragment, this.arrangement, new ArrayList<Category>()));
    }

    public void setExpenseCategoryList(List<Category> expenseList){
        ((CategorySection)getSection(expenseTAG)).setCategoryList(expenseList);
        notifyDataSetChanged();
    }

    public void setIncomeCategoryList(List<Category> incomeList){
        ((CategorySection)getSection(incomeTAG)).setCategoryList(incomeList);
        notifyDataSetChanged();
    }

    public List<Category> getExpenseCategoryList(){
        return ((CategorySection)getSection(expenseTAG)).getCategoryList();
    }

    public List<Category> getIncomeCategoryList(){
        return ((CategorySection)getSection(incomeTAG)).getCategoryList();
    }
}
