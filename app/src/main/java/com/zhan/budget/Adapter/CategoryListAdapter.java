package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import java.util.List;

/**
 * Created by Zhan on 15-12-28.
 */
public class CategoryListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Category> categoryList;

    public CategoryListAdapter(Activity activity, List<Category> categoryList) {
        this.activity = activity;
        this.categoryList = categoryList;
    }

    @Override
    public int getCount() {
        return this.categoryList.size();
    }

    @Override
    public Object getItem(int location) {
        return categoryList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_category, null);



        TextView name = (TextView) convertView.findViewById(R.id.categoryName);
        TextView budget = (TextView) convertView.findViewById(R.id.categoryBudget);
        TextView cost = (TextView) convertView.findViewById(R.id.categoryCost);
        RoundCornerProgressBar progressBar = (RoundCornerProgressBar) convertView.findViewById(R.id.categoryProgress);

        // getting category data for the row
        Category category = categoryList.get(position);


        /*
        View icon = convertView.findViewById(R.id.categoryIcon);

        //Get Drawable from @drawable/circular_category
        LayerDrawable drawable = (LayerDrawable) ContextCompat.getIcon(activity.getApplicationContext(), R.drawable.circular_category);

        // If we don't mutate the drawable, then all drawable's with this id will have a color
        // filter applied to it.
        drawable.mutate();

        //Color
        GradientDrawable shape = (GradientDrawable)drawable.findDrawableByLayerId(R.id.layerColorId);
        shape.setCircleColor(Color.parseColor(category.getColor()));

        //Icon
        Drawable iconDrawable = CategoryUtil.getIconDrawable(activity, category.getIcon());
        drawable.setDrawableByLayerId(R.id.layerIconId, iconDrawable);

        icon.setBackground(drawable);
*/

        CircularView circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
        circularView.setCircleColor(Color.parseColor(category.getColor()));
        circularView.setIconDrawable(ResourcesCompat.getDrawable(activity.getResources(), CategoryUtil.getIconResourceId(category.getIcon()), activity.getTheme()));



        Log.d("CANVAS", "Category name:"+category.getName()+", color:"+category.getColor()+", icon:"+category.getIcon());

        // Name
        name.setText(category.getName());

        //Budget
        budget.setText("$"+ Util.setPriceToCorrectDecimalInString(category.getBudget()));

        //Cost
        cost.setText("$"+Util.setPriceToCorrectDecimalInString(category.getCost()));

        //ProgressBar
        progressBar.setMax(category.getBudget());
        progressBar.setProgress(category.getCost());

        return convertView;
    }

    public void refreshList(List<Category> categoryList) {
        this.categoryList = categoryList;
        Log.d("CANVAS","----------------------- "+this.categoryList.size());
        notifyDataSetChanged();
    }
}
