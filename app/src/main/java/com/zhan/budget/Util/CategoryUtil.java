package com.zhan.budget.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.zhan.budget.R;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor


    public static Drawable getIconDrawable(Context context, int iconId){
        if(iconId == 0){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 1){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 2){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 3){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 4){
            return ContextCompat.getDrawable(context, R.drawable.coffee);
        }else if(iconId == 5){
            return ContextCompat.getDrawable(context, R.drawable.house);
        }else if(iconId == 6){
            return ContextCompat.getDrawable(context, R.drawable.airplane);
        }else if(iconId == 7){
            return ContextCompat.getDrawable(context, R.drawable.shirt);
        }else if(iconId == 8){
            return ContextCompat.getDrawable(context, R.drawable.etc);
        }else if(iconId == 9){
            return ContextCompat.getDrawable(context, R.drawable.utilities);
        }else if(iconId == 10){
            return ContextCompat.getDrawable(context, R.drawable.bill);
        }else{
            return ContextCompat.getDrawable(context, R.drawable.bread);
            //return ContextCompat.getDrawable(context, R.color.cyan);
        }
    }

    public static Drawable getColorDrawable(Context context, String colorId){
        int colorInt = Color.parseColor(colorId);
/*
        Color color = Color.parseColor("#"+Integer.toHexString(colorInt));
        new Drawable();

        */
        return new ColorDrawable(colorInt);

    }
}
