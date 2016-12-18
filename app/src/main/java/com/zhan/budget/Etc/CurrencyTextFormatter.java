package com.zhan.budget.Etc;

import android.util.Log;

import com.zhan.budget.Model.Realm.BudgetCurrency;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Zhan on 16-02-11.
 */
public final class CurrencyTextFormatter {

    //Setting a max length because after this length, java represents doubles in scientific notation which breaks the formatter
    public static final int MAX_RAW_INPUT_LENGTH = 15;

    private CurrencyTextFormatter(){}

    public static String formatText(String val, BudgetCurrency currency){

        //special case for the start of a negative number
        //if(val.equals("-")) return val;

        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00

        final double CURRENCY_DECIMAL_DIVISOR = (int) Math.pow(10, Currency.getInstance(Locale.CANADA).getDefaultFractionDigits());
        DecimalFormat currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.CANADA);

        //if there's nothing left, that means we were handed an empty string. Also, cap the raw input so the formatter doesn't break.
        if(!val.equals("") && val.length() < MAX_RAW_INPUT_LENGTH && !val.equals("-")) {
            //Convert the string into a double, which will later be passed into the currency formatter
            double newTextValue = Double.valueOf(val);

            /** Despite having a formatter, we actually need to place the decimal ourselves.
             * IMPORTANT: This double division does have a small potential to introduce rounding errors (though the likelihood is very small for two digits)
             * Therefore, do not attempt to pull the numerical value out of the String text of this object. Instead, call getRawValue to retrieve
             * the actual number input by the user. See CurrencyEditText.getRawValue() for more information.
             */
            newTextValue = newTextValue / CURRENCY_DECIMAL_DIVISOR;
            val = currencyFormatter.format(newTextValue);
        }else if(val.equals("") || val.equals("-")){
            val = currencyFormatter.format(0);
        }
        /*else {
            throw new IllegalArgumentException("Invalid argument in val");
        }*/

        if(currency != null){
            val = val.replace("$","").replace("(","-").replace(")","") + currency.getCurrencyCode();
            return val;
        }else{
            Log.e("CurrencyTextFormatter", "Currency is null");
            return "Currency is null";
        }
    }

    public static float formatCurrency(String val){
        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00
        final float CURRENCY_DECIMAL_DIVISOR = (int) Math.pow(10, Currency.getInstance(Locale.CANADA).getDefaultFractionDigits());

        float newTextValue;

        if(val.equals("") && !val.equals("-")){
            newTextValue = 0;
        }else if(val.charAt(0) == '('){
            val = val.replace("(", "-").replace(")","");
            newTextValue = Float.valueOf(val);
        }else{
            newTextValue = Float.valueOf(val);
        }

        newTextValue = newTextValue / CURRENCY_DECIMAL_DIVISOR;

        return newTextValue;
    }

    public static String formatFloat(float val, BudgetCurrency currency){
        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00
        DecimalFormat currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.CANADA);
        if(currency != null){
            return currencyFormatter.format(val).replace("$","").replace("(","-").replace(")","") + currency.getCurrencyCode();
        }else {
            Log.e("CurrencyTextFormatter", "Currency is null");
            return "Currency is null";
        }
    }

    public static String stripCharacters(String value, BudgetCurrency currency){
        if(currency != null) {
            return value.replace("$", "").replace("-", "").replace("+", "").replace(".", "").replace(",", "").replace("(", "").replace(")", "").replace(currency.getCurrencyCode(), "");
        }else{
            Log.e("CurrencyTextFormatter", "Currency is null");
            return "Currency is null";
        }
    }

    /**
     * Converts amount of provided currency to defaults currency
     * @param amount Amount
     * @param currency The source of the currency
     * @return The amount after converting to default currency
     */
    public static float convertCurrency(float amount, BudgetCurrency currency){
        return (float)(1 / currency.getRate()) * amount;
    }
}
