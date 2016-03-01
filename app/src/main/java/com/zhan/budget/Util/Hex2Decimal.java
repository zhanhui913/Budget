package com.zhan.budget.Util;

/**
 * Created by Zhan on 16-02-29.
 */
public final class Hex2Decimal {

    private Hex2Decimal(){}

    private static final String HEX = "0123456789ABCDEF";

    public static int hex2decimal(String s) {
        s = s.replace("#", "");

        //String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = HEX.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }


    // precondition:  d is a nonnegative integer
    public static String decimal2hex(int d) {
        //String digits = "0123456789ABCDEF";
        if (d == 0) return "0";
        String hex = "";
        while (d > 0) {
            int digit = d % 16;                // rightmost digit
            hex = HEX.charAt(digit) + hex;  // string concatenation
            d = d / 16;
        }
        return hex;
    }

}
