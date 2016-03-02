package com.zhan.budget.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.CategoryPercent;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Created by Zhan on 16-02-17.
 */
public class CategoryPercentListAdapter extends ArrayAdapter<CategoryPercent> {

    private Activity activity;
    private List<CategoryPercent> categoryList;

    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
        public TextView percent;
        public TextView cost;
    }

    public CategoryPercentListAdapter(Activity activity, List<CategoryPercent> categoryList) {
        super(activity, R.layout.item_category_percent_view, categoryList);
        this.activity = activity;
        this.categoryList = categoryList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // reuse views
        if (convertView == null) {
            // configure view holder
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_category_percent_view, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.categoryName);
            viewHolder.percent = (TextView) convertView.findViewById(R.id.categoryPercent);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.categoryCost);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting category data for the row
        CategoryPercent category = categoryList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(category.getCategory().getColor());
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(getContext(), category.getCategory().getIcon()));

        viewHolder.name.setText(category.getCategory().getName());
       // viewHolder.percent.setText(String.format("%.2f", category.getPercent())+"%");
        viewHolder.percent.setText(category.getPercent()+"%");

        //Only EXPENSE Category type would be displayed using this layout, so no need to check if
        //its budget type.
        viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(category.getCategory().getCost(), Constants.BUDGET_LOCALE));

        return convertView;
    }
}
