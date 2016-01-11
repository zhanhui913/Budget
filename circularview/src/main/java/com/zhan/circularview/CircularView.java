package com.zhan.circularview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Zhan on 16-01-07.
 */
public class CircularView extends View {

    public enum IconSize {XSMALL, SMALL, MEDIUM, LARGE, XLARGE}

    //Default values
    private final static int DEFAULT_BG_COLOR = R.color.colorPrimary;
    private final static int DEFAULT_STROKE_WIDTH = 0;
    private final static int DEFAULT_STROKE_COLOR = R.color.black;
    private final static int DEFAULT_STROKE_PADDING = 0;
    private final static int DEFAULT_ICON_SIZE  = 5;//LARGE
    private final static int DEFAULT_ICON_COLOR = R.color.white;


    private Context context;
    private int backgroundColor;
    private int strokeWidth;
    private int strokeColor;
    private int strokePadding;
    private IconSize eiconSize;
    private int iconSize;
    private int iconColor;
    private Drawable iconDrawable;
    private Paint paint;

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

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularView, 0, 0);
        try{
            backgroundColor = a.getColor(R.styleable.CircularView_cv_bgColor, getResources().getColor(DEFAULT_BG_COLOR));
            strokeWidth = a.getInteger(R.styleable.CircularView_cv_strokeWidth, DEFAULT_STROKE_WIDTH);
            strokeColor = a.getColor(R.styleable.CircularView_cv_strokeColor, getResources().getColor(DEFAULT_STROKE_COLOR));
            strokePadding = a.getInteger(R.styleable.CircularView_cv_strokePadding, DEFAULT_STROKE_PADDING);
            iconDrawable = a.getDrawable(R.styleable.CircularView_cv_iconDrawable);
            iconSize = a.getInteger(R.styleable.CircularView_cv_iconSize, DEFAULT_ICON_SIZE);
            iconColor = a.getColor(R.styleable.CircularView_cv_iconColor, getResources().getColor(DEFAULT_ICON_COLOR));
        }finally {
            a.recycle();
        }

        paint = new Paint();
        paint.setAntiAlias(true);
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
        return 50;
    }

    private int getDesiredHeight(){
        return 50;
    }

    private void drawCircle(Canvas canvas, int radius, int width, int height){
        if(strokeWidth > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(strokeColor);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawCircle(width, height, radius - (strokeWidth/2), paint);


            paint.setStyle(Paint.Style.FILL);
            paint.setColor(backgroundColor);
            canvas.drawCircle(width, height, radius - (strokeWidth) - strokePadding, paint);
        }else{
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(backgroundColor);
            canvas.drawCircle(width, height, radius, paint);
        }
    }

    private void drawIcon(Canvas canvas){
        if(iconDrawable != null){
            Rect bounds = canvas.getClipBounds();

            int multiplier = 5;

            bounds.left += (iconSize * multiplier);
            bounds.right -= (iconSize * multiplier);
            bounds.top += (iconSize * multiplier);
            bounds.bottom -= (iconSize * multiplier);

            iconDrawable.setBounds(bounds);
            iconDrawable.mutate().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            iconDrawable.draw(canvas);
        }
    }

    public int getCircleColor() {
        return backgroundColor;
    }

    public void setCircleColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        invalidate();
    }

    public int getStrokePadding() {
        return strokePadding;
    }

    public void setStrokePadding(int strokePadding) {
        this.strokePadding = strokePadding;
    }

    public IconSize getIconSize() {
        return eiconSize;
    }

    public void setIconSize(IconSize size) {
        this.eiconSize = size;
        this.iconSize = convertEnumToSize(this.eiconSize);
        invalidate();
    }

    private int convertEnumToSize(IconSize size){
        if(size == IconSize.XSMALL){
            return 17;
        }else if(size == IconSize.SMALL){
            return 13;
        }else if(size == IconSize.MEDIUM){
            return 9;
        }else if(size == IconSize.LARGE){
            return 5;
        }else{
            return 1;
        }
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
