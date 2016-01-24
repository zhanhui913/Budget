package com.zhan.budget.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import com.zhan.budget.R;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.PtrUIHandlerHook;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;

/**
 * Created by zhanyap on 2016-01-20.
 */
public class CenterPanelView extends View implements PtrUIHandler{

    private Context context;
    private Drawable iconDrawable;
    private int iconColor, iconSize;
    private float mScale = 1f;
    private PtrFrameLayout mPtrFrameLayout;
    private Paint paint;
    private double mWidth, mHeight;

    public CenterPanelView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public CenterPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CenterPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public CenterPanelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;
        iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add);
        iconColor = ContextCompat.getColor(context, R.color.black);

        paint = new Paint();
        paint.setAntiAlias(true);

        setupSize();
    }

    private void setupSize(){
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float screenDensity = metrics.density;
        mWidth = 40 * screenDensity;
        mHeight = 40 * screenDensity;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = iconDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        int width = iconDrawable.getIntrinsicWidth();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


/*
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
*/
    }

    private int getTopOffset() {
        return getPaddingTop() + PtrLocalDisplay.dp2px(10);
    }

    private int getBottomOffset() {
        return getPaddingBottom() + PtrLocalDisplay.dp2px(10);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = iconDrawable.getIntrinsicHeight();
        iconDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawIcon(canvas);
        invalidate();
    }


    private void drawIcon(Canvas canvas){
        if(iconDrawable != null){
            Rect bounds = canvas.getClipBounds();
/*
            int l = 0;//getPaddingLeft() ;//+ (getMeasuredWidth() - iconDrawable.getIntrinsicWidth()) / 2;
            canvas.translate(l, getPaddingTop());
            canvas.scale(mScale, mScale, bounds.exactCenterX(), bounds.exactCenterY());
            iconDrawable.setBounds(bounds);

            iconDrawable.mutate().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            iconDrawable.draw(canvas);
*/


/*
            Rect rect = iconDrawable.getBounds();
           // int l = getPaddingLeft() + getPaddingRight();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            canvas.scale(mScale, mScale, bounds.exactCenterX(), bounds.exactCenterY());
            iconDrawable.mutate().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            iconDrawable.draw(canvas);
            */


/*
            bounds.left += 100;
            bounds.right -= 100;
            bounds.top += 0;
            bounds.bottom -= 0;
            */
            //canvas.translate((getWidth()/2) - (iconDrawable.getIntrinsicWidth()/2), getPaddingTop());

            int l = getWidth() - iconDrawable.getIntrinsicWidth();
            int t = getHeight() - iconDrawable.getIntrinsicHeight();



            //iconDrawable.setBounds(bounds);
            iconDrawable.mutate().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            iconDrawable.draw(canvas);
        }
    }

    public void playRotateAnimation(){
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.anim_rotate);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("CENTER_PANEL_VIEW","animation done");
                    }
                }, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //play rotate animation of the plus icon
        this.startAnimation(anim);
    }

    private Animation mScaleAnimation = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            mScale = 1f - interpolatedTime;
            iconDrawable.setAlpha((int) (255 * mScale));
            invalidate();
        }
    };

    public void setPtrFrameLayout(PtrFrameLayout layout) {

        final PtrUIHandlerHook mPtrUIHandlerHook = new PtrUIHandlerHook() {
            @Override
            public void run() {
                startAnimation(mScaleAnimation);
            }
        };

        mScaleAnimation.setDuration(200);
        mScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPtrUIHandlerHook.resume();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPtrFrameLayout = layout;
        mPtrFrameLayout.setRefreshCompleteHook(mPtrUIHandlerHook);
    }


    public int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * When the content view has reached top and refresh has been completed, view will be reset.
     *
     * @param frame
     */
    @Override
    public void onUIReset(PtrFrameLayout frame) {
        Log.d("CENTER_PANEL_VIEW","onUIReset");
        mScale = 1f;
    }

    /**
     * prepare for loading
     *
     * @param frame
     */
    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        Log.d("CENTER_PANEL_VIEW","onUIRefreshPrepare");
    }

    /**
     * perform refreshing UI
     *
     * @param frame
     */
    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        Log.d("CENTER_PANEL_VIEW","onUIRefreshBegin");
        playRotateAnimation();
    }

    /**
     * perform UI after refresh
     *
     * @param frame
     */
    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        Log.d("CENTER_PANEL_VIEW","onUIRefreshComplete");
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        float percent = Math.min(1f, ptrIndicator.getCurrentPercent());
    }
}
