package com.zhan.budget.Adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.circularview.CircularView;

import java.util.List;


/**
 * Created by zhanyap on 2016-01-07.
 */
public class CategoryGridAdapter extends BaseAdapter {

    private Context context;
    private List<Category> categoryList;

    public CategoryGridAdapter(Context context, List<Category> categoryList) {
        this.context = context;
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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_category_grid, null);


        TextView name = (TextView) convertView.findViewById(R.id.categoryName);
        final CircularView circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);

        // getting category data for the row
        Category category = categoryList.get(position);

        /*
        View icon = convertView.findViewById(R.id.categoryIcon);

        //Get Drawable from @drawable/circular_category
        LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(this.context, R.drawable.circular_category);

        // If we don't mutate the drawable, then all drawable's with this id will have a color
        // filter applied to it.
        drawable.mutate();

        //Color
        GradientDrawable shape = (GradientDrawable)drawable.findDrawableByLayerId(R.id.layerColorId);
        shape.setCircleColor(Color.parseColor(category.getColor()));

        //Icon
        Drawable iconDrawable = CategoryUtil.getIconDrawable(this.context, category.getIcon());
        drawable.setDrawableByLayerId(R.id.layerIconId, iconDrawable);

        icon.setBackground(drawable);
        */


        circularView.setCircleColor(Color.parseColor(category.getColor()));
        circularView.setIconDrawable(ResourcesCompat.getDrawable(this.context.getResources(), CategoryUtil.getIconResourceId(category.getIcon()), this.context.getTheme()));

        // Name
        name.setText(category.getName());

        return convertView;
    }

    public void refreshGrid(List<Category> categoryList){
        Log.d("CATEGORY", "refresh grid, new size = "+categoryList.size());
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }
}
