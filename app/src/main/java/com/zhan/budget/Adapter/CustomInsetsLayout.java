package com.zhan.budget.Adapter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

/**
 * http://stackoverflow.com/questions/21092888/windowsoftinputmode-adjustresize-not-working-with-translucent-action-navbar
 *
 * @author Kevin
 *         Date Created: 3/7/14
 *
 * https://code.google.com/p/android/issues/detail?id=63777
 *
 * When using a translucent status bar on API 19+, the window will not
 * resize to make room for input methods (i.e.
 * {@link android.view.WindowManager.LayoutParams#SOFT_INPUT_ADJUST_RESIZE} and
 * {@link android.view.WindowManager.LayoutParams#SOFT_INPUT_ADJUST_PAN} are
 * ignored).
 *
 * To work around this; override {@link #fitSystemWindows(Rect)},
 * capture and override the system insets, and then call through to FrameLayout's
 * implementation.
 *
 * For reasons yet unknown, modifying the bottom inset causes this workaround to
 * fail. Modifying the top, left, and right insets works as expected.
 */
public class CustomInsetsLayout extends RelativeLayout {
    private int[] mInsets = new int[4];

    public CustomInsetsLayout(Context context) {
        super(context);
    }

    public CustomInsetsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomInsetsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public final int[] getInsets() {
        return mInsets;
    }

    @Override
    protected final boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Intentionally do not modify the bottom inset. For some reason,
            // if the bottom inset is modified, window resizing stops working.
            // TODO: Figure out why.
            mInsets[0] = insets.left;
            mInsets[1] = insets.top;
            mInsets[2] = insets.right;

            insets.left = 0;
            insets.top = 0;
            insets.right = 0;

            if (insets.bottom > 0) {
                // Remove bottom margin from the root view
                LayoutParams params = (LayoutParams) getLayoutParams();
                params.bottomMargin = 0;
                setLayoutParams(params);

            }
        }

        return super.fitSystemWindows(insets);
    }

    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (insets.getSystemWindowInsetBottom() > 0) {
                // Remove bottom margin from the root view
                LayoutParams params = (LayoutParams) getLayoutParams();
                params.bottomMargin = 0;
                setLayoutParams(params);

            }
            mInsets[0] = insets.getSystemWindowInsetLeft();
            mInsets[1] = insets.getSystemWindowInsetTop();
            mInsets[2] = insets.getSystemWindowInsetRight();
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0,
                    insets.getSystemWindowInsetBottom()));

        } else {
            return insets;
        }
    }
}