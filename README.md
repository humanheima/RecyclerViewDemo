##RecyclerView
所有关于RecyclerView 的例子都以这个工程为准
1. 下拉刷新，上拉加载
2. DiffUtil 的使用
3. 测试 Banner作为RecyclerView的HeadView使用
4. 测试RecyclerView中item的倒计时功能

RecyclerView 要能正确显示数据 需要 adapter ，layoutManager

```java
void markItemDecorInsetsDirty() {
        //获取RecyclerView的子view数量
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = mChildHelper.getUnfilteredChildAt(i);
            ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
        }
        mRecycler.markItemDecorInsetsDirty();
    }

```