package com.zhan.budget.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zhan.budget.R;
import com.zhan.circularview.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2016-02-04.
 */
public class ColorCategoryGridAdapter extends ArrayAdapter<Integer> {
    private Context context;
    private List<Integer> colorList;

    static class ViewHolder {
        public CircularView circularView;
    }

    public ColorCategoryGridAdapter(Context context, List<Integer> colorList) {
        super(context, R.layout.item_category_grid, colorList);
        this.context = context;
        this.colorList = colorList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_icon_picker_category, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get color data
        viewHolder.circularView.setCircleColor(colorList.get(position));

        return convertView;
    }
}
