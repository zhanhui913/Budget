package com.zhan.budget.Adapter;

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
import android.widget.Toast;

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
public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder>
        implements ItemTouchHelperAdapter{

    private Fragment fragment;
    private List<Category> categoryList;
    private OnCategoryAdapterInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;
    private boolean showDate;

    public CategoryRecyclerAdapter(Fragment fragment, List<Category> list, boolean showDate, OnStartDragListener startDragListener) {
        this.fragment = fragment;
        this.categoryList = list;
        this.showDate = showDate;
        this.mDragStartListener = startDragListener;

        //Any activity or fragment that uses this adapter needs to implement the OnTransactionAdapterInteractionListener interface
        if (fragment instanceof OnCategoryAdapterInteractionListener) {
            mListener = (OnCategoryAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.getContext().toString() + " must implement OnCategoryAdapterInteractionListener.");
        }
    }

    public void setData(List<Category> list){
        this.categoryList = list;
        notifyDataSetChanged();
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
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        // getting category data for the row
        Category category = categoryList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(category.getColor());
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(this.fragment.getContext(), category.getIcon()));

        viewHolder.name.setText(category.getName());
        viewHolder.budget.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())) {
            viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(category.getCost(), Constants.BUDGET_LOCALE));

            //ProgressBar
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setMax(category.getBudget());
            viewHolder.progressBar.setProgress(Math.abs(category.getCost()));
        }else{
            viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(Math.abs(category.getCost()), Constants.BUDGET_LOCALE));

            viewHolder.progressBar.setVisibility(View.GONE);
        }


        if(category.getBudget() == Math.abs(category.getCost())){ //If its exactly the same
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(this.fragment.getContext(), R.color.colorPrimary));
        }else if(category.getBudget() > Math.abs(category.getCost())){ //If its less than budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(this.fragment.getContext(), R.color.sunflower));
        }else{ //If exceeded budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(this.fragment.getContext(), R.color.red));
        }

        viewHolder.swipeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDragStartListener.onStartDrag(viewHolder);
                Log.d("RECYCLER_DEBUG", "start drag");

                return true;
            }
        });

        viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEditCategory(position);
                Toast.makeText(fragment.getContext(), "onEdit "+position, Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteCategory(position);
                Toast.makeText(fragment.getContext(), "onDelete "+position, Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.swipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(fragment.getContext(), "onClick "+position, Toast.LENGTH_SHORT).show();
                mListener.onClick(position);
            }
        });
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(categoryList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        Log.d("RECYCLER_DEBUG", "moved from " + fromPosition + " to " + toPosition);
        return true;
    }

    @Override
    public void onItemEndDrag(){
        Log.d("RECYCLER_DEBUG", "end drag");
        mListener.onDisablePtrPullDown(false);
        mListener.onDoneDrag();
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return this.categoryList.size();
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
        public TextView name;
        public TextView budget;
        public TextView cost;
        public RoundCornerProgressBar progressBar;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn;
        public ImageView editBtn;

        private Drawable defaultDrawable;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            name = (TextView) itemView.findViewById(R.id.categoryName);
            budget = (TextView) itemView.findViewById(R.id.categoryBudget);
            cost = (TextView) itemView.findViewById(R.id.categoryCost);
            progressBar = (RoundCornerProgressBar) itemView.findViewById(R.id.categoryProgress);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCategory);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);
            editBtn = (ImageView) itemView.findViewById(R.id.editBtn);

            defaultDrawable = itemView.getBackground();
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Colors.getColorFromAttr(itemView.getContext(), R.attr.themeColorHighlight));
        }

        @Override
        public void onItemClear() {
            itemView.setBackground(defaultDrawable);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCategoryAdapterInteractionListener {
        void onDeleteCategory(int position);

        void onEditCategory(int position);

        void onDisablePtrPullDown(boolean value);

        void onDoneDrag();

        void onClick(int position);
    }
}

