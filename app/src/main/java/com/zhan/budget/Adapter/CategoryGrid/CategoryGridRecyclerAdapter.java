package com.zhan.budget.Adapter.CategoryGrid;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class CategoryGridRecyclerAdapter extends RecyclerView.Adapter<CategoryGridRecyclerAdapter.ViewHolder> {

    protected Context context;
    protected List<Category> categoryList;
    protected OnCategoryGridAdapterInteractionListener mListener;
    private BudgetType type;

    //Dont use this to instantiate, only created for subclass purposes
    public CategoryGridRecyclerAdapter(){}

    public CategoryGridRecyclerAdapter(Fragment fragment, List<Category> list) {
        this.context = fragment.getContext();
        this.categoryList = list;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryGridAdapterInteractionListener interface
        if (fragment instanceof OnCategoryGridAdapterInteractionListener) {
            mListener = (OnCategoryGridAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCategoryGridAdapterInteractionListener.");
        }
    }

    public CategoryGridRecyclerAdapter(Activity activity, List<Category> list) {
        this.context = activity;
        this.categoryList = list;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryGridAdapterInteractionListener interface
        if (activity instanceof OnCategoryGridAdapterInteractionListener) {
            mListener = (OnCategoryGridAdapterInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnCategoryGridAdapterInteractionListener.");
        }
    }

    /**
     * Call this to add 1 last item to the list for users to click to add a category (Expense or Income)
     */
    public void addExpenseOrIncome(BudgetType type){
        Category category = new Category();
        category.setName("");
        category.setIcon("c_add");

        int color[] = new int[]{R.color.colorPrimary};

        category.setColor(context.getResources().getString(color[0]));
        category.setType(type.toString());
        category.setIndex(this.categoryList.size());

        this.type = type;

/*
        c.setId(Util.generateUUID());
        c.setName(tempCategoryIncomeNameList[i]);
        c.setColor(getResources().getString(tempCategoryIncomeColorList[i]));
        c.setIcon(getResources().getResourceEntryName(tempCategoryIncomeIconList[i]));
        c.setBudget(0);
        c.setType(BudgetType.INCOME.toString());
        c.setCost(0);
        c.setIndex(i);*/


        this.categoryList.add(category);
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circular_view, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting category data for the row
        final Category category = categoryList.get(position);

        viewHolder.circularView.setCircleColor(category.getColor());

        if(category.isText()){
            viewHolder.circularView.setText(""+Util.getFirstCharacterFromString(category.getName().toUpperCase().trim()));
            viewHolder.circularView.setIconResource(0);
        }else{
            viewHolder.circularView.setText("");
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, category.getIcon()));
        }

        // Name
        viewHolder.name.setText(category.getName());

        if(category.isSelected()){
            viewHolder.circularView.setStrokeColor(category.getColor());
        }else{
            viewHolder.circularView.setStrokeColor(R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return this.categoryList.size();
    }

    public void setCategoryList(List<Category> list){
        this.categoryList = list;
        notifyDataSetChanged();
    }

    public List<Category> getCategoryList(){
        return this.categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public TextView name;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            name = (TextView) itemView.findViewById(R.id.categoryName);

            circularView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getLayoutPosition() != (getItemCount() - 1)){
                        mListener.onClick(getLayoutPosition());
                    }else{
                        mListener.onClickAddNewCategory(type);
                    }
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCategoryGridAdapterInteractionListener {
        void onClick(int position);

        void onClickAddNewCategory(BudgetType type);
    }
}

