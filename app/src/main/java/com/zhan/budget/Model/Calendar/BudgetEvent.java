package com.zhan.budget.Model.Calendar;

/**
 * Created by zhanyap on 2015-12-30.
 */
public class BudgetEvent implements com.p_v.flexiblecalendar.entity.Event {

    private int color;

    public BudgetEvent(int color){
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }
}
