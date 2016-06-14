package com.zhan.budget.Util;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

import com.zhan.budget.Model.Realm.Category;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Colors {

    private static final String HEX_PATTERN = "^#([A-Fa-f0-9])$";

    private Colors() {
    }

    public static long hex2Long(String s){
        return Long.parseLong(s, 16);
    }

    public static String long2Hex(long value){
        return Long.toHexString(value).toUpperCase();
    }

    public static String validateHex(String hex) throws Exception{
        Pattern pattern = Pattern.compile(HEX_PATTERN);
        Matcher matcher = pattern.matcher(hex);

        if(matcher.matches()){
            return String.format("#%08X", hex2Long(hex.replace("#","")));
        }
        throw new Exception(hex+" contains values that shouldnt be in hexadecimal.");
    }

    public static String getHexColorFromAttr(Context context, @AttrRes int attr){
        /*int[] attrs = new int[] { attr};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int cc = ((ColorDrawable)ta.getDrawable(0)).getColor();
        ta.recycle();
        return "#"+Integer.toHexString(cc);
        */

        //2nd way
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int color = typedValue.data;
        return "#"+Integer.toHexString(color);
    }

    public static int getColorFromAttr(Context context, @AttrRes int attr){
        int[] attrs = new int[] { attr};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int cc = ((ColorDrawable)ta.getDrawable(0)).getColor();
        ta.recycle();
        return cc;
    }

    public static int getRandomColor(Context context){
        String randomColorString = getRandomColorString(context);

        try {
            return CategoryUtil.getColorID(context, randomColorString);
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static String getRandomColorString(Context context){
        List<Category> catColor = CategoryUtil.getListOfCategoryColors(context);

        return catColor.get(new Random().nextInt(catColor.size())).getColor();
    }
}
