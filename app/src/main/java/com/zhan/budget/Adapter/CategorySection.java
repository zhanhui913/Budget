package com.zhan.budget.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Adapter.Helper.ItemTouchHelperViewHolder;
import com.zhan.budget.Adapter.Helper.OnStartDragListener;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by zhanyap on 2017-04-03.
 */

public class CategorySection extends StatelessSection {

    public enum ARRANGEMENT{
        BUDGET,
        MOVE,
        PERCENT
    }

    public ARRANGEMENT arrangement;

    private Context context;
    private List<Category> categoryList;
    private CategoryGenericRecyclerAdapter.OnCategoryGenericAdapterInteractionListener mListener;
    private OnStartDragListener mDragStartListener;


    String title;

    public CategorySection(String title, Fragment fragment, ARRANGEMENT arrangement, List<Category> list) {
        super(R.layout.header_list, R.layout.item_category);

        this.title = title;
        this.context = fragment.getContext();
        this.arrangement = arrangement;
        this.categoryList = list;
    }

    public void setCategoryList(List<Category> list){
        this.categoryList = list;
    }

    public List<Category> getCategoryList(){
        return this.categoryList;
    }

    @Override
    public int getContentItemsTotal() {
        return categoryList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        // getting category data for the row
        final Category category = categoryList.get(position);

        //CircularView
        itemHolder.circularView.setCircleColor(category.getColor());
        itemHolder.circularView.setStrokeColor(category.getColor());

        if(category.isText()){
            itemHolder.circularView.setText(""+ Util.getFirstCharacterFromString(category.getName().toUpperCase().trim()));
            itemHolder.circularView.setIconResource(0);
        }else{
            itemHolder.circularView.setText("");
            itemHolder.circularView.setIconResource(CategoryUtil.getIconID(context, category.getIcon()));
        }

        itemHolder.name.setText(category.getName());

        if(arrangement == ARRANGEMENT.BUDGET || arrangement == ARRANGEMENT.MOVE){
            itemHolder.budget.setText(String.format(context.getString(R.string.category_budget), CurrencyTextFormatter.formatDouble(category.getBudget())));
        }else{
            itemHolder.budget.setText(String.format(context.getString(R.string.category_percent), category.getPercent()));
        }

        if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())) {
            if(arrangement == ARRANGEMENT.BUDGET) {
                itemHolder.cost.setText(CurrencyTextFormatter.formatDouble(category.getCost()));

                itemHolder.dragIcon.setVisibility(View.INVISIBLE);

                //ProgressBar
                if(category.getBudget() > 0){
                    itemHolder.progressBar.setVisibility(View.VISIBLE);
                    itemHolder.progressBar.setMax((float)category.getBudget());
                    itemHolder.progressBar.setProgress((float)Math.abs(category.getCost()));
                    itemHolder.progressBar.setProgressColor(Color.parseColor(category.getColor()));
                }else{
                    //If there is no budget even though its an expense
                    itemHolder.progressBar.setVisibility(View.GONE);
                }
            }else if(arrangement == ARRANGEMENT.MOVE){
                itemHolder.cost.setVisibility(View.GONE);
                itemHolder.costTitle.setVisibility(View.GONE);
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.dragIcon.setVisibility(View.VISIBLE);
            }else if(arrangement == ARRANGEMENT.PERCENT){
                itemHolder.dragIcon.setVisibility(View.GONE);
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.costTitle.setVisibility(View.GONE);
                itemHolder.cost.setText(CurrencyTextFormatter.formatDouble(category.getCost()));
                itemHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        } else if(category.getType().equalsIgnoreCase(BudgetType.INCOME.toString())) {
            itemHolder.budget.setVisibility(View.GONE);

            if(arrangement == ARRANGEMENT.BUDGET){
                itemHolder.cost.setText(CurrencyTextFormatter.formatDouble(Math.abs(category.getCost())));
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.dragIcon.setVisibility(View.INVISIBLE);
            }else if(arrangement == ARRANGEMENT.MOVE){
                itemHolder.cost.setVisibility(View.GONE);
                itemHolder.costTitle.setVisibility(View.GONE);
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.dragIcon.setVisibility(View.VISIBLE);
            }else if(arrangement == ARRANGEMENT.PERCENT){
                itemHolder.dragIcon.setVisibility(View.GONE);
                itemHolder.progressBar.setVisibility(View.GONE);
                itemHolder.costTitle.setVisibility(View.GONE);
                itemHolder.cost.setText(CurrencyTextFormatter.formatDouble(category.getCost()));
                itemHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.green));
            }
        }

        itemHolder.dragIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.onPullDownAllow(false);
                mDragStartListener.onStartDrag(holder);
                return false;
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.headerTitle.setText(title);
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView headerTitle;

        public HeaderViewHolder(View view) {
            super(view);

            headerTitle = (TextView) view.findViewById(R.id.headerText);
        }
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     * Provide a direct reference to each of the views within a data item.
     * Used to cache the views within the item layout for fast access
     */
    class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public ImageView dragIcon;
        public TextView name, budget, cost, costTitle;
        public RoundCornerProgressBar progressBar;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, editBtn;

        private Drawable defaultDrawable;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ItemViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            dragIcon = (ImageView) itemView.findViewById(R.id.dragIcon);
            name = (TextView) itemView.findViewById(R.id.categoryName);
            budget = (TextView) itemView.findViewById(R.id.categoryBudget);
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
}