package com.zhan.budget.Adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

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

        View icon = convertView.findViewById(R.id.categoryIcon);
        TextView name = (TextView) convertView.findViewById(R.id.categoryName);

        // getting category data for the row
        Category category = categoryList.get(position);

        //Get Drawable from @drawable/circular_category
        LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(this.context, R.drawable.circular_category);

        // If we don't mutate the drawable, then all drawable's with this id will have a color
        // filter applied to it.
        drawable.mutate();

        //Color
        GradientDrawable shape = (GradientDrawable)drawable.findDrawableByLayerId(R.id.layerColorId);
        shape.setColor(Color.parseColor(category.getColor()));

        //Icon
        Drawable iconDrawable = CategoryUtil.getIconDrawable(this.context, category.getIcon());
        drawable.setDrawableByLayerId(R.id.layerIconId, iconDrawable);

        icon.setBackground(drawable);


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
