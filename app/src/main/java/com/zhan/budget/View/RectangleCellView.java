package com.zhan.budget.View;

import android.content.Context;
import android.util.AttributeSet;

import com.p_v.flexiblecalendar.view.CircularEventCellView;

/**
 * Created by Zhan on 15-12-16.
 */
public class RectangleCellView extends CircularEventCellView {

    public RectangleCellView(Context context) {
        super(context);
    }

    public RectangleCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangleCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
