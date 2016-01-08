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
    private final static int DEFAULT_STROKE_WIDTH = 5;
    private final static int DEFAULT_STROKE_COLOR = R.color.black;
    private final static int DEFAULT_ICON_SIZE  = 5;//LARGE
    private final static int DEFAULT_ICON_COLOR = R.color.white;


    private Context context;
    private int bg_color;
    private int stroke_width;
    private int stroke_color;
    private IconSize eiconSize;
    private int icon_size;
    private int icon_color;
    private Drawable icon;
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

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularCellView, 0, 0);
        try{
            bg_color = a.getColor(R.styleable.CircularCellView_bg_color, getResources().getColor(DEFAULT_BG_COLOR));
            stroke_width = a.getInteger(R.styleable.CircularCellView_stroke_width, DEFAULT_STROKE_WIDTH);
            stroke_color = a.getColor(R.styleable.CircularCellView_stroke_color, getResources().getColor(DEFAULT_STROKE_COLOR));
            icon = a.getDrawable(R.styleable.CircularCellView_cv_icon);
            icon_size = a.getInteger(R.styleable.CircularCellView_icon_size, DEFAULT_ICON_SIZE);
            icon_color = a.getColor(R.styleable.CircularCellView_icon_color, getResources().getColor(DEFAULT_ICON_COLOR));
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

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        int radius;
        if (viewWidthHalf > viewHeightHalf) {
            radius = viewHeightHalf;
        } else {
            radius = viewWidthHalf;
        }

        drawCircle(canvas, radius, viewWidthHalf, viewHeightHalf);
        drawIcon(canvas);
    }

    private int getDesiredWidth() {
        return 50;
    }

    private int getDesiredHeight(){
        return 50;
    }

    private void drawCircle(Canvas canvas, int radius, int width, int height){
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bg_color);
        canvas.drawCircle(width, height, radius, paint);

        if(stroke_width > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(stroke_color);
            float saveStrokeWidth = paint.getStrokeWidth();
            paint.setStrokeWidth(stroke_width);
            canvas.drawCircle(width, height, radius - (stroke_width / 2), paint);
            paint.setStrokeWidth(saveStrokeWidth);
        }
    }

    private void drawIcon(Canvas canvas){
        if(icon != null){
            Rect bounds = canvas.getClipBounds();

            int multiplier = 5;

            bounds.left += (icon_size * multiplier);
            bounds.right -= (icon_size * multiplier);
            bounds.top += (icon_size * multiplier);
            bounds.bottom -= (icon_size * multiplier);

            icon.setBounds(bounds);
            icon.mutate().setColorFilter(icon_color, PorterDuff.Mode.SRC_IN);
            icon.draw(canvas);
        }
    }

    public int getBg_color() {
        return bg_color;
    }

    public void setBg_color(int bg_color) {
        this.bg_color = bg_color;
    }

    public int getStroke_width() {
        return stroke_width;
    }

    public void setStroke_width(int stroke_width) {
        this.stroke_width = stroke_width;
    }

    public int getStroke_color() {
        return stroke_color;
    }

    public void setStroke_color(int stroke_color) {
        this.stroke_color = stroke_color;
    }

    public IconSize getIcon_size() {
        return eiconSize;
    }

    public void setIcon_size(IconSize size) {
        this.eiconSize = size;
        this.icon_size = convertEnumToSize(this.eiconSize);
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

    public int getIcon_color() {
        return icon_color;
    }

    public void setIcon_color(int icon_color) {
        this.icon_color = icon_color;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
/*
    public void setIcon(int drawableId){
        this.icon = ResourcesCompat.getDrawable(getResources(), drawableId, this.context.getTheme());
    }*/


}
