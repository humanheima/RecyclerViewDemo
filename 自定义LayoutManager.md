


RecyclerView.Recycler
```java
public final class Recycler {

    final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
    ArrayList<ViewHolder> mChangedScrap = null;

    
    //一级缓存，优先级最高
    final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

    private final List<ViewHolder>
            mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

    private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
    int mViewCacheMax = DEFAULT_CACHE_SIZE;

    //三级缓存
    RecycledViewPool mRecyclerPool;

    //二级缓存：自定义缓存
    private ViewCacheExtension mViewCacheExtension;

    static final int DEFAULT_CACHE_SIZE = 2;
    
    //...
    

}
```

Recycler中用来表示三级缓存的变量的优先级从高到低分别为：mCacheViews、mViewCacheExtension和mRecyclerPool。其中mViewCacheExtension是自定义缓存，本文不做展开，只看mCacheView和mRecyclerPool，首先需要明确的是，这两者缓存的内容都是已经不在屏幕内展示的ViewHolder。

* mCacheViews是更高效的缓存，既不需要创建ViewHolder步骤，也不需要重新绑定ViewHolder步骤，这意味着只有在数据对象完全匹配的时候，即待展示的数据Item与缓存的ViewHolder中维护的数据Item完全匹配时（ItemType与Item都相同），才会复用mCacheViews中的ViewHolder。

* mRecyclerPool中缓存的ViewHolder对象的使用条件，相较于mCacheViews要求更低，只需ItemType匹配，即可复用ViewHolder，但使用时需要重新绑定ViewHolder。


作者：快手电商无线团队
链接：https://juejin.cn/post/7044797219878223909
来源：稀土掘金
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。


参考链接

* [自定义 LayoutManager，让 RecyclerView 效果起飞](https://juejin.cn/post/7044797219878223909)
