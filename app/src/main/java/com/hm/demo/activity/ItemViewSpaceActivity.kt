package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.TestItemViewSpaceAdapter
import com.hm.demo.layoutmanager.TempLayoutManager
import com.hm.demo.model.CheckBoxModel

/**
 * Created by p_dmweidu on 2023/8/18
 * Desc: 测试 RecyclerView 的 ItemView 可见性设置为 GONE，依然会占据一定高度的问题。
 * 参考链接：https://blog.csdn.net/juer2017/article/details/124428140?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-124428140-blog-89317190.235%5Ev38%5Epc_relevant_anti_vip&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-124428140-blog-89317190.235%5Ev38%5Epc_relevant_anti_vip&utm_relevant_index=5
 *
 * 原因：
 * 1. 在RecyclerView 的 LayoutManager measure ItemView 的时候，无论是否 ItemView 是否可见都会来测量出 ItemView 的高度。
 * 2. 其他ViewGroup 在测量子View的时候，如果子View的可见性为 GONE，那么就不会测量子View的高度。比如FrameLayout。
 *
 * 验证：
 * 2. 自定义LayoutManager，重写 measureChildWithMargins 方法，打印出 ItemView 的高度，发现即使 ItemView 的可见性为 GONE，也会打印出 ItemView 的高度。
 * 3. 然后 重写 measureChildWithMargins 方法，如果发现 ItemView 可见性为 GONE的时候，不测量  ItemView ，那么 ItemView 的高度就为 0。（不考虑复用，如果复用的话，可能还会有问题，没有验证。）
 * 4. measureChildWithMargins方法，是在{Adapter#onBindViewHolder}之前的，在 onBindViewHolder 方法里面设置 ItemView 的高度为 GONE是不起作用的。
 *
 * 解决：
 * 1. 通过在{Adapter#onBindViewHolder}设置 ItemView 的高度为 0，可以解决问题。
 * 2. 将ItemView 的直接子布局设置为Visible.GONE。
 */
class ItemViewSpaceActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var rv2: RecyclerView


    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, ItemViewSpaceActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_view_space)
        rv = findViewById(R.id.rv_theory)
        rv2 = findViewById(R.id.rv_theory2)

        initRv()
        initRv2()

    }

    private fun initRv() {
        rv.layoutManager = LinearLayoutManager(this)
        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 3) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        rv.adapter = TestItemViewSpaceAdapter(this, arrayList)
    }

    private fun initRv2() {
        rv2.layoutManager = TempLayoutManager(this)
        val arrayList2 = arrayListOf<CheckBoxModel>()
        for (i in 0 until 3) {
            arrayList2.add(CheckBoxModel("Hello$i", false))
        }
        rv2.adapter = TestItemViewSpaceAdapter(this, arrayList2)
    }

}