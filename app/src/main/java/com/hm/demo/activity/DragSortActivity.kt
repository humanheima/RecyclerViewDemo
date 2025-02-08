package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityScrollToCenterBinding
import com.hm.demo.model.TestBean
import java.util.Collections

/**
 * Created by p_dmweidu on 2025/2/8
 * Desc: 测试 RecyclerView 拖动排序
 */
class DragSortActivity : AppCompatActivity() {


    companion object {

        const val TAG = "DragSortActivity"

        fun launch(context: Context) {
            val intent = Intent(context, DragSortActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityScrollToCenterBinding

    private val testData = getTestData()

    private val centerLayoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollToCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rv.layoutManager = centerLayoutManager
        binding.rv.adapter = object : BaseAdapter<TestBean>(this, testData) {

            override fun getHolderType(position: Int, testBean: TestBean): Int {
                return R.layout.item_scroll_to_center
            }

            override fun bindViewHolder(holder: BaseViewHolder, testBean: TestBean, position: Int) {
                holder.setTextViewText(R.id.tv1, testBean.name)
                holder.setImageViewResource(R.id.iv, testBean.picture)
                holder.setOnItemClickListener(R.id.iv) { view, position ->
                    Toast.makeText(
                        this@DragSortActivity,
                        "onItemClick position=$position",
                        Toast.LENGTH_SHORT
                    ).show()
                    centerLayoutManager.smoothScrollToPosition(
                        binding.rv,
                        RecyclerView.State(),
                        position
                    )
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                //左右滑动删除
                val swipeFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(
                    dragFlag, swipeFlag
                )

//                return makeMovementFlags(
//                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//                )

            }

            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPos: Int,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int
            ) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.absoluteAdapterPosition
                val toPosition = target.absoluteAdapterPosition

                Collections.swap(testData, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //侧滑删除
                val position = viewHolder.absoluteAdapterPosition
                testData?.removeAt(position)
                binding.rv.adapter?.notifyItemRemoved(position)
            }

        })

        itemTouchHelper.attachToRecyclerView(binding.rv)
    }

    private fun getTestData(): MutableList<TestBean>? {
        val mDatas: MutableList<TestBean> = ArrayList()
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "beiguo", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "最后一个", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "beiguo", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "最后一个", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "beiguo", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "最后一个", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "beiguo", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "最后一个", R.drawable.pic_5))

        mDatas.forEachIndexed { index, testBean ->
            testBean.name = "测试 index = ${index}"
        }
        return mDatas
    }
}