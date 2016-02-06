package com.zhan.budget.Adapter;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2016-01-08.
 */
public class CategoryListAdapter extends ArrayAdapter<Category> {

    private Activity activity;
    private List<Category> categoryList;

    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
        public TextView budget;
        public TextView cost;
        public RoundCornerProgressBar progressBar;
    }

    public CategoryListAdapter(Activity activity, List<Category> categoryList) {
        super(activity, R.layout.item_category, categoryList);
        this.activity = activity;
        this.categoryList = categoryList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting category data for the row
        Category category = categoryList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(category.getColor());
        viewHolder.circularView.setIconDrawable(ResourcesCompat.getDrawable(activity.getResources(), category.getIcon(), activity.getTheme()));

        viewHolder.name.setText(category.getName());
        viewHolder.budget.setText("$" + Util.setPriceToCorrectDecimalInString(category.getBudget()));
        viewHolder.cost.setText(Util.setPriceToCorrectDecimalInString(category.getCost()));

        //ProgressBar
        viewHolder.progressBar.setMax(category.getBudget());
        viewHolder.progressBar.setProgress(Math.abs(category.getCost()));

        if(category.getBudget() == Math.abs(category.getCost())){ //If its exactly the same
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        }else if(category.getBudget() > Math.abs(category.getCost())){ //If its less than budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.green));
        }else{ //If exceeded budget
            viewHolder.progressBar.setProgressColor(ContextCompat.getColor(activity, R.color.red));
        }

        return convertView;
    }
}
