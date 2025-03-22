
### 草稿


dispatchLayoutStep1 方法中，会调用 `mLayouManager.onLayoutChildren` 方法。这个方法是干嘛的呢？

看下来，感觉对 onItemChanged，onItemInserted 来说应该没有用。对 onItemRemoved 有用。在预布局的时候，layout 把屏幕之外的 ViewHolder。这样在后面动画的时候
才能执行，屏幕外的 ViewHolder 进入屏幕的动画效果。



### 去掉 notifyItemChanged 的时候的白一下的动画效果

有动画的时候，是 removeOldView , addNewView 

没有动画的时候，是 detachOldView，attachOldView

1. 设置 `ItemAnimator` 为 `null`，即可去掉 `notifyItemChanged` 的时候的白一下的动画效果。

2. 如果是使用默认的
   SimpleItemAnimator，可以通过设置`mRecyclerView.getItemAnimator().setChangeDuration(0);`
   来去掉`notifyItemChanged`的动画效果。
3. 如果是使用默认的
   SimpleItemAnimator，可以通过设置`mRecyclerView.getItemAnimator().supportsChangeAnimations(false);`
   来去掉`notifyItemChanged`的动画效果。

第3种方法的原理是动画前后的两个ViewHolder是同一个对象，不会有动画效果。

oldViewHolder 存储在 mViewInfoStore 中


```java
//动画前的ViewHolder
ViewHolder{f491949 position=1 id=-1, oldPos=-1, pLpos:-1 update}
```

 在 dispatchLayoutStep1 的时候， Recycler scrapView 的时候，会加入到mAttachedScrap 

```java
void scrapView(View view) {
    final ViewHolder holder = getChildViewHolderInt(view);
    if(holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID) || !holder.isUpdated() || canReuseUpdatedViewHolder(holder)) {
        if(holder.isInvalid() && !holder.isRemoved() && !mAdapter.hasStableIds()) {
            throw new IllegalArgumentException("Called scrap view with an invalid view." + " Invalid views cannot be reused from scrap, they should rebound from" + " recycler pool." + exceptionLabel());
        }
        holder.setScrapContainer(this, false);
        //注释1处，设置  SimpleItemAnimator.supportsChangeAnimations = false，会走到这里
        mAttachedScrap.add(holder);
    } else {
        if(mChangedScrap == null) {
            mChangedScrap = new ArrayList < ViewHolder > ();
        }
        holder.setScrapContainer(this, true);
        mChangedScrap.add(holder);
    }
}

```

在我们这个例子中，设置`mRecyclerView.getItemAnimator().supportsChangeAnimations(false);`
来去掉`notifyItemChanged`的动画效果。canReuseUpdatedViewHolder 会返回true，为 true 会加入  mAttachedScrap ，否则加入 mChangedScrap


在 dispatchLayoutStep1 fill过程中，重新找到ViewHolder的时候，Recycler 的 getScrapOrHiddenOrCachedHolderForPosition 方法会找到这个ViewHolder

```java
ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
    final int scrapCount = mAttachedScrap.size();

    // Try first for an exact, non-invalid match from scrap.
    for(int i = 0; i < scrapCount; i++) {
        final ViewHolder holder = mAttachedScrap.get(i);
        if(!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
            holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
            //注释1处，这里会返回同一个ViewHolder
            return holder;
        }
    }
}
```

不支持动画的时候，holder.isTmpDetached()为true。只是简单的 detach 和 attach。 `mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);`。

RecyclerView.LayoutManager 的 addViewInt(View child, int index, boolean disappearing) 方法。

