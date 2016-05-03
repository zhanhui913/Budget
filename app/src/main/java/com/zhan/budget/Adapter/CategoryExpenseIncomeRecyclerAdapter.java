package com.zhan.budget.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Adapter.Helper.ItemTouchHelperAdapter;
import com.zhan.budget.Adapter.Helper.ItemTouchHelperViewHolder;
import com.zhan.budget.Adapter.Helper.OnStartDragListener;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.library.CircularView;

import java.util.Collections;
import java.util.List;

/**
 * Simple CategoryRecyclerAdapter that implements {@link ItemTouchHelperAdapter} to respond to move
 * from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class CategoryExpenseIncomeRecyclerAdapter extends RecyclerView.Adapter<CategoryExpenseIncomeRecyclerAdapter.ViewHolder>
        implements ItemTouchHelperAdapter{

    private Context context;
    private List<Category> categoryList;
    private boolean displayBudget;
    private static OnCategoryExpenseIncomeAdapterInteractionListener mListener;
    private static OnStartDragListener mDragStartListener;

    public CategoryExpenseIncomeRecyclerAdapter(Fragment fragment, List<Category> list, boolean displayBudget, OnStartDragListener startDragListener) {
        this.context = fragment.getContext();
        this.categoryList = list;
        this.displayBudget = displayBudget;
        mDragStartListener = startDragListener;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryExpenseAdapterInteractionListener interface
        if (fragment instanceof OnCategoryExpenseIncomeAdapterInteractionListener) {
            mListener = (OnCategoryExpenseIncomeAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCategoryExpenseAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting category data for the row
        final Category category = categoryList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(category.getColor());
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, category.getIcon()));

        viewHolder.name.setText(category.getName());
        viewHolder.budget.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())) {
            if(displayBudget) {
                viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(category.getCost(), Constants.BUDGET_LOCALE));

                viewHolder.dragIcon.setVisibility(View.INVISIBLE);

                //ProgressBar
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setMax(category.getBudget());
                viewHolder.progressBar.setProgress(Math.abs(category.getCost()));

                if(category.getBudget() == Math.abs(category.getCost())){ //If its exactly the same
                    viewHolder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }else if(category.getBudget() > Math.abs(category.getCost())){ //If its less than budget
                    viewHolder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.sunflower));
                }else{ //If exceeded budget
                    viewHolder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.red));
                }
            }else{
                viewHolder.cost.setVisibility(View.GONE);
                viewHolder.costTitle.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.dragIcon.setVisibility(View.VISIBLE);
            }
        } else if(category.getType().equalsIgnoreCase(BudgetType.INCOME.toString())) {
            viewHolder.budgetTitle.setVisibility(View.GONE);
            viewHolder.budget.setVisibility(View.GONE);
            viewHolder.costTitle.setVisibility(View.GONE);

            if(displayBudget){
                viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(Math.abs(category.getCost()), Constants.BUDGET_LOCALE));
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.dragIcon.setVisibility(View.INVISIBLE);
            }else{
                viewHolder.cost.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.dragIcon.setVisibility(View.VISIBLE);
            }
        }
        /*
        viewHolder.swipeLayout.getSurfaceView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onPullDownAllow(false);
                mDragStartListener.onStartDrag(viewHolder);
                return false;
            }
        });
        */

        viewHolder.dragIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onPullDownAllow(false);
                mDragStartListener.onStartDrag(viewHolder);
                return false;
            }
        });
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Log.d("RECYCLER_DEBUG", "old indices -----------");
        for(int i = 0; i < categoryList.size(); i++){
            String name = categoryList.get(i).getName();
            int index = categoryList.get(i).getIndex();
            Log.d("RECYCLER_DEBUG", index+"->"+name);
        }
        Log.d("RECYCLER_DEBUG", "old indices -----------");

        Collections.swap(categoryList, fromPosition, toPosition);

        Log.d("RECYCLER_DEBUG", "new suppose indices -----------");
        for(int i = 0; i < categoryList.size(); i++){
            String name = categoryList.get(i).getName();
            Log.d("RECYCLER_DEBUG", i+"->"+name);
        }
        Log.d("RECYCLER_DEBUG", "new suppose indices -----------");

        notifyItemMoved(fromPosition, toPosition);
        Log.d("ZHAP", "1 moved from " + fromPosition + " to " + toPosition);
        return true;
    }

    @Override
    public void onItemEndDrag(){
        Log.d("RECYCLER_DEBUG", "end drag");
        mListener.onDoneDrag();
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

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     * Provide a direct reference to each of the views within a data item.
     * Used to cache the views within the item layout for fast access
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public ImageView dragIcon;
        public TextView name;
        public TextView budget;
        public TextView budgetTitle;
        public TextView cost;
        public TextView costTitle;
        public RoundCornerProgressBar progressBar;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn;
        public ImageView editBtn;

        private Drawable defaultDrawable;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            dragIcon = (ImageView) itemView.findViewById(R.id.dragIcon);
            name = (TextView) itemView.findViewById(R.id.categoryName);
            budget = (TextView) itemView.findViewById(R.id.categoryBudget);
            budgetTitle = (TextView) itemView.findViewById(R.id.categoryBudgetTitle);
            cost = (TextView) itemView.findViewById(R.id.categoryCost);
            costTitle = (TextView) itemView.findViewById(R.id.categoryCostTitle);
            progressBar = (RoundCornerProgressBar) itemView.findViewById(R.id.categoryProgress);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCategory);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);
            editBtn = (ImageView) itemView.findViewById(R.id.editBtn);

            defaultDrawable = itemView.getBackground();

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getLayoutPosition());
                    Log.d("ZHAP", "on click : " + getLayoutPosition());
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ZHAP", "editting category : " + getLayoutPosition());
                    mListener.onEditCategory(getLayoutPosition());
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteCategory(getLayoutPosition());
                }
            });
        }

        @Override
        public void onItemSelected() {
            swipeLayout.getSurfaceView().setBackgroundColor(Colors.getColorFromAttr(itemView.getContext(), R.attr.themeColorHighlight));
        }

        @Override
        public void onItemClear() {
            swipeLayout.getSurfaceView().setBackground(defaultDrawable);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCategoryExpenseIncomeAdapterInteractionListener {
        void onDeleteCategory(int position);

        void onEditCategory(int position);

        void onPullDownAllow(boolean value);

        void onDoneDrag();

        void onClick(int position);
    }
}

