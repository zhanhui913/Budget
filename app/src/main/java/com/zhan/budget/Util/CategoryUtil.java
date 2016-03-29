package com.zhan.budget.Util;

import android.content.Context;
import android.content.res.TypedArray;

import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor

    public static List<CategoryIconColor> getListOfUniqueIcons(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);

        List<CategoryIconColor> iconList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            //Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s)+" -> "+context.getResources().getString(s));

            CategoryIconColor cc = new CategoryIconColor();
            cc.setIcon(context.getResources().getResourceEntryName(s));
            cc.setIsSelected(false); //default

            iconList.add(cc);
        }
        icons.recycle();
        return iconList;
    }

    public static List<CategoryIconColor> getListOfCategoryColors(Context context){
        TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);

        List<CategoryIconColor> colorList = new ArrayList<>();
        for(int i = 0; i < colors.length(); i++){
            // get resource ID by index
            int s = colors.getResourceId(i, 0);
            //Log.d("CATEGORY_UTIL", i + "->" + s + ", name :" + context.getResources().getResourceName(s) + ", entry name:" + context.getResources().getResourceEntryName(s) + " -> " + context.getResources().getString(s));

            CategoryIconColor cc = new CategoryIconColor();
            cc.setColor(context.getResources().getString(s));
            cc.setIsSelected(false); //default
            colorList.add(cc);
        }
        colors.recycle();
        return colorList;
    }

    public static int getIconID(Context context, String value){
        return context.getResources().getIdentifier(value, "drawable", context.getPackageName());
    }

    public static int getColorID(Context context, String value) throws Exception{
        List<CategoryIconColor> colorList = getListOfCategoryColors(context);

        int position = -1;

        for(int i = 0; i < colorList.size(); i++){
            if(colorList.get(i).getColor().equalsIgnoreCase(value)){
                position = i;
            }
        }

        if(position != -1){
            TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);
            // get resource ID by index
            return colors.getResourceId(position, 0);
        }
        throw new Exception("Cannot find color value in list of colors for category in array.xml");
    }

    /**
     * Get first color in R.array.category_colors
     * @param context
     * @return name of first color in array
     */
    public static String getDefaultCategoryColor(Context context){
        TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);
        return context.getResources().getString(colors.getResourceId(0, 0));
    }

    /**
     * Get the first icon in R.array.category_icons
     * @param context
     * @return name of first icon in array
     */
    public static String getDefaultCategoryIcon(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);
        return context.getResources().getResourceEntryName(icons.getResourceId(0, 0));
    }

}
