##RecyclerView
所有关于RecyclerView 的例子都以这个工程为准
1. 下拉刷新，上拉加载
2. DiffUtil 的使用
3. 测试 Banner作为RecyclerView的HeadView使用
4. 测试RecyclerView中item的倒计时功能

RecyclerView 要能正确显示数据 需要 adapter ，layoutManager

#### ItemDecoration

```
public void addItemDecoration(ItemDecoration decor, int index) {
        if (mLayout != null) {
            mLayout.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or"
                    + " layout");
        }
        if (mItemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (index < 0) {
            mItemDecorations.add(decor);
        } else {
            mItemDecorations.add(index, decor);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }
```

`mItemDecorations`是一个`ArrayList`,我们可以为RecyclerView添加多个`ItemDecoration`。

```java
void markItemDecorInsetsDirty() {
        //获取RecyclerView的子view
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = mChildHelper.getUnfilteredChildAt(i);
            ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
        }
        mRecycler.markItemDecorInsetsDirty();
    }

```
Recycler#markItemDecorInsetsDirty()
```
void markItemDecorInsetsDirty() {
            //获取缓存的view
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                LayoutParams layoutParams = (LayoutParams) holder.itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }
```
`mInsetsDirty`字段的作用其实是一种优化性能的缓存策略，添加分割线对象时，无论是 RecyclerView 的子 view，
还是缓存的 view，都将其置为 true，接着就调用了 requestLayout 方法。

这里简单说一下 requestLayout 方法用一种责任链的方式，层层向上传递，最后传递到 ViewRootImpl，
然后重新调用 view 的 measure、layout、draw 方法来展示布局。

`mItemDecorations`的使用时机

**RecyclerView#draw(Canvas c)**

```
 @Override
    public void draw(Canvas c) {
        super.draw(c);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            //调用ItemDecoration的onDrawOver()方法
            mItemDecorations.get(i).onDrawOver(c, this, mState);
        }

        //...
    }
```
**RecyclerView#onDraw(Canvas c)**

```
@Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            //调用ItemDecoration的onDraw()方法
            mItemDecorations.get(i).onDraw(c, this, mState);
        }
    }
```

可以看到在 View 的以上两个方法中，分别调用了 ItemDecoration 对象的 onDraw(), onDrawOver() 方法。
我们继承 ItemDecoration 来覆盖这两个方法，他们区别就是 onDraw() 在 itemView 绘制之前调用，
onDrawOver() 在 itemView 绘制之后调用。

还记得刚才的 `mInsetsDirty` 字段吗？在添加分割线的时候，无论是 RecyclerView 子 View，还是缓存中的 View，
其 LayoutParams 中的 mInsetsDirty 属性，都被置为 true。 我们来解释一下这个字段的作用。

**RecyclerView#getItemDecorInsetsForChild(View child)**
```
Rect getItemDecorInsetsForChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.mInsetsDirty) {
            return lp.mDecorInsets;
        }

        if (mState.isPreLayout() && (lp.isItemChanged() || lp.isViewInvalid())) {
            // changed/invalid items should not be updated until they are rebound.
            return lp.mDecorInsets;
        }
        final Rect insets = lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        final int decorCount = mItemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            mTempRect.set(0, 0, 0, 0);
            mItemDecorations.get(i).getItemOffsets(mTempRect, child, this, mState);
            insets.left += mTempRect.left;
            insets.top += mTempRect.top;
            insets.right += mTempRect.right;
            insets.bottom += mTempRect.bottom;
        }
        lp.mInsetsDirty = false;
        return insets;
    }
```

来解释一下这段代码，首先 getItemDecorInsetsForChild 方法是在 RecyclerView 进行 measureChild 时调用的。
目的就是为了取出 RecyclerView 的 ChildView 中的分割线属性 --- 在 LayoutParams 中缓存的 mDecorInsets 。
而 mDecorInsets 就是 Rect 对象， 其记录的是所有添加分割线需要的空间累加的总和，由分割线的 `getItemOffsets` 方法影响。

最后在 measureChild 方法里，将分割线 ItemDecoration 的尺寸加入到 itemView 的 padding 中。

