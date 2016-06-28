package com.zhan.budget.View;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zhanyap on 2016-06-28.
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int horizontalSpace;
    private int verticalSpace;

    public SpacesItemDecoration(int space) {
        this(space, space);
    }

    public SpacesItemDecoration(int horizontalSpace, int verticalSpace){
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
    }

    public SpacesItemDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    public SpacesItemDecoration(@NonNull Context context, @DimenRes int horizontalSpace, @DimenRes int verticalSpace) {
        this(context.getResources().getDimensionPixelSize(horizontalSpace), context.getResources().getDimensionPixelSize(verticalSpace));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(horizontalSpace, verticalSpace, horizontalSpace, verticalSpace);
    }
}