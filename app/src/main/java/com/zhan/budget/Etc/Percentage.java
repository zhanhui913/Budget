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



////////////////////////////////////////////////////////////////////////////////////////////////////
        //before percent rounded
        float notRoundedSum = 0;
        for(int i = 0; i < unRoundedPercentList.size(); i++){
            notRoundedSum += unRoundedPercentList.get(i);
        }
        Log.d("PERCENTAGE", "UNROUNDED PERCENT SUM : "+notRoundedSum);

////////////////////////////////////////////////////////////////////////////////////////////////////
        //after percent rounded
        float roundedSum = 0;
        for(int i = 0; i < roundedPercentList.size(); i++){
            roundedSum += roundedPercentList.get(i);
        }
        Log.d("PERCENTAGE", "ROUNDED PERCENT SUM : "+roundedSum);
    }


    public static BigDecimal roundTo2DecimalPlace(float value, int scale){
        BigDecimal current = BigDecimal.valueOf(value);
        return current.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    public static Float getError(float unRoundedValue){
        BigDecimal rounded = roundTo2DecimalPlace(unRoundedValue, SCALE);
        float error = ((rounded.floatValue() - unRoundedValue) / rounded.floatValue()) * 100;
        Log.d("PERCENTAGE", unRoundedValue+", "+rounded.floatValue()+" => "+Math.abs(error * 100)+"%");
        return error;
    }
}
