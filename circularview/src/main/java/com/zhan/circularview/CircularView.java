package com.zhan.circularview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by Zhan on 16-01-07.
 */
public class CircularView extends View {

    public enum IconSize {XSMALL, SMALL, MEDIUM, LARGE, XLARGE}

    //Default values
    private final static int DEFAULT_BG_RADIUS = 50; //pixels
    private final static int DEFAULT_BG_COLOR = R.color.colorPrimary;
    private final static int DEFAULT_STROKE_WIDTH = 0; //pixels
    private final static int DEFAULT_STROKE_COLOR = R.color.black;
    private final static int DEFAULT_STROKE_PADDING = 0; //pixels
    private final static int DEFAULT_ICON_SIZE  = 5;//LARGE
    private final static int DEFAULT_ICON_COLOR = R.color.white;
    private final static int DEFAULT_ICON_TOP_PADDING = 5;
    private final static int DEFAULT_ICON_BOTTOM_PADDING = 5;
    private final static int DEFAULT_ICON_LEFT_PADDING = 5;
    private final static int DEFAULT_ICON_RIGHT_PADDING = 5;

    private Context context;
    private int backgroundRadius; //pixels
    private int circleColor;
    private int strokeWidth;  //pixels
    private int strokeColor;
    private int strokePadding; //pixels
    private IconSize eiconSize;
    private int iconSize;
    private int iconColor;
    private Drawable iconDrawable;
    private int iconTopPadding;
    private int iconBottomPadding;
    private int iconLeftPadding;
    private int iconRightPadding;
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
            backgroundRadius = a.getDimensionPixelSize(R.styleable.CircularView_cv_bgRadius, DEFAULT_BG_RADIUS);
            circleColor = a.getColor(R.styleable.CircularView_cv_bgColor, ContextCompat.getColor(this.context, DEFAULT_BG_COLOR));
            strokeWidth = a.getDimensionPixelSize(R.styleable.CircularView_cv_strokeWidth, DEFAULT_STROKE_WIDTH);
            strokeColor = a.getColor(R.styleable.CircularView_cv_strokeColor, ContextCompat.getColor(this.context, DEFAULT_STROKE_COLOR));
            strokePadding = a.getDimensionPixelSize(R.styleable.CircularView_cv_strokePadding, DEFAULT_STROKE_PADDING);
            iconDrawable = a.getDrawable(R.styleable.CircularView_cv_iconDrawable);
            iconSize = a.getInteger(R.styleable.CircularView_cv_iconSize, DEFAULT_ICON_SIZE);
            iconColor = a.getColor(R.styleable.CircularView_cv_iconColor, ContextCompat.getColor(this.context, DEFAULT_ICON_COLOR));
        }finally {
            a.recycle();
        }

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ss = ((strokeWidth + strokePadding) * 2);

        int desiredWidth = (backgroundRadius * 2) + ss;
        int desiredHeight = (backgroundRadius * 2) + ss;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) { //specific value
            //Must be this size
            width = widthSize;
            circleColor = Color.RED;
        } else if (widthMode == MeasureSpec.AT_MOST) { //match parent
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
            circleColor = Color.BLUE;
        } else { //wrap content
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
            strokeColor = Color.GRAY;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
            strokeColor = Color.GREEN;
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidthHalf = this.getWidth() / 2;
        int viewHeightHalf = this.getHeight() / 2;
/*
        int radius;
        if (viewWidthHalf > viewHeightHalf) {
            radius = viewHeightHalf;
        } else {
            radius = viewWidthHalf;
        }

        drawCircle(canvas, radius, viewWidthHalf, viewHeightHalf);*/
        drawCircle(canvas, backgroundRadius, viewWidthHalf, viewHeightHalf);
        drawIcon(canvas);
        invalidate();
    }
/*
    private int getDesiredWidth() {
        return 50;
    }

    private int getDesiredHeight() {
        return 50;
    }*/

    private void drawCircle(Canvas canvas, int radius, int width, int height){
        if(Math.round(strokeWidth) > 0) { //If there's stroke defined
            //Paint the stroke
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(strokeColor);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawCircle(width, height, radius + strokePadding + (strokeWidth / 2), paint);
        }

        //Paint the inner circle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(circleColor);
        canvas.drawCircle(width, height, radius , paint);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Getters & Setters
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getBackgroundRadius() {
        return backgroundRadius;
    }

    public void setBackgroundRadius(int backgroundRadius) {
        this.backgroundRadius = backgroundRadius;
    }

    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
        invalidate();
    }

    public int getStrokeWidth() {
        return pxToDp(strokeWidth);
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = dpToPx(strokeWidth);
        invalidate();
    }

    public int getStrokeColor() {
        return pxToDp(strokeColor);
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        invalidate();
    }

    public int getStrokePadding() {
        return pxToDp(strokePadding);
    }

    public void setStrokePadding(int strokePadding) {
        this.strokePadding = dpToPx(strokePadding);
        invalidate();
    }

    public IconSize getIconSize() {
        return eiconSize;
    }

    public void setIconSize(IconSize size) {
        this.eiconSize = size;
        this.iconSize = convertEnumToSize(this.eiconSize);
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

    public int getIconTopPadding() {
        return iconTopPadding;
    }

    public void setIconTopPadding(int iconTopPadding) {
        this.iconTopPadding = iconTopPadding;
        invalidate();
    }

    public int getIconBottomPadding() {
        return iconBottomPadding;
    }

    public void setIconBottomPadding(int iconBottomPadding) {
        this.iconBottomPadding = iconBottomPadding;
        invalidate();
    }

    public int getIconLeftPadding() {
        return iconLeftPadding;
    }

    public void setIconLeftPadding(int iconLeftPadding) {
        this.iconLeftPadding = iconLeftPadding;
        invalidate();
    }

    public int getIconRightPadding() {
        return iconRightPadding;
    }

    public void setIconRightPadding(int iconRightPadding) {
        this.iconRightPadding = iconRightPadding;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

        /*
    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                this.context.getResources().getDisplayMetrics());
    }*/

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
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
}
