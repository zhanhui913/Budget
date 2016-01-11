package com.zhan.budget.Adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.circularview.CircularView;

import java.util.List;


/**
 * Created by zhanyap on 2016-01-07.
 */
public class CategoryGridAdapter extends ArrayAdapter<Category> {

    private Context context;
    private List<Category> categoryList;

    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
    }

    public CategoryGridAdapter(Context context, List<Category> categoryList) {
        super(context, R.layout.item_category_grid, categoryList);
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_category_grid, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.categoryName);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get category data
        Category category = categoryList.get(position);

        viewHolder.circularView.setCircleColor(Color.parseColor(category.getColor()));
        viewHolder.circularView.setIconDrawable(ResourcesCompat.getDrawable(this.context.getResources(), CategoryUtil.getIconResourceId(category.getIcon()), this.context.getTheme()));

        // Name
        viewHolder.name.setText(category.getName());

        return convertView;
    }
}
