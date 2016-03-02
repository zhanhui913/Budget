package com.zhan.budget.Adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2016-01-08.
 */
public class CategoryListAdapter extends ArrayAdapter<Category> {

    private OnCategoryAdapterInteractionListener mListener;
    private Activity activity;
    private List<Category> categoryList;

    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
        public TextView budget;
        public TextView cost;
        public RoundCornerProgressBar progressBar;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn;
        public ImageView editBtn;
    }

    public CategoryListAdapter(Activity activity, List<Category> categoryList) {
        super(activity, R.layout.item_category, categoryList);
        this.activity = activity;
        this.categoryList = categoryList;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryAdapterInteractionListener interface
        if(activity instanceof OnCategoryAdapterInteractionListener){
            mListener = (OnCategoryAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnCategoryAdapterInteractionListener.");
        }
    }

    public CategoryListAdapter(Fragment fragment,  List<Category> categoryList) {
        super(fragment.getActivity(), R.layout.item_category, categoryList);
        this.activity = fragment.getActivity();
        this.categoryList = categoryList;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryAdapterInteractionListener interface
        if (fragment instanceof OnCategoryAdapterInteractionListener) {
            mListener = (OnCategoryAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnCategoryAdapterInteractionListener.");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // reuse views
        if (convertView == null) {
            // configure view holder
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_category, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.categoryName);
            viewHolder.budget = (TextView) convertView.findViewById(R.id.categoryBudget);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.categoryCost);
            viewHolder.progressBar = (RoundCornerProgressBar) convertView.findViewById(R.id.categoryProgress);

            viewHolder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipeCategory);
            viewHolder.deleteBtn = (ImageView) convertView.findViewById(R.id.deleteBtn);
            viewHolder.editBtn = (ImageView) convertView.findViewById(R.id.editBtn);

            viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onstartopen");
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "on open");
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onstartclose");
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onclose");
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onupdate "+leftOffset+","+topOffset);
                    mListener.onDisablePtrPullDown(true);
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onhandrelease :"+xvel+","+yvel);
                    mListener.onDisablePtrPullDown(false);
                }
            });

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "delete category at :"+position, Toast.LENGTH_SHORT).show();
                mListener.onDeleteCategory(position);
            }
        });

        viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "edit", Toast.LENGTH_SHORT).show();
                mListener.onEditCategory(position);
            }
        });

        // getting category data for the row
        Category category = categoryList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(category.getColor());
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(getContext(), category.getIcon()));

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
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        }else if(category.getBudget() > Math.abs(category.getCost())){ //If its less than budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.sunflower));
        }else{ //If exceeded budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.red));
        }

        return convertView;
    }

    public interface OnCategoryAdapterInteractionListener {
        void onDeleteCategory(int position);

        void onEditCategory(int position);

        void onDisablePtrPullDown(boolean value);
    }
}
