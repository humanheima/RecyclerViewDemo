### 去掉notifyItemChanged 的时候的白一下的动画效果

1. 设置 `ItemAnimator` 为 `null`，即可去掉 `notifyItemChanged` 的时候的白一下的动画效果。

2. 如果是使用默认的
   SimpleItemAnimator，可以通过设置`mRecyclerView.getItemAnimator().setChangeDuration(0);`
   来去掉`notifyItemChanged`的动画效果。
3. 如果是使用默认的
   SimpleItemAnimator，可以通过设置`mRecyclerView.getItemAnimator().supportsChangeAnimations(false);`
   来去掉`notifyItemChanged`的动画效果。

*[Android RecyclerView内部机制](https://www.jianshu.com/p/5284d6066a38)