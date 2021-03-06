package com.zhan.budget.Util;

import android.content.Context;
import android.content.res.TypedArray;

import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor

    public static List<Category> getListOfUniqueIcons(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);

        List<Category> iconList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            //Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s)+" -> "+context.getResources().getString(s));

            Category cc = new Category();
            cc.setIcon(context.getResources().getResourceEntryName(s));
            cc.setSelected(false); //default

            iconList.add(cc);
        }
        icons.recycle();
        return iconList;
    }

    public static List<Category> getListOfCategoryColors(Context context){
        TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);

        List<Category> colorList = new ArrayList<>();
        for(int i = 0; i < colors.length(); i++){
            // get resource ID by index
            int s = colors.getResourceId(i, 0);
            //Log.d("CATEGORY_UTIL", i + "->" + s + ", name :" + context.getResources().getResourceName(s) + ", entry name:" + context.getResources().getResourceEntryName(s) + " -> " + context.getResources().getString(s));

            Category cc = new Category();
            cc.setColor(context.getResources().getString(s));
            cc.setSelected(false); //default
            colorList.add(cc);
        }
        colors.recycle();
        return colorList;
    }

    public static int getIconID(Context context, String value){
        return context.getResources().getIdentifier(value, "drawable", context.getPackageName());
    }

    public static int getColorID(Context context, String value) throws Exception{
        List<Category> colorList = getListOfCategoryColors(context);

        int position = -1;

        for(int i = 0; i < colorList.size(); i++){
            if(colorList.get(i).getColor().equalsIgnoreCase(value)){
                position = i;
            }
        }

        if(position != -1){
            TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);
            //colors.recycle();
            return colors.getResourceId(position, 0);
            // get resource ID by index

           /* int ss = colors.getResourceId(position, 0);
            colors.recycle();
            return ss;*/
        }
        throw new Exception("Cannot find color value in list of colors for category in array.xml");
    }

    /**
     * Get first color in R.array.category_colors
     * @param context The content
     * @return name of first color in array
     */
    public static String getDefaultCategoryColor(Context context){
        TypedArray colors = context.getResources().obtainTypedArray(R.array.category_colors);
        //colors.recycle();
        return context.getResources().getString(colors.getResourceId(0, 0));

        /*
        String ss = context.getResources().getString(colors.getResourceId(0, 0));
        colors.recycle();
        return ss;*/
    }

    /**
     * Get the first icon in R.array.category_icons
     * @param context The content
     * @return name of first icon in array
     */
    public static String getDefaultCategoryIcon(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);
        //icons.recycle();
        return context.getResources().getResourceEntryName(icons.getResourceId(0, 0));

        /*
        String ss = context.getResources().getResourceEntryName(icons.getResourceId(0, 0));
        icons.recycle();
        return ss;*/
    }

}
