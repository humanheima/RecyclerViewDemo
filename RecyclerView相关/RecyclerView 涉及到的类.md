
RecyclerView.Adapter : 适配器
RecyclerView.ViewHolder : 视图持有者
RecyclerView.RecyclerViewDataObserver : 数据观察者，adapter.notifyxx()方法会调用这个类的方法
AdapterHelper : 适配器助手，处理适配器更新
ChildHelper : 子视图助手，管理子视图的工具类，增加了一些隐藏子View的方法
RecyclerView.LayoutManager : 布局管理器
RecyclerView.LayoutManager.LayoutState : 布局状态，用于 LayoutManager 在填充空白空间时保持临时状态。
RecyclerView.ItemDecoration : 装饰
RecyclerView.ItemAnimator : 动画
RecyclerView.Recycler : "Recycler"负责管理已废弃（scrapped）或已脱离（detached）的项目视图，以便于重用。
```java
public final class Recycler {

    final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
    ArrayList<ViewHolder> mChangedScrap = null;

    final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

    private final List<ViewHolder>
            mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

    private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
    int mViewCacheMax = DEFAULT_CACHE_SIZE;

    RecycledViewPool mRecyclerPool;

    private ViewCacheExtension mViewCacheExtension;

    static final int DEFAULT_CACHE_SIZE = 2;
    
    //...
}
```
RecyclerView.State : "State"负责RecyclerView的状态管理。
RecyclerView.RecycledViewPool : "RecycledViewPool"允许你在多个RecyclerView之间共享视图。

RecyclerView.ViewCacheExtension : 自定义外部缓存视图的接口。只有一个get方法。
```java
public abstract static class ViewCacheExtension {

        /**
         * Returns a View that can be binded to the given Adapter position.
         * <p>
         * This method should <b>not</b> create a new View. Instead, it is expected to return
         * an already created View that can be re-used for the given type and position.
         * If the View is marked as ignored, it should first call
         * {@link LayoutManager#stopIgnoringView(View)} before returning the View.
         * <p>
         * RecyclerView will re-bind the returned View to the position if necessary.
         *
         * @param recycler The Recycler that can be used to bind the View
         * @param position The adapter position
         * @param type     The type of the View, defined by adapter
         * @return A View that is bound to the given position or NULL if there is no View to re-use
         * @see LayoutManager#ignoreView(View)
         */
        @Nullable
        public abstract View getViewForPositionAndType(@NonNull Recycler recycler, int position,
                int type);
    }
```


### 草稿