**RecyclerView# measureChild(View child, int widthUsed, int heightUsed)**
```
public void measureChild(View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;
            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
                    getPaddingLeft() + getPaddingRight() + widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
                    getPaddingTop() + getPaddingBottom() + heightUsed, lp.height,
                    canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }

```


但是大家都知道缓存并不是总是可用的，mInsetsDirty 这个 boolean 字段来记录它的时效性，当 mInsetsDirty 为 false 时，
说明缓存可用，直接取出可以，当 mInsetsDirty 为 true 时，说明缓存的分割线属性就需要重新计算了。

到此，关于 RecyclerView 添加分割线 ItemDecoration 的源码分析，也就基本结束了。

### ItemAnimator
**RecyclerView#setItemAnimator(ItemAnimator animator)**
```
public void setItemAnimator(ItemAnimator animator) {
        if (mItemAnimator != null) {
            mItemAnimator.endAnimations();
            mItemAnimator.setListener(null);
        }
        mItemAnimator = animator;
        if (mItemAnimator != null) {
            mItemAnimator.setListener(mItemAnimatorListener);
        }
    }
```

## LayoutManager

## Recycler

 Recycler获取ViewHolder的过程

 1. 尝试从Recycler#mChangedScrap中取得ViewHolder
 2. 尝试从Recycler#mAttachedScrap中取得ViewHolder
 3. 尝试从ChildHelper#mHiddenViews中取得ViewHolder
 4. 尝试从Recycler#mCachedViews中取得ViewHolder
 4. 尝试从Recycler#mViewCacheExtension中取得ViewHolder。RecyclerView 自身并不会实现它，一般正常的使用也用不到

 如果到这里 holder 还是为null的话,就从RecycledViewPool中获取holder
 ```
 if (holder == null){ // fallback to pool
                     if (DEBUG) {
                         Log.d(TAG, "tryGetViewHolderForPositionByDeadline("
                                 + position + ") fetching from shared pool");
                     }
                     holder = getRecycledViewPool().getRecycledView(type);
                     if (holder != null) {
                         holder.resetInternal();
                         if (FORCE_INVALIDATE_DISPLAY_LIST) {
                             invalidateDisplayListInt(holder);
                         }
                     }
  }
 ```
 RecycledViewPool 其实是一个 SparseArray 保存 ScrapData 对象的结构。
 根据 type 缓存 ViewHolder，每个 type，默认最多保存5个 ViewHolder。
 上面提到的 mCachedViews 这个集合默认最大值是 2 。

 RecycledViewPool 可以由多个 ReyclerView 共用。

 ```

 private static final int DEFAULT_MAX_SCRAP = 5;

 SparseArray<ScrapData> mScrap = new SparseArray<>();

 static class ScrapData {
             final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
             int mMaxScrap = DEFAULT_MAX_SCRAP;
             long mCreateRunningAverageNs = 0;
             long mBindRunningAverageNs = 0;
         }
 ```

 RecycledViewPool 就是缓存结构中的第四级缓存了，如果 RecycledViewPool 中依然没有
 缓存的 ViewHolder 怎么办呢？

 没办法只能调用`mAdapter.createViewHolder(RecyclerView.this, type)`创建一个

 ```
  if (holder == null) {
                     long start = getNanoTime();
                     if (deadlineNs != FOREVER_NS
                             && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
                         // abort - we have a deadline we can't meet
                         return null;
                     }
                     //创建一个viewHolder
                     holder = mAdapter.createViewHolder(RecyclerView.this, type);
                     if (ALLOW_THREAD_GAP_WORK) {
                         // only bother finding nested RV if prefetching
                         RecyclerView innerView = findNestedRecyclerView(holder.itemView);
                         if (innerView != null) {
                             holder.mNestedRecyclerView = new WeakReference<>(innerView);
                         }
                     }

                     long end = getNanoTime();
                     mRecyclerPool.factorInCreateTime(type, end - start);
                     if (DEBUG) {
                         Log.d(TAG, "tryGetViewHolderForPositionByDeadline created new ViewHolder");
                     }
                 }
             }
 ```

参考链接：

[1]: [RecyclerView 源码分析](https://www.jianshu.com/p/5f6151c1b6f8)

