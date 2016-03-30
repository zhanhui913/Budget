package com.zhan.budget.View;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.p_v.flexiblecalendar.entity.Event;
import com.p_v.flexiblecalendar.view.BaseCellView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhan on 15-12-16.
 */
public class CalendarSmallCircleView extends BaseCellView {

    private int eventCircleY;
    private int radius;
    private int padding;
    private int leftMostPosition = Integer.MIN_VALUE;
    private List<Paint> paintList;

    public CalendarSmallCircleView(Context context) {
        super(context);
    }

    public CalendarSmallCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CalendarSmallCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, com.p_v.fliexiblecalendar.R.styleable.CircularEventCellView);
        try{
            radius = a.getDimensionPixelSize(com.p_v.fliexiblecalendar.R.styleable.CircularEventCellView_event_radius, dpToPx(2));
            padding = a.getDimensionPixelSize(com.p_v.fliexiblecalendar.R.styleable.CircularEventCellView_event_circle_padding,dpToPx(1));
        }finally {
            a.recycle();
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(paintList != null){
            int num = paintList.size();

            Paint p = new Paint();
            p.setTextSize(getTextSize());

            Rect rect = new Rect();
            p.getTextBounds("31", 0, 1, rect); // measuring using fake text

            eventCircleY = (3 * getHeight() + rect.height()) / 4;

            //calculate left most position for the circle
            if (leftMostPosition == Integer.MIN_VALUE) {
                leftMostPosition = (getWidth() / 2) - (num / 2) * 2 * (padding + radius);
                if (num % 2 == 0) {
                    leftMostPosition = leftMostPosition + radius + padding;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(paintList != null){
            int num = paintList.size();
            for (int i=0;i<num;i++) {
                canvas.drawCircle(calculateStartPoint(i), eventCircleY, radius, paintList.get(i));
            }
        }
    }

    private int calculateStartPoint(int offset){
        return leftMostPosition + offset *(2*(radius+padding)) ;
    }

    @Override
    public void setEvents(List<? extends Event> colorList){
        if(colorList!=null){
            paintList = new ArrayList<>(colorList.size());
            for(Event e: colorList){
                Paint eventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                eventPaint.setStyle(Paint.Style.FILL);
                eventPaint.setColor(ContextCompat.getColor(getContext(), e.getColor()));
                paintList.add(eventPaint);
            }
            invalidate();
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height =  (3 * width) / 5;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
