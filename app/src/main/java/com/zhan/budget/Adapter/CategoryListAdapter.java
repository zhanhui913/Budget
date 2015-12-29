package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Category;
import com.zhan.budget.R;

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
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.category_item, null);


        TextView name = (TextView) convertView.findViewById(R.id.categoryName);

        // getting category data for the row
        Category category = categoryList.get(position);

        // Name
        name.setText(category.getName());

        return convertView;
    }
}
