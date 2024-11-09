# DiffUtil 和 payload 的关系

DiffUtil 和 payload 在 Android 的 RecyclerView
中都是用来优化列表更新的工具，但它们的使用场景和工作机制有所不同。下面我们详细说明这两者的关系以及如何结合使用。

## DiffUtil

DiffUtil 是一个工具类，用于计算两个列表之间的差异，主要用于高效更新 RecyclerView 中的数据。当你在数据集发生变化时，使用
DiffUtil 可以减少不必要的视图绑定和更新，从而优化性能。

## Payload

payload 是 RecyclerView.Adapter 中 onBindViewHolder 方法的一个参数，用于支持局部更新。当你只需要更新列表项的部分内容时，可以使用
payload 来提高性能。

