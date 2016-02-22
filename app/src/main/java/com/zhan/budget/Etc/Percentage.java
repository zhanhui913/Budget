package com.zhan.budget.Etc;

import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-02-20.
 */
public final class Percentage {

    public static final int SCALE = 4;

    //There is a possiblity that after rounding will have the sum = 1.0 where as before rounding will have it to 1.0001
    //http://stackoverflow.com/questions/13483430/how-to-make-rounded-percentages-add-up-to-100
    /*

        case 1:
    	List<Float> list = new ArrayList<>();
		list.add(1.2011f);
		list.add(2.5312f);
		list.add(3.644f);
		list.add(13.212f);
		list.add(13.655f);

        produces unrounded sum = 1.0000001
        and rounded sum = 1.0
     */
/*

        case 2:
		List<Float> list = new ArrayList<>();
		list.add(12.2031f);
		list.add(20.5902f);
		list.add(3.749f);
		list.add(13.232f);
		list.add(13.6885f);v

        produces both sum = 1.0
 */
/*

    case 3:
		List<Float> list = new ArrayList<>();
		list.add(12.90311006f);
		list.add(20.59224f);
		list.add(3.7493f);
		list.add(13.9817632f);

		prduces unrounded sum = 0.99999
		and rounded sum = 1.0
     */

    public static void roundTo100(List<Float> list){
        //fake the list for now
        /*list = new ArrayList<>();
        list.add( 13.626332f);
        list.add(47.989636f);
        list.add(9.596008f);
        list.add(28.788024f);*/

        List<Float> unRoundedPercentList = new ArrayList<>();
        List<Float> errorList = new ArrayList<>();
        List<Float> roundedPercentList = new ArrayList<>();

        //Get the sum for the original list
        float sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum += list.get(i);
        }

        //Get the unrounded percentage (from 0 to 1)
        for(int i = 0; i < list.size(); i++){
            unRoundedPercentList.add((list.get(i) / sum));
        }

        //Get the rounded percentage (from 0 to 1)
        for(int i = 0; i < unRoundedPercentList.size(); i++){
            roundedPercentList.add(roundTo2DecimalPlace(unRoundedPercentList.get(i), SCALE).floatValue());
        }


        for(int i = 0; i < unRoundedPercentList.size(); i++){
            errorList.add(getError(unRoundedPercentList.get(i)));
        }



////////////////////////////////////////////////////////////////////////////////////////////////////
        int pos = -1;
        for(int i = 0; i < errorList.size(); i++){
            if(errorList.get(i) > errorList.size()){
                Log.d("PERCENTAGE", "DONT ROUND " + errorList.get(i) + " @ pos " + i);
                pos = i;
                //replace rounded at position with unrounded
                double v =  Math.floor(unRoundedPercentList.get(i) * Math.pow(10, SCALE)) / Math.pow(10, SCALE);
                roundedPercentList.set(i, (float)v);
            }
        }



////////////////////////////////////////////////////////////////////////////////////////////////////
        //before percent rounded
        float notRoundedSum = 0;
        for(int i = 0; i < unRoundedPercentList.size(); i++){
            notRoundedSum += unRoundedPercentList.get(i);
        }
        Log.d("PERCENTAGE", "UNROUNDED PERCENT SUM : " + notRoundedSum);

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //after percent rounded
        float roundedSum = 0;
        for(int i = 0; i < roundedPercentList.size(); i++){
            roundedSum += roundedPercentList.get(i);
        }
        Log.d("PERCENTAGE","ROUNDED PERCENT SUM : " + roundedSum);

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        Log.d("PERCENTAGE", "ROUNDED PERCENT LIST [");
        for(int i = 0; i < roundedPercentList.size(); i++){
            Log.d("PERCENTAGE", roundedPercentList.get(i) + ",");
        }
        Log.d("PERCENTAGE", "]");

    }

/*
    function foo(l, target) {
        var off = target - _.reduce(l, function(acc, x) {
            return acc + Math.round(x)
        }, 0);
        return _.chain(l).sortBy(function(x) {
            return Math.round(x) - x
        }).
        map(function(x, i) {
            return Math.round(x) + (off > i) - (i >= (l.length + off))
        }).
        value();
    }
    */

    public static void foo(List<Float> list, int target){
        float off = target - getSumFromList(list);
/*
        //chain sort by smallest
        List<Float> diffList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            diffList.add(Math.round(list.get(i)) - list.get(i));
        }

        //Sort from smallest to largest
        Collections.sort(diffList, new Comparator<Float>() {
            @Override
            public int compare(Float f1, Float f2) {
                //ascending order
                return Float.compare(f1, f2);
            }
        });*/




    }

    public static float getSumFromList(List<Float> list) {
        float sum = 0;
        for (int i = 0; i < list.size(); i++){
            sum += list.get(i);
        }
        return sum;
    }

    public static BigDecimal roundTo2DecimalPlace(float value, int scale){
        BigDecimal current = BigDecimal.valueOf(value);
        return current.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    public static Float getError(float unRoundedValue){
        BigDecimal rounded = roundTo2DecimalPlace(unRoundedValue, SCALE);
        float error = ((rounded.floatValue() - unRoundedValue) / unRoundedValue) * (float)Math.pow(10, SCALE);
        Log.d("PERCENTAGE",unRoundedValue+", "+rounded.floatValue()+" => "+Math.abs(error)+"%");
        return error;
    }
}