```java
private void addViewInt(View child, int index, boolean disappearing) {
    final ViewHolder holder = getChildViewHolderInt(child);
    if(disappearing || holder.isRemoved()) {
        // these views will be hidden at the end of the layout pass.
        mRecyclerView.mViewInfoStore.addToDisappearedInLayout(holder);
    } else {
        // This may look like unnecessary but may happen if layout manager supports
        // predictive layouts and adapter removed then re-added the same item.
        // In this case, added version will be visible in the post layout (because add is
        // deferred) but RV will still bind it to the same View.
        // So if a View re-appears in post layout pass, remove it from disappearing list.
        mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(holder);
    }
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    if(holder.wasReturnedFromScrap() || holder.isScrap()) {
        if(holder.isScrap()) {
            holder.unScrap();
        } else {
            holder.clearReturnedFromScrapFlag();
        }
        //从 mChangedScrap 或者 mAttachedScrap 中的View，会走到这里，重新添加到RecyclerView上
        mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);
        if(DISPATCH_TEMP_DETACH) {
            ViewCompat.dispatchFinishTemporaryDetach(child);
        }
    } else if(child.getParent() == mRecyclerView) { // it was not a scrap but a valid child
        // ensure in correct position
        //View 可用，但位置变化了，移动到正确的位置
        int currentIndex = mChildHelper.indexOfChild(child);
        if(index == -1) {
            index = mChildHelper.getChildCount();
        }
        if(currentIndex == -1) {
            throw new IllegalStateException("Added View has RecyclerView as parent but" + " view is not a real child. Unfiltered index:" + mRecyclerView.indexOfChild(child) + mRecyclerView.exceptionLabel());
        }
        if(currentIndex != index) {
            mRecyclerView.mLayout.moveView(currentIndex, index);
        }
    } else {
        //添加View
        mChildHelper.addView(child, index, false);
        lp.mInsetsDirty = true;
        if(mSmoothScroller != null && mSmoothScroller.isRunning()) {
            mSmoothScroller.onChildAttachedToWindow(child);
        }
    }
    if(lp.mPendingInvalidate) {
        if(sVerboseLoggingEnabled) {
            Log.d(TAG, "consuming pending invalidate on child " + lp.mViewHolder);
        }
        holder.itemView.invalidate();
        lp.mPendingInvalidate = false;
    }
}
```


```java
private void animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo,
    boolean oldHolderDisappearing, boolean newHolderDisappearing) {
    oldHolder.setIsRecyclable(false);
    if(oldHolderDisappearing) {
        addAnimatingView(oldHolder);
    }
    if(oldHolder != newHolder) {
        //注释1处，同样的ViewHolder，不会有动画效果
        if(newHolderDisappearing) {
            addAnimatingView(newHolder);
        }
        oldHolder.mShadowedHolder = newHolder;
        // old holder should disappear after animation ends
        addAnimatingView(oldHolder);
        mRecycler.unscrapView(oldHolder);
        newHolder.setIsRecyclable(false);
        newHolder.mShadowingHolder = oldHolder;
    }
    //注释2处，这里也不会执行动画
    if(mItemAnimator.animateChange(oldHolder, newHolder, preInfo, postInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，同样的ViewHolder，不会有动画效果。注释2处，因为translationX 和 translationY 都为0，这里也不会执行动画。


### 支持动画的时候

在 dispatchLayoutStep1 的时候， Recycler scrapView 的时候，会加入到 mChangedScrap 中 。然后在 fill 的过程中，会从 mChangedScrap 中找到ViewHolder，然后添加完 ViewHolder 后，会从 mChangedScrap 中移除。

Recycler 的 unscrapView 方法

```java
//Note: 看样在预布局过程中，都会把 ViewHolder 从 mChangedScrap 或者 mAttachedScrap 中移除
void unscrapView(ViewHolder holder) {
    if(holder.mInChangeScrap) {
        mChangedScrap.remove(holder);
    } else {
        mAttachedScrap.remove(holder);
    }
    holder.mScrapContainer = null;
    holder.mInChangeScrap = false;
    holder.clearReturnedFromScrapFlag();
}
```


在 dispatchLayoutStep2 的时候， Recycler scrapView 的时候，还会加入到 mChangedScrap 中 。

dispatchLayoutStep2 过程 fill 的时候， 获取 ViewHolder Recycler 的 tryGetViewHolderForPositionByDeadline 方法

```java
 @Nullable
 ViewHolder tryGetViewHolderForPositionByDeadline(int position,
     boolean dryRun, long deadlineNs) {
     if(position < 0 || position >= mState.getItemCount()) {
         throw new IndexOutOfBoundsException("Invalid item position " + position + "(" + position + "). Item count:" + mState.getItemCount() + exceptionLabel());
     }
     boolean fromScrapOrHiddenOrCache = false;
     ViewHolder holder = null;
     // 0) If there is a changed scrap, try to find from there
     if(mState.isPreLayout()) {
         //注释1处，预布局的时候，才会从 mChangedScrap 中去找 
         holder = getChangedScrapViewForPosition(position);
         fromScrapOrHiddenOrCache = holder != null;
     }
     //...
     //注释2处，最终会创建一个新的ViewHolder   
 }
 
