package com.zhan.budget.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2016-02-04.
 */
public class IconCategoryGridAdapter extends ArrayAdapter<CategoryIconColor> {
    private List<CategoryIconColor> iconList;
    private String color;

    static class ViewHolder {
        public CircularView circularView;
    }

    public IconCategoryGridAdapter(Context context, List<CategoryIconColor> iconList, String color) {
        super(context, R.layout.item_circular_view, iconList);
        this.iconList = iconList;
        this.color = color;
    }

    public void updateColor(String color){
        this.color = color;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_circular_view, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            convertView.findViewById(R.id.categoryName).setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get drawable data
        viewHolder.circularView.setCircleColor(this.color);
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(getContext(), iconList.get(position).getIcon()));
        viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColor));

        if(iconList.get(position).isSelected()){
            viewHolder.circularView.setStrokeColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColorText));
        }else{
            viewHolder.circularView.setStrokeColor(R.color.transparent);
        }

        return convertView;
    }
}
