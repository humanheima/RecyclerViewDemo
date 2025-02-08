# RecyclerView 使用 ItemTouchHelper.Callback 实现拖动排序和侧滑删除

要在 `RecyclerView` 中同时实现 **拖动排序** 和 **侧滑删除**，可以通过扩展 `ItemTouchHelper.Callback` 来实现。以下是完整的实现步骤：

---

### 1. 修改 `ItemTouchHelper.Callback`
在 `SimpleItemTouchHelperCallback` 中，启用侧滑删除功能，并实现侧滑删除的逻辑。

```java
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemAdapter adapter;

    public SimpleItemTouchHelperCallback(ItemAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // 允许长按拖动
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true; // 允许侧滑删除
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN; // 允许上下拖动
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END; // 允许左右侧滑
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 拖动排序的逻辑
        adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 侧滑删除的逻辑
        adapter.removeItem(viewHolder.getAdapterPosition());
    }
}
```

---

### 2. 在 Adapter 中添加删除方法
在 `ItemAdapter` 中添加一个方法，用于删除指定位置的 item：

```java
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemName.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // 拖动排序的方法
    public void moveItem(int fromPosition, int toPosition) {
        Item fromItem = itemList.get(fromPosition);
        itemList.remove(fromPosition);
        itemList.add(toPosition, fromItem);
        notifyItemMoved(fromPosition, toPosition);
    }

    // 侧滑删除的方法
    public void removeItem(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
        }
    }
}
```

---

### 3. 将 ItemTouchHelper 附加到 RecyclerView
在 `Activity` 或 `Fragment` 中，将 `ItemTouchHelper` 附加到 `RecyclerView`：

```java
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item("Item 1"));
        itemList.add(new Item("Item 2"));
        itemList.add(new Item("Item 3"));
        itemList.add(new Item("Item 4"));

        adapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(adapter);

        // 创建 ItemTouchHelper 并附加到 RecyclerView
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
```

---

### 4. 运行效果
- **拖动排序**：长按 item 并上下拖动，可以调整 item 的顺序。
- **侧滑删除**：向左或向右滑动 item，可以将其删除。

---

### 5. 可选：添加删除动画
如果你希望在删除 item 时添加动画效果，可以在 `Adapter` 的 `removeItem` 方法中调用 `notifyItemRemoved(position)`，这会自动触发默认的删除动画。

---

### 6. 总结
通过 `ItemTouchHelper`，我们可以轻松实现 `RecyclerView` 的 **拖动排序** 和 **侧滑删除** 功能。关键点在于：
1. 在 `ItemTouchHelper.Callback` 中启用拖动和侧滑。
2. 在 `Adapter` 中实现 `moveItem` 和 `removeItem` 方法。
3. 将 `ItemTouchHelper` 附加到 `RecyclerView`。

这种方式非常灵活，适用于大多数列表交互场景。