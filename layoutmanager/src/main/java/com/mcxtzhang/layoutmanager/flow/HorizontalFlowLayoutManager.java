package com.mcxtzhang.layoutmanager.flow;

import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dumingwei on 2021/6/21
 * <p>
 * Desc:横向排列，竖向可以指定最多的行数，失败，失败，失败
 */

public class HorizontalFlowLayoutManager extends RecyclerView.LayoutManager {


    private static final String TAG = "HorizontalFlowLayoutMan";
    /**
     * 记录滑动的偏移量
     */
    private int mVerticalOffset;//竖直偏移量 每次换行时，要根据这个offset判断
    private int mHorizontalOffset;
    private int mFirstVisiPos;//屏幕可见的第一个View的Position
    private int mLastVisiPos;//屏幕可见的最后一个View的Position


    private SparseArray<Rect> mItemRects;//key 是View的position，保存View的bounds 和 显示标志，

    @RecyclerView.Orientation
    private int orientation;

    /**
     * 横向排列的时候，最多可以排几行，默认两行
     */
    private int maxLine = 2;
    //todo 是否需要记录每行最大的高度呢？

    private List<Integer> lineHeights;
    //每一行的最后一个item的右坐标
    private List<Integer> lineRights;

    public HorizontalFlowLayoutManager() {
        mItemRects = new SparseArray<>();
        orientation = RecyclerView.VERTICAL;
    }

    public HorizontalFlowLayoutManager(@RecyclerView.Orientation int orientation, int maxLine) {
        mItemRects = new SparseArray<>();
        this.orientation = orientation;
        if (orientation == RecyclerView.HORIZONTAL) {
            this.maxLine = maxLine;
            lineHeights = new ArrayList<>(maxLine);
            lineRights = new ArrayList<>(maxLine);

            for (int i = 0; i < maxLine; i++) {
                lineHeights.add(0);
                lineRights.add(0);
            }
        }
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "onLayoutChildren: ");
        if (getItemCount() == 0) {//没有Item，界面空着吧
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {//state.isPreLayout()是支持动画的
            return;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);

        //初始化区域
        mVerticalOffset = 0;
        mFirstVisiPos = 0;
        mLastVisiPos = getItemCount();

        //初始化时调用 填充childView
        if (orientation == RecyclerView.VERTICAL) {
            fillVertical(recycler, state);
        } else {
            fillHorizontal(recycler, state);
        }
    }

    /**
     * 初始化时调用 填充childView
     *
     * @param recycler
     * @param state
     */
    private void fillVertical(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fillVertical(recycler, state, 0);
    }

    /**
     * 初始化时调用 填充childView
     *
     * @param recycler
     * @param state
     */
    private void fillHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fillHorizontal(recycler, state, 0);
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     *
     * @param recycler
     * @param state
     * @param dx       RecyclerView给我们的位移量，手指从右向左滑动的时候，dx > 0 ; 手指从左向右滑动的时候，dx < 0 ；
     * @return 修正以后真正的dx（可能剩余空间不够移动那么多了 所以返回值小于dx的绝对值）
     */
    private int fillHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();

        //第一行的View的top坐标一定是RecyclerView的paddingTop
        lineHeights.set(0, paddingTop);

        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                if (dx > 0) {//需要回收从屏幕左边缘滑动出去的Views
                    if (getDecoratedRight(child) - dx < paddingLeft) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;
                    }
                } else if (dx < 0) {//需要回收从屏幕下边缘滑动出去的Views
                    if (getDecoratedLeft(child) - dx > getWidth() - paddingRight) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;
                    }
                }
            }
        }

        //布局子View阶段
        if (dx >= 0) {
            Log.i(TAG, "fillHorizontal: dx >= 0");
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {//如果有子View的话
                //注意，这里一定要用（getChildCount() - 1）这个下标
                //注意，这里一定要用（getChildCount() - 1）这个下标
                //注意，这里一定要用（getChildCount() - 1）这个下标

                int lastChildViewPosition = getChildCount() - 1;
                View lastView = getChildAt(lastChildViewPosition);
                //第一个可以添加View的position
                minPos = getPosition(lastView) + 1;


            }
            //顺序addChildView
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);

                //取余后的下标
                int remainderPosition = i % maxLine;
                //要添加的View的top坐标
                int top = lineHeights.get(remainderPosition);
                //要添加的View的left坐标
                int left = lineRights.get(remainderPosition);
                int decoratedWidth = getDecoratedMeasurementHorizontal(child);
                int decoratedHeight = getDecoratedMeasurementVertical(child);
                layoutDecoratedWithMargins(child, left, top, left + decoratedWidth, top + decoratedHeight);

                lineRights.set(remainderPosition, left + decoratedWidth);


                lineHeights.set(remainderPosition, top);

                int nextLineRemainderPosition = (remainderPosition + 1) % maxLine;
                if (nextLineRemainderPosition != 0) {
                    lineHeights.set(nextLineRemainderPosition, lineHeights.get(remainderPosition) + decoratedHeight);
                }

                Rect rect = new Rect(left, top + mHorizontalOffset, left + decoratedWidth, top + decoratedHeight + mHorizontalOffset);
                mItemRects.put(i, rect);

                int shouldBreak = 0;
                for (Integer lineRight : lineRights) {
                    if (lineRight > getWidth() - paddingRight) {
                        Log.i(TAG, "fillHorizontal: lineRight = " + lineRight);
                        //三行的宽度都超过屏幕的宽度了，结束外层循环
                        shouldBreak++;
                    }
                }
                Log.i(TAG, "fillHorizontal: shouldBreak = " + shouldBreak);
                if (shouldBreak == lineRights.size()) {
                    break;
                }
            }
            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy

            int lastChildViewPosition = getChildCount() - 1;
            View lastChild = getChildAt(lastChildViewPosition);
            int maxGap = 0;
            int gap = getWidth() - getPaddingRight() - getDecoratedRight(lastChild);
