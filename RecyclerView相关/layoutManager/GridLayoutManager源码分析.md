# 草稿


GridLayoutManager 的 `layoutChunk` 方法是一个重要的方法，它负责对子 View 进行布局。在这个方法中，主要完成了以下几个步骤：

1. 计算当前行或列的剩余空间，以及当前行或列的最大尺寸。
2. 为当前行或列的子 View 分配 Span。
3. 测量子 View 的尺寸。
4. 为子 View 布局。


```java
@Override
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, 
        LayoutState layoutState, LayoutChunkResult result) {
    // 这里的 Dir 应该是 Direction 的缩写
    final int otherDirSpecMode = mOrientationHelper.getModeInOther();
    final boolean flexibleInOtherDir = otherDirSpecMode != View.MeasureSpec.EXACTLY;
    final int currentOtherDirSize = getChildCount() > 0 ? mCachedBorders[mSpanCount] : 0;
    // if grid layout's dimensions are not specified, let the new row change the measurements
    // This is not perfect since we not covering all rows but still solves an important case
    // where they may have a header row which should be laid out according to children.
    if(flexibleInOtherDir) {
        updateMeasurements(); //  reset measurements
    }
    final boolean layingOutInPrimaryDirection = layoutState.mItemDirection == LayoutState.ITEM_DIRECTION_TAIL;
    int count = 0;
    //剩余的 span
    int remainingSpan = mSpanCount;
    if(!layingOutInPrimaryDirection) {
        int itemSpanIndex = getSpanIndex(recycler, state, layoutState.mCurrentPosition);
        int itemSpanSize = getSpanSize(recycler, state, layoutState.mCurrentPosition);
        remainingSpan = itemSpanIndex + itemSpanSize;
    }
    while(count < mSpanCount && layoutState.hasMore(state) && remainingSpan > 0) {
        int pos = layoutState.mCurrentPosition;
        //默认跨度是1
        final int spanSize = getSpanSize(recycler, state, pos);
        if(spanSize > mSpanCount) {
            throw new IllegalArgumentException("Item at position " + pos + " requires " + spanSize + " spans but GridLayoutManager has only " + mSpanCount + " spans.");
        }
        remainingSpan -= spanSize;
        if(remainingSpan < 0) {
            break; // item did not fit into this row or column
        }
        //获取下一个 View
        View view = layoutState.next(recycler);
        if(view == null) {
            break;
        }
        //mSet Temporary array to keep views in layoutChunk method
        mSet[count] = view;
        count++;
    }
    if(count == 0) {
        result.mFinished = true;
        return;
    }
    int maxSize = 0;
    float maxSizeInOther = 0; // use a float to get size per span
    // we should assign spans before item decor offsets are calculated
    assignSpans(recycler, state, count, layingOutInPrimaryDirection);
    for(int i = 0; i < count; i++) {
        View view = mSet[i];
        if(layoutState.mScrapList == null) {
            if(layingOutInPrimaryDirection) {
                //添加 View
                addView(view);
            } else {
                addView(view, 0);
            }
        } else {
            if(layingOutInPrimaryDirection) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        calculateItemDecorationsForChild(view, mDecorInsets);
        //测量子 View
        measureChild(view, otherDirSpecMode, false);
        final int size = mOrientationHelper.getDecoratedMeasurement(view);
        if(size > maxSize) {
            //在我们的例子中，这里是获取当前行最大的高度
            maxSize = size;
        }
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final float otherSize = 1f * mOrientationHelper.getDecoratedMeasurementInOthe(view) / lp.mSpanSize;
        //比如我们纵向布局的时候，这里的 otherSize 就是横向的尺寸，这里是为了找出当前行最大的宽度
        if(otherSize > maxSizeInOther) {
            maxSizeInOther = otherSize;
        }
    }
    if(flexibleInOtherDir) {
        // re-distribute columns
        guessMeasurement(maxSizeInOther, currentOtherDirSize);
        // now we should re-measure any item that was match parent.
        maxSize = 0;
        for(int i = 0; i < count; i++) {
            View view = mSet[i];
            measureChild(view, View.MeasureSpec.EXACTLY, true);
            final int size = mOrientationHelper.getDecoratedMeasurement(view);
            if(size > maxSize) {
                maxSize = size;
            }
        }
    }
    // Views that did not measure the maxSize has to be re-measured
    // We will stop doing this once we introduce Gravity in the GLM layout params
    for(int i = 0; i < count; i++) {
        final View view = mSet[i];
        if(mOrientationHelper.getDecoratedMeasurement(view) != maxSize) {
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            final Rect decorInsets = lp.mDecorInsets;
            final int verticalInsets = decorInsets.top + decorInsets.bottom + lp.topMargin + lp.bottomMargin;
            final int horizontalInsets = decorInsets.left + decorInsets.right + lp.leftMargin + lp.rightMargin;
            final int totalSpaceInOther = getSpaceForSpanRange(lp.mSpanIndex, lp.mSpanSize);
            final int wSpec;
            final int hSpec;
            if(mOrientation == VERTICAL) {
                wSpec = getChildMeasureSpec(totalSpaceInOther, View.MeasureSpec.EXACTLY, horizontalInsets, lp.width, false);
                hSpec = View.MeasureSpec.makeMeasureSpec(maxSize - verticalInsets, View.MeasureSpec.EXACTLY);
            } else {
                wSpec = View.MeasureSpec.makeMeasureSpec(maxSize - horizontalInsets, View.MeasureSpec.EXACTLY);
                hSpec = getChildMeasureSpec(totalSpaceInOther, View.MeasureSpec.EXACTLY, verticalInsets, lp.height, false);
            }
            measureChildWithDecorationsAndMargin(view, wSpec, hSpec, true);
        }
    }
    //记录消耗的高空间，在我们的例子中，是高度
    result.mConsumed = maxSize;
    int left = 0, right = 0, top = 0, bottom = 0;
    if(mOrientation == VERTICAL) {
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            bottom = layoutState.mOffset;
            top = bottom - maxSize;
        } else {
            //确定当前行的 top 和 bottom
            top = layoutState.mOffset;
            bottom = top + maxSize;
        }
    } else {
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            right = layoutState.mOffset;
            left = right - maxSize;
        } else {
            left = layoutState.mOffset;
            right = left + maxSize;
        }
    }
    for(int i = 0; i < count; i++) {
        View view = mSet[i];
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if(mOrientation == VERTICAL) {
            if(isLayoutRTL()) {
                right = getPaddingLeft() + mCachedBorders[mSpanCount - params.mSpanIndex];
                left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
            } else {
                //确定当前 View 的 left 和 right
                left = getPaddingLeft() + mCachedBorders[params.mSpanIndex];
                right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
            }
        } else {
            top = getPaddingTop() + mCachedBorders[params.mSpanIndex];
            bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view);
        }
        // We calculate everything with View's bounding box (which includes decor and margins)
        // To calculate correct layout position, we subtract margins.
        //摆放子 View
        layoutDecoratedWithMargins(view, left, top, right, bottom);
        if(DEBUG) {
            Log.d(TAG, "laid out child at position " + getPosition(view) + ", with l:" + (left + params.leftMargin) + ", t:" + (top + params.topMargin) + ", r:" + (right - params.rightMargin) + ", b:" + (bottom - params.bottomMargin) + ", span:" + params.mSpanIndex + ", spanSize:" + params.mSpanSize);
        }
        // Consume the available space if the view is not removed OR changed
        if(params.isItemRemoved() || params.isItemChanged()) {
            result.mIgnoreConsumed = true;
        }
        result.mFocusable |= view.hasFocusable();
    }
    Arrays.fill(mSet, null);
}
```