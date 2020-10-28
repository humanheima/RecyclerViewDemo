```
/**
 * A Callback class used by DiffUtil while calculating the diff between two lists.
 */
    public abstract static class Callback {
        /**
         * Returns the size of the old list.
         *
         * @return The size of the old list.
         */
        public abstract int getOldListSize();

        /**
         * Returns the size of the new list.
         *
         * @return The size of the new list.
         */
        public abstract int getNewListSize();

        /**
         * Called by the DiffUtil to decide whether two object represent the same Item.
         * <p>
         * For example, if your items have unique ids, this method should check their id equality.
         *
         * @param oldItemPosition The position of the item in the old list
         * @param newItemPosition The position of the item in the new list
         * @return True if the two items represent the same object or false if they are different.
         */
        public abstract boolean areItemsTheSame(int oldItemPosition, int newItemPosition);

        /**
         * Called by the DiffUtil when it wants to check whether two items have the same data.
         * DiffUtil uses this information to detect if the contents of an item has changed.
         * <p>
         * DiffUtil uses this method to check equality instead of {@link Object#equals(Object)}
         * so that you can change its behavior depending on your UI.
         * For example, if you are using DiffUtil with a
         * {@link android.support.v7.widget.RecyclerView.Adapter RecyclerView.Adapter}, you should
         * return whether the items' visual representations are the same.
         * <p>
         * This method is called only if {@link #areItemsTheSame(int, int)} returns
         * {@code true} for these items.
         *
         * @param oldItemPosition The position of the item in the old list
         * @param newItemPosition The position of the item in the new list which replaces the
         *                        oldItem
         * @return True if the contents of the items are the same or false if they are different.
         */
        public abstract boolean areContentsTheSame(int oldItemPosition, int newItemPosition);

        /**
         * 比较两个item的时候，如果 {@link #areItemsTheSame(int, int)} 方法返回 true 并且 
         * {@link #areContentsTheSame(int, int)} 方法返回 false，DiffUtil调用此方法以获取两个item之间更改的有效负载（payload）。
         * 我的理解是这样的，比如item有5个字段都参与比较，比如两者之间有3个字段不一样，那他们之间更改的payload就是这个3个字段。
         * <p>
         * 例如，如果你配合 {@link RecyclerView} 使用 DiffUtil, 你可以返回item中改变的字段，那么你的
         * {@link android.support.v7.widget.RecyclerView.ItemAnimator ItemAnimator} 可以使用那些信息来运行正确的动画。
         * <p>
         * 默认实现返回null。
         *
         * @param oldItemPosition item在老的列表中的位置。
         * @param newItemPosition item在新的列表中的位置。
         *
         * @return 返回一个 payload 对象来代表两个item之间的改变。
         */
        @Nullable
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return null;
        }
    }
```