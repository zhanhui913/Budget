package com.zhan.budget.Etc;

import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by Zhan on 16-02-11.
 */
public final class CurrencyTextFormatter {

    //Setting a max length because after this length, java represents doubles in scientific notation which breaks the formatter
    public static final int MAX_RAW_INPUT_LENGTH = 15;

    private CurrencyTextFormatter(){}

    public static String formatText(String val){
        //special case for the start of a negative number
        //if(val.equals("-")) return val;
        boolean isNeg = false;
        if(val.length()>0){
            if(val.substring(0,1).equalsIgnoreCase("-")){
                val = val.replace("-","");
                isNeg=true;
            }
        }

        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00

        final double CURRENCY_DECIMAL_DIVISOR = (int) Math.pow(10, Currency.getInstance(Locale.CANADA).getDefaultFractionDigits());
        DecimalFormat currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.CANADA);

        //if there's nothing left, that means we were handed an empty string. Also, cap the raw input so the formatter doesn't break.
        if(!val.equals("") && !val.equals("-") && val.length() <= MAX_RAW_INPUT_LENGTH) {
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

        val = val.replace("$","").replace("(","-").replace(")","");
        if(isNeg){
            val = "-" + val;
        }

        return val;
    }

    public static double formatCurrency(String val){
        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00
        final double CURRENCY_DECIMAL_DIVISOR = (int) Math.pow(10, Currency.getInstance(Locale.CANADA).getDefaultFractionDigits());

        double newTextValue;

        if(val.equals("") && !val.equals("-")){
            newTextValue = 0;
        }else if(val.charAt(0) == '('){
            val = val.replace("(", "-").replace(")","");
            newTextValue = Double.valueOf(val);
        }else{
            newTextValue = Double.valueOf(val);
        }

        newTextValue = newTextValue / CURRENCY_DECIMAL_DIVISOR;

        return newTextValue;
    }

    public static String formatDouble(double val){
        //We're using Locale.CANADA so that the currency fraction digits is 2 zeroes at the end
        //ie: $x.00
        DecimalFormat currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.CANADA);
        return currencyFormatter.format(val).replace("$","").replace("(","-").replace(")","");
    }

    public static String stripCharacters(String value){
        return value.replace("$", "").replace("-", "").replace("+", "").replace(".", "").replace(",", "").replace("(", "").replace(")", "");
    }

    //Simply remove the cents value of the price by rounding up
    //ie : 1.00 ==> 1
    //1.50 ==> 1
    public static String removeCents(String value){
        int centsIndex = value.indexOf(".");
        value = value.substring(0, centsIndex);
        return value;
    }

    /**
     * Given a list of transactions that may contain different currencies, calculate the total
     * in respect to the default currency while using the rate set at the time the transaction was
     * created.
     * This will filter out transactions with no category and whos day type set to scheduled.
     * @return sum in the default currency
     */
    public static double findTotalCostForTransactions(List<Transaction> transactionList){
        double currentSum = 0f;
        for(int i = 0; i < transactionList.size(); i++){
            if(transactionList.get(i).getDayType().equalsIgnoreCase(DayType.COMPLETED.toString()) && transactionList.get(i).getCategory() != null) {
                currentSum += transactionList.get(i).getPrice();
            }
        }
        return currentSum;
    }
}