//                if (gap > 0) {
//                    dx -= gap;
//                }
            maxGap = Math.max(maxGap, gap);

            lastChildViewPosition--;

            if (lastChildViewPosition >= 0) {
                lastChild = getChildAt(lastChildViewPosition);
                gap = getWidth() - getPaddingRight() - getDecoratedRight(lastChild);

                maxGap = Math.max(maxGap, gap);
            }

            lastChildViewPosition--;

            if (lastChildViewPosition >= 0) {
                lastChild = getChildAt(lastChildViewPosition);
                gap = getWidth() - getPaddingRight() - getDecoratedRight(lastChild);

                maxGap = Math.max(maxGap, gap);
            }
            if (maxGap > 0) {
                dx -= maxGap;
            }
        } else {
            Log.i(TAG, "fillHorizontal: dx < 0");
            /**
             * ##  利用Rect保存子View边界
             */
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }

            for (int i = maxPos; i >= mFirstVisiPos; i--) {
                Rect rect = mItemRects.get(i);
                View child = recycler.getViewForPosition(i);
                addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                measureChildWithMargins(child, 0, 0);

                layoutDecoratedWithMargins(child, rect.left, rect.top - mHorizontalOffset, rect.right, rect.bottom - mHorizontalOffset);
            }
        }


        Log.d("TAG", "count= [" + getChildCount() + "]" + ",[recycler.getScrapList().size():" + recycler.getScrapList().size() + ", dx:" + dx + ",  mVerticalOffset" + mVerticalOffset + ", ");

        return dx;
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     *
     * @param recycler
     * @param state
     * @param dy       RecyclerView给我们的位移量，手指从下向上滑动的时候，dy > 0 ; 手指从上向下滑动的时候，dy < 0 ；
     * @return 修正以后真正的dy（可能剩余空间不够移动那么多了 所以return <|dy|）
     */
    private int fillVertical(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        //回收越界子View
        if (getChildCount() > 0) {//滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                if (dy > 0) {//需要回收从屏幕上边缘滑动出去的Views
                    if (getDecoratedBottom(child) - dy < paddingTop) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiPos++;
                    }
                } else if (dy < 0) {//需要回收从屏幕下边缘滑动出去的Views
                    if (getDecoratedTop(child) - dy > getHeight() - paddingBottom) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos--;
                    }
                }
            }
        }

        int leftOffset = getPaddingLeft();
        int lineMaxHeight = 0;
        //布局子View阶段
        if (dy >= 0) {
            int minPos = mFirstVisiPos;
            mLastVisiPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                //第一个可以添加View的position
                minPos = getPosition(lastView) + 1;
                /**
                 * 获取最后一个View的相关位置信息
                 */
                paddingTop = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            //顺序addChildView
            for (int i = minPos; i <= mLastVisiPos; i++) {
                //找recycler要一个childItemView,我们不管它是从scrap里取，还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                //计算宽度 包括margin
                if (leftOffset + getDecoratedMeasurementHorizontal(child) <= getHorizontalSpace()) {//当前行还排列的下
                    //摆放View
                    layoutDecoratedWithMargins(child, leftOffset, paddingTop, leftOffset + getDecoratedMeasurementHorizontal(child), paddingTop + getDecoratedMeasurementVertical(child));

                    //保存Rect供逆序layout用
                    Rect rect = new Rect(leftOffset, paddingTop + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), paddingTop + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                    mItemRects.put(i, rect);

                    //改变 left  lineHeight
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                } else {//当前行排列不下
                    //改变top  left  lineHeight
                    leftOffset = getPaddingLeft();
                    paddingTop += lineMaxHeight;
                    lineMaxHeight = 0;

                    //新起一行的时候要判断一下边界
                    if (paddingTop - dy > getHeight() - getPaddingBottom()) {
                        //超过了下边界，就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiPos = i - 1;
                    } else {
                        layoutDecoratedWithMargins(child, leftOffset, paddingTop, leftOffset + getDecoratedMeasurementHorizontal(child), paddingTop + getDecoratedMeasurementVertical(child));

                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, paddingTop + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), paddingTop + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);

                        //改变 left  lineHeight
                        leftOffset += getDecoratedMeasurementHorizontal(child);
                        lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    }
                }
            }
            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }

            }

        } else {
            /**
             * ##  利用Rect保存子View边界
             正序排列时，保存每个子View的Rect，逆序时，直接拿出来layout。
             */
            int maxPos = getItemCount() - 1;
            mFirstVisiPos = 0;
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                maxPos = getPosition(firstView) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiPos; i--) {
                Rect rect = mItemRects.get(i);

                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiPos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                    measureChildWithMargins(child, 0, 0);

                    layoutDecoratedWithMargins(child, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                }
            }
        }


        Log.d("TAG", "count= [" + getChildCount() + "]" + ",[recycler.getScrapList().size():" + recycler.getScrapList().size() + ", dy:" + dy + ",  mVerticalOffset" + mVerticalOffset + ", ");

        return dy;
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == RecyclerView.VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == RecyclerView.HORIZONTAL;
    }

    /**
     * @param dx       手指从右向左滑动的时候，dx > 0 ; 手指从左向右滑动的时候，dy < 0 ；
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "scrollHorizontallyBy: dx = " + dx);
        //位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }

        int realOffset = dx;//实际滑动的距离， 可能会在边界处被修复

        //边界修复代码
        if (mHorizontalOffset + realOffset < 0) {//总的滑动距离加上滑动距离小于0，说明是处于左边界，不需要滑动了应该
            //这里处理逻辑是：如果水平偏移量 mVerticalOffset 是10 ，realOffset是-20，这个时候，滑动到0就可以了，所以realOffset是 -mVerticalOffset
            realOffset = -mHorizontalOffset;
        } else if (realOffset > 0) {//如果是在右边界，手指从右向左滑动，也不应该有动作
            //利用最后一个子View比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getWidth() - getPaddingEnd() - getDecoratedRight(lastChild);
                if (gap > 0) {//说明数据量很少，没有占满整个RecyclerView，直接不需要滑动
                    realOffset = 0;
                }
            }
        }

        if (realOffset == 0) {
            return 0;
        }

        realOffset = fillHorizontal(recycler, state, realOffset);//先填充，再位移。
        mHorizontalOffset += realOffset;//累加实际滑动距离
        offsetChildrenHorizontal(-realOffset);//横向滑动

        return realOffset;
    }


    /**
     * @param dy       手指从下向上滑动的时候，dy > 0 ; 手指从上向下滑动的时候，dy < 0 ；
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.i(TAG, "scrollVerticallyBy: dy = " + dy);
        //位移0、没有子View 当然不移动
        if (dy == 0 || getChildCount() == 0) {
            return 0;
        }

        int realOffset = dy;//实际滑动的距离， 可能会在边界处被修复

        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {//总的滑动距离加上滑动距离小于0，说明是处于上边界，不需要滑动了应该
            //这里处理逻辑是 如果竖直偏移量 mVerticalOffset 是10 ，realOffset是-20，这个时候，滑动到0就可以了，所以realOffset是 -mVerticalOffset
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {//如果是在下边界，手指从下向上滑动，也不应该有动作
            //利用最后一个子View比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {//说明数据量很少，没有占满整个RecyclerView，直接不需要滑动
                    realOffset = 0;
                }
            }
        }

        if (realOffset == 0) {
            return 0;
        }

        realOffset = fillVertical(recycler, state, realOffset);//先填充，再位移。

        mVerticalOffset += realOffset;//累加实际滑动距离

        offsetChildrenVertical(-realOffset);//滑动

        return realOffset;
    }

    //模仿LLM Horizontal 源码

    /**
     * 获取某个childView在水平方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        int verticalSpace = getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
        //Log.i(TAG, "getDecoratedMeasurementVertical: verticalSpace = " + verticalSpace);
        return verticalSpace;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
