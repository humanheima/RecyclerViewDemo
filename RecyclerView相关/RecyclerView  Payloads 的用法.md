1. 首先复写 RecyclerView.Adapter 的 三个参数的 onBindViewHolder 方法，内部不再调用 两个参数的
   onBindViewHolder 方法。

```java
public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
    onBindViewHolder(holder, position);
}
```

自定义适配器 PayloadTestAdapter(private val items: List<PayloadBean>) : RecyclerView.Adapter<
PayloadTestAdapter.MyViewHolder>()

复写后的方法：

```kotlin

override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: List<Any>) {
    Log.d(TAG, "onBindViewHolder: ${Log.getStackTraceString(Throwable())}")
    if (payloads.isNotEmpty()) {
        Log.d(TAG, "onBindViewHolder: payloads = $payloads  position = $position")
        // 处理 payloads
        for (payload in payloads) {
            if (payload is String) {
                holder.textView1.text = payload
            }
        }
    } else {
        Log.d(TAG, "onBindViewHolder: 全量更新 position = $position")
        // 全量更新
        val item = items[position]
        holder.textView1.text = item.text1
        holder.textView2.text = item.text2
    }
}

```

### Payloads 是怎么来的？

```kotlin
/**
 * 使用 payload notifyItemChanged，只更新部分View
 */
fun updateItemTextPayload(position: Int, newText: String) {
    //注释1处，注意，这里数据也要变化
    items[position].text1 = newText
    //注释2处，使用 payload 更新
    notifyItemChanged(position, newText)
}
```

调用 notifyItemChanged 方法时，会传入一个 payload 参数，payload 就是需要更新的部分。


有了 payload，notifyItemChanged 的时候。只是把 View  detach 了，然后又 attach 了而已。

```java
void scrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            //注释1处，canReuseUpdatedViewHolder(holder) 条件满足
            if (holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID)
                    || !holder.isUpdated() || canReuseUpdatedViewHolder(holder)) {
                if (holder.isInvalid() && !holder.isRemoved() && !mAdapter.hasStableIds()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view."
                            + " Invalid views cannot be reused from scrap, they should rebound from"
                            + " recycler pool." + exceptionLabel());
                }
                holder.setScrapContainer(this, false);
                //添加到 mAttachedScrap
                mAttachedScrap.add(holder);
            } else {
                if (mChangedScrap == null) {
                    mChangedScrap = new ArrayList<ViewHolder>();
                }
                holder.setScrapContainer(this, true);
                mChangedScrap.add(holder);
            }
        }
```

注释1处，canReuseUpdatedViewHolder(holder) 条件满足，因为 payloads 不为null，添加到 mAttachedScrap。 真正布局的时候，就是从新 mAttachedScrap 中取出来 attach 一下而已。

DefaultItemAnimator 的动画

```java
 public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                              RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
    if (oldHolder == newHolder) {
        //注释1处，有payload的情况，不会执行透明度变化动画，没有闪一下的效果
        // Don't know how to run change animations when the same view holder is re-used.
        // run a move animation to handle position changes.
        return animateMove(oldHolder, fromLeft, fromTop, toLeft, toTop);
    }
    /注释2处，没有payload的情况，会执行透明度变化动画
    final float prevTranslationX = oldHolder.itemView.getTranslationX();
    final float prevTranslationY = oldHolder.itemView.getTranslationY();
    final float prevAlpha = oldHolder.itemView.getAlpha();
    resetAnimation(oldHolder);
    int deltaX = (int) (toLeft - fromLeft - prevTranslationX);
    int deltaY = (int) (toTop - fromTop - prevTranslationY);
    // recover prev translation state after ending animation
    oldHolder.itemView.setTranslationX(prevTranslationX);
    oldHolder.itemView.setTranslationY(prevTranslationY);
    oldHolder.itemView.setAlpha(prevAlpha);
    if (newHolder != null) {
        // carry over translation values
        resetAnimation(newHolder);
        newHolder.itemView.setTranslationX(-deltaX);
        newHolder.itemView.setTranslationY(-deltaY);
        newHolder.itemView.setAlpha(0);
    }
    mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop));
    return true;
}
```

注释1处，有payload的情况，不会执行透明度变化动画，没有闪一下的效果。

注释2处，没有payload的情况，会执行透明度变化动画，会闪一下。