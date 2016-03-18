package com.zhan.budget.Util;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;

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
        int[] attrs = new int[] { attr};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int cc = ((ColorDrawable)ta.getDrawable(0)).getColor();
        ta.recycle();
        return "#"+Integer.toHexString(cc);
    }

    public static int getColorFromAttr(Context context, @AttrRes int attr){
        int[] attrs = new int[] { attr};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int cc = ((ColorDrawable)ta.getDrawable(0)).getColor();
        ta.recycle();
        return cc;
    }
}
