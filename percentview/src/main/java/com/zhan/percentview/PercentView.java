package com.zhan.percentview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.zhan.percentview.Model.Slice;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Zhan on 16-01-07.
 */
public class PercentView extends View {

    //Default values
    private final static int DEFAULT_BG_RADIUS = 50; //pixels

    private Context context;

    private Paint paint;
    private float sumWeight;
    List<Slice> sliceList = new ArrayList<>();

    public PercentView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public PercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public PercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public PercentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 50;
        int desiredHeight = 50;

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
        } else if (widthMode == MeasureSpec.AT_MOST) { //match parent
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else { //wrap content
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRectangle(canvas, this.getWidth(), this.getHeight());
    }

    private void drawRectangle(Canvas canvas, int width, int height){
        int w = 0;

        if(sliceList.size() != 0) {
            for (int i = 0; i < sliceList.size(); i++) {
                paint.setColor(Color.parseColor(sliceList.get(i).getColor()));
                canvas.drawRect(w, 0, w += sliceList.get(i).getPixels(), getHeight(), paint);
            }
        }else{
            paint.setColor(ContextCompat.getColor(getContext(), R.color.harbor_rat));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Getters & Setters
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public List<Slice> getSliceList() {
        return sliceList;
    }

    public void setSliceList(List<Slice> sliceList) {
        this.sliceList = sliceList;

        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkColorAndDrawable(int value){
        if(value == 0){
            throw new IllegalArgumentException("Cannot process color or drawable with value = 0");
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
