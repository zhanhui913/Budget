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
    private CategorySection expenseSection, incomeSection;
    private OnCategorySectionAdapterInteractionListener mListener;

    public CategorySectionAdapter(Fragment fragment, CategorySection.ARRANGEMENT arrangement){
        this.fragment = fragment;
        this.arrangement = arrangement;

        expenseSection = new CategorySection(BudgetType.EXPENSE.toString(), this.fragment, this.arrangement, new ArrayList<Category>());
        incomeSection = new CategorySection(BudgetType.INCOME.toString(), this.fragment, this.arrangement, new ArrayList<Category>());

        expenseTAG = addSection(expenseSection);
        incomeTAG = addSection(incomeSection);
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

    public void clearBothList(){
        clearExpenseList();
        clearIncomeList();
    }

    public void clearExpenseList(){
        ((CategorySection) getSection(expenseTAG)).getCategoryList().clear();
        notifyDataSetChanged();
    }

    public void clearIncomeList(){
        ((CategorySection) getSection(incomeTAG)).getCategoryList().clear();
        notifyDataSetChanged();
    }

    public void setInteraction(OnCategorySectionAdapterInteractionListener mListener){
        this.mListener = mListener;

        expenseSection.setListener(this.mListener);
        incomeSection.setListener(this.mListener);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCategorySectionAdapterInteractionListener {
        void onDeleteCategory(int position);

        void onEditCategory(int position);

        void onClick(int position);
    }
}