```

注释1处，预布局的时候，才会从 mChangedScrap 中去找，在 dispatchLayoutStep2 过程中，虽然 mChangedScrap 有存储的ViewHolder ,但是没从 mChangedScrap 中找到ViewHolder，最终会创建一个新的ViewHolder。

fill 内部会把 View 添加到 RecyclerView 上。

有了新旧不同的ViewHolder，就会执行动画。


```java
private void animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo,
    boolean oldHolderDisappearing, boolean newHolderDisappearing) {
    oldHolder.setIsRecyclable(false);
    if(oldHolderDisappearing) {
        addAnimatingView(oldHolder);
    }
    if(oldHolder != newHolder) {
        //注释1处，不同的的ViewHolder，会有动画效果
        if(newHolderDisappearing) {
            addAnimatingView(newHolder);
        }
        oldHolder.mShadowedHolder = newHolder;
        // old holder should disappear after animation ends
        //注释2处，动画开始前，这里会把  mChangedScrap 中存储的ViewHolder 从 mChangedScrap 中移除，对应的View 会添加到 ChildHelper.mHiddenViews 中
        addAnimatingView(oldHolder);
        mRecycler.unscrapView(oldHolder);
        newHolder.setIsRecyclable(false);
        newHolder.mShadowingHolder = oldHolder;
    }
    if(mItemAnimator.animateChange(oldHolder, newHolder, preInfo, postInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，不同的的ViewHolder，会有动画效果。
注释2处，动画开始前，这里会把  mChangedScrap 中存储的ViewHolder 从 mChangedScrap 中移除，对应的View 会添加到 ChildHelper.mHiddenViews 中。

在动画结束后

```java
boolean removeAnimatingView(View view) {
    startInterceptRequestLayout();
    //注释1处，会从 mHiddenViews 把 老的 View 删除。也会从RecyclerView 移除。`RecyclerView.this.removeViewAt(index);`。
    final boolean removed = mChildHelper.removeViewIfHidden(view);
    if(removed) {
        final ViewHolder viewHolder = getChildViewHolderInt(view);
        //注释2处，这里再次调用，但，mChangedScrap 中已经没有了
        mRecycler.unscrapView(viewHolder);
        //注释3处，
        mRecycler.recycleViewHolderInternal(viewHolder);
        if(sVerboseLoggingEnabled) {
            Log.d(TAG, "after removing animated view: " + view + ", " + this);
        }
    }
    // only clear request eaten flag if we removed the view.
    stopInterceptRequestLayout(!removed);
    return removed;
}
```

注释1处，会从 mHiddenViews 把 老的 View 删除。也会从RecyclerView 移除。`RecyclerView.this.removeViewAt(index);`。
注释2处，这里注意一下，这里再次调用 unscrapView。但，mChangedScrap 中已经没有了老的ViewHolder了。
注释3处，会把 老的 ViewHolder ，根据 ItemViewType，回收到 RecycledViewPool 中，一种 ItemViewType 默认缓存5个。最后也会从 mViewInfoStore 删除 老的 ViewHolder 信息。

为什么不回收到 mCachedViews呢？ 因为 有标志位，`ViewHolder.FLAG_UPDATE` 会被设置，所以不会回收到 mCachedViews 中。



### notifyItemChanged 的时候，数据变了，是新的ViewHolder，还是旧的ViewHolder？

* 支持动画的时候，无论数据变没变，都是新的ViewHolder。老的 ViewHolder 是放在 mChangedScrap 中的。但是在 dispatchLayoutStep2 方法中，不会从 mChangedScrap 找，所以会创建新的 ViewHolder。
* 不支持动画的时候，无论数据变没变，都是同一个ViewHolder，都是从 mAttachedScrap 中取出来的，会重新绑定数据。




### 点击的时候，改变了ViewHolder中部分View的状态，是否要调用notifyItemChanged()？ 


在使用 RecyclerView 时，如果你在 ViewHolder 中更改了某个 View 的状态（例如，改变颜色、显示/隐藏某些视图等），通常不需要调用 notifyItemChanged()，因为你并没有改变数据集本身，只是更新了视图的状态。

但是，如果你的更新是基于数据变化的（例如，你更改了与 ViewHolder 相关联的数据），那么调用 notifyItemChanged(position) 是合适的。这样会确保 RecyclerView 能够正确地更新视图，并且在需要时重新绑定数据。

总结：
只更新视图状态：不需要调用 notifyItemChanged()。
更新数据：应调用 notifyItemChanged(position)。
确保你在更新时考虑到性能和用户体验，避免不必要的重绘。

### RecyclerView 中 ChildHelper#mHiddenViews 是干什么用的？

回答话术：就这样回答。
当用户删除某一项时，ChildHelper 会将该子 View 暂时隐藏（存入 mHiddenViews）。 执行删除动画期间，该 View 仍存在于父容器中，但不可见。 动画结束后，RecyclerView 会真正移除该 View，并触发数据更新。
这个待研究

示例场景
假设 RecyclerView 需要执行一个删除项的动画：

当用户删除某一项时，ChildHelper 会将该子 View 暂时隐藏（存入 mHiddenViews）。

执行删除动画期间，该 View 仍存在于父容器中，但不可见。

动画结束后，RecyclerView 会真正移除该 View，并触发数据更新。


`mHiddenViews` 是 `RecyclerView` 中用于支持动画效果的重要机制之一。它的主要作用是临时存储被隐藏的 `View`，以便在动画执行期间保持布局的稳定性，并在动画结束后正确地处理这些 `View`。通过 `mHiddenViews`，`RecyclerView` 能够实现平滑的动画过渡效果，同时避免因 `View` 突然消失或出现导致的性能问题。

如果你对 `RecyclerView` 的动画机制或源码实现有更深入的兴趣，可以进一步研究 `RecyclerView.ItemAnimator` 和 `RecyclerView.Recycler` 的相关源码。


### RecyclerView 为什么会在 onMeasure 的时候，调用 dispatchLayoutStep1(); 和 dispatchLayoutStep2(); 呢?

在 RecyclerView 宽高不确定的情况下，比如都是WRAP_CONTENT，那么在 onMeasure 的时候，会调用 dispatchLayoutStep1(); 和 dispatchLayoutStep2(); 获取子View的宽高。然后累加子View的宽高设置setMeasuredDimension。


### 各级缓存的作用


|缓存级别|实际变量| 含义                                                                                                                                                              |
|--------|-----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
|一级缓存|`mAttachedScrap`和`mChangedScrap`| 优先级最高的缓存，RecyclerView在获取ViewHolder时,优先会到这两个缓存来找。其中mChangedScrap存储的是数据被更新的ViewHolder,比如说调用了Adapter的notifyItemChanged方法。 这样的ViewHolder有标记位 ViewHolder.FLAG_UPDATE |
|二级缓存|`mCachedViews`| 默认大小为2，在滚动的时候会存储一些ViewHolder。                                                                                                                                   |
|三级缓存|`ViewCacheExtension`| 这个是自定义缓存，一般用不到。                                                                                                                                                 |
|四级缓存|`RecyclerViewPool`| 根据ViewType来缓存ViewHolder，每个ViewType的数组大小默认为5，可以动态的改变。                                                                                                            |


