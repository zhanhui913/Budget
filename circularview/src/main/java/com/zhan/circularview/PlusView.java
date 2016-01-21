package com.zhan.circularview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by zhanyap on 2016-01-20.
 */
public class PlusView extends View {


    //Default values
    private final static int DEFAULT_BG_COLOR = R.color.green;
    private final static int DEFAULT_ICON_SIZE  = 0;//NONE
    private final static int DEFAULT_ICON_COLOR = R.color.black;


    private Context context;
    private int backgroundColor;
    private int iconSize;
    private int iconColor;
    private Drawable iconDrawable;
    private Paint paint;

    public PlusView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public PlusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public PlusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public PlusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularView, 0, 0);
        try{
            backgroundColor = a.getColor(R.styleable.PlusView_pv_bgColor, getResources().getColor(DEFAULT_BG_COLOR));
            //iconDrawable = a.getDrawable(R.styleable.PlusView_pv_iconDrawable);
            iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add);
            iconSize = a.getInteger(R.styleable.PlusView_pv_iconSize, DEFAULT_ICON_SIZE);
            iconColor = a.getColor(R.styleable.PlusView_pv_iconColor, getResources().getColor(DEFAULT_ICON_COLOR));
        }finally {
            a.recycle();
        }

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Get the width measurement
        int widthSize = View.resolveSize(getDesiredWidth(), widthMeasureSpec);

        //Get the height measurement
        int heightSize = View.resolveSize(getDesiredHeight(), heightMeasureSpec);

        //MUST call this to store the measurements
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidthHalf = this.getWidth() / 2;
        int viewHeightHalf = this.getHeight() / 2;

        int radius;
        if (viewWidthHalf > viewHeightHalf) {
            radius = viewHeightHalf;
        } else {
            radius = viewWidthHalf;
        }

        drawCircle(canvas, radius, viewWidthHalf, viewHeightHalf);
        drawIcon(canvas);
        invalidate();
    }

    private int getDesiredWidth() {
        return dp2px(context, 50);
    }

    private int getDesiredHeight(){
        return dp2px(context, 50);
    }

    private void drawCircle(Canvas canvas, int radius, int width, int height){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);

        canvas.drawCircle(width, height, radius, paint);
    }

    private void drawIcon(Canvas canvas){
        if(iconDrawable != null){

            Rect bounds = canvas.getClipBounds();

            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float left = (metrics.widthPixels/2) - (iconDrawable.getIntrinsicWidth() / 2);

            canvas.translate(left,0);

            //iconDrawable.setBounds(bounds);

            iconDrawable.mutate().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            iconDrawable.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = iconDrawable.getIntrinsicHeight();
        iconDrawable.setBounds(0, 0, size, size);
    }

    public int getCircleColor() {
        return backgroundColor;
    }

    public void setCircleColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
        invalidate();
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        invalidate();
    }
/*
    public void setIcon(int drawableId){
        this.iconDrawable = ResourcesCompat.getDrawable(getResources(), drawableId, this.context.getTheme());
    }*/


}
