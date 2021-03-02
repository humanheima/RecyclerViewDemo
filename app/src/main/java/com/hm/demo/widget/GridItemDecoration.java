package com.hm.demo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private int mLeftSpace;
    private int mRightSpace;

    /**
     * @param context
     * @param leftSpace
     * @param rightSpace
     */
    public GridItemDecoration(Context context, int leftSpace, int rightSpace) {
        this.mLeftSpace = leftSpace;
        this.mRightSpace = rightSpace;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = mLeftSpace;
        outRect.right = mRightSpace;
    }
}