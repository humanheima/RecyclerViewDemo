package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.SimpleAdapterAdapter
import com.hm.demo.databinding.ActivityRecyclerScrollRelateBinding
import com.hm.demo.model.CheckBoxModel

/**
 * Created by p_dmweidu on 2024/1/17
 * Desc: 测试RecyclerView的是否滚动到了底部
 * 1. 正常的从上到下布局。
 * 2. 从下到上布局。
 */
class RecyclerScrollRangeRelateActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    private lateinit var binding: ActivityRecyclerScrollRelateBinding

    private val arrayList = arrayListOf<CheckBoxModel>()

    private var count = 1

    companion object {

        private const val TAG = "RecyclerScrollRangeRela"

        fun launch(context: Context) {
            val intent = Intent(context, RecyclerScrollRangeRelateActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerScrollRelateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInsertItem.setOnClickListener {
//            val tempList = arrayListOf<CheckBoxModel>()
//            tempList.add(CheckBoxModel("新增Hello1", false))
//            tempList.add(CheckBoxModel("新增Hello2", false))
//            arrayList.addAll(0, tempList)
//            rv.adapter?.notifyItemRangeInserted(0, tempList.size)
//            rv.adapter?.notifyItemChanged(0)

//            arrayList.add(0, CheckBoxModel("新增Hello$count", false))
//            rv.adapter?.notifyItemInserted(0)
//            rv.scrollToPosition(0)

            arrayList.add(CheckBoxModel("新增Hello$count", false))
            rv.adapter?.notifyItemInserted(arrayList.size)
            rv.scrollToPosition(arrayList.size)
            count++

        }
        binding.btnGetScrollInfo.setOnClickListener {
//            val scrollOffset = rv.computeVerticalScrollOffset()
//            val computeVerticalScrollExtent = rv.computeVerticalScrollExtent()
//            val scrollRange = rv.computeVerticalScrollRange()
//            val height = rv.height
//            Log.i(
//                TAG,
//                "onCreate: computeVerticalScrollExtent = $computeVerticalScrollExtent ， scrollOffset = $scrollOffset ， scrollRange = $scrollRange  , height = $height"
//            )
            Log.i(TAG, "onCreate: isSlideToBottom = ${isSlideToBottom(rv)}")
            if (displayShowBack(rv)) {
                binding.tvBackToLatest.visibility = RecyclerView.VISIBLE
            } else {
                binding.tvBackToLatest.visibility = RecyclerView.GONE
            }
        }
        rv = findViewById(R.id.rv_theory)

        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        for (i in 0 until count) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        //arrayList.add(CheckBoxModel("等闲识得东风面，\n万紫千红总是春", false))
        rv.adapter = SimpleAdapterAdapter(this, arrayList)

    }

    /**
     * 何时展示回到最新的按钮呢？定义一个逻辑
     * 1. 没有到达底部，并且距离底部超过控件的高度就显示
     */
    private fun displayShowBack(recyclerView: RecyclerView?): Boolean {

        if (recyclerView == null) return false
        //View的高度
        val scrollExtent = recyclerView.computeVerticalScrollExtent()

        //竖直方向上滚动的距离
        val scrollOffset = recyclerView.computeVerticalScrollOffset()

        //整个View控件的高度。
        val scrollRange = recyclerView.computeVerticalScrollRange()

        val height = recyclerView.height
        Log.i(
            TAG,
            "scrollExtent = $scrollExtent , scrollOffset = $scrollOffset , scrollRange = $scrollRange , height = $height"
        )
        //距离可以滚动到的底部的距离
        val distanceToBottom = (scrollRange - (scrollExtent + scrollOffset))
        Log.i(TAG, "displayShowBack: distanceToBottom = $distanceToBottom")
        return distanceToBottom >= height

    }

    private fun isSlideToBottom(recyclerView: RecyclerView?): Boolean {
        if (recyclerView == null) return false
        //View的高度
        val scrollExtent = recyclerView.computeVerticalScrollExtent()

        //竖直方向上滚动的距离
        val scrollOffset = recyclerView.computeVerticalScrollOffset()

        //整个View控件的高度。
        val scrollRange = recyclerView.computeVerticalScrollRange()

        Log.i(
            TAG,
            "scrollExtent = $scrollExtent , scrollOffset = $scrollOffset , scrollRange = $scrollRange , height = ${recyclerView.height}"
        )
        return (scrollExtent + scrollOffset >= scrollRange)
    }

}


