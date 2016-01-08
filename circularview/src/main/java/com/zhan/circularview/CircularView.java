package com.zhan.circularview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Zhan on 16-01-07.
 */
public class CircularView extends View {

    private Context context;
    private int bg_color;
    private int stroke_width;
    private int stroke_color;
    private Paint paintCircle;
    private Paint paintBorder;

    private int drawable;
    private ImageView icon;

    public CircularView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public CircularView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CircularView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public CircularView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;

        paintCircle = new Paint();
        paintBorder = new Paint();

        paintCircle.setAntiAlias(true);
        paintBorder.setAntiAlias(true);


        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularCellView, 0, 0);
        try{
            bg_color = a.getColor(R.styleable.CircularCellView_bg_color, getResources().getColor(android.R.color.holo_blue_light)); //default is holo_blue_light
            stroke_width = a.getInteger(R.styleable.CircularCellView_stroke_width, 0);//default is 0
            stroke_color = a.getColor(R.styleable.CircularCellView_stroke_color, getResources().getColor(android.R.color.holo_blue_light)); //default is holo_blue_light
            drawable = a.getInteger(R.styleable.CircularCellView_cv_icon, 0);
        }finally {
            a.recycle();
        }

        if(drawable != 0){
            icon.setBackgroundResource(drawable);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Get the width measurement
        int widthSize = View.resolveSize(getDesiredWidth(), widthMeasureSpec);

        //Get the height measurement
        int heightSize = View.resolveSize(getDesiredHeight(), heightMeasureSpec);

        //MUST call this to store the measurements
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override protected void onDraw(Canvas canvas) {
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        int radius;
        if (viewWidthHalf > viewHeightHalf) {
            radius = viewHeightHalf;
        } else {
            radius = viewWidthHalf;
        }

        drawCircle(canvas, radius, viewWidthHalf, viewHeightHalf);
    }

    private int getDesiredWidth() {
        return 50;
    }

    private int getDesiredHeight(){
        return 50;
    }

    private void drawCircle(Canvas canvas, int radius, int width, int height){
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(bg_color);
        canvas.drawCircle(width, height, radius, paintCircle);

        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setColor(stroke_color);
        paintBorder.setStrokeWidth(stroke_width);
        canvas.drawCircle(width, height, radius, paintBorder);
    }

    /**
     * Sets color to shape.
     *
     * @param color a color integer associated with a particular resource id
     */
    public void setShapeColor(int color) {
        this.bg_color = color;
        invalidate();
    }

    public int getBgColor(){
        return this.bg_color;
    }

    public Paint getShapePaint() {
        return paintCircle;
    }



}
