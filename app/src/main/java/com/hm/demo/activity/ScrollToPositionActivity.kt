package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityScrollToTopBinding
import com.hm.demo.model.TestBean
import java.util.Random

/**
 * Created by dumingwei on 2020/10/28
 *
 * Desc: 将RecyclerView的某个item滑动到RecyclerView的中间
 */
class ScrollToPositionActivity : AppCompatActivity() {

    private val TAG: String = "ScrollToCenterActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ScrollToPositionActivity::class.java)
            context.startActivity(intent)
        }

    }

    //private val centerLayoutManager = CenterLayoutManager(this)

    private lateinit var binding: ActivityScrollToTopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollToTopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScroll.setOnClickListener {

            scrollToPosition()
        }

        binding.rv.layoutManager = LinearLayoutManager(this)

//        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
//                    mShouldScroll = false
//                    smoothMoveToPosition(rv, mToPosition);
//                }
//            }
//        })

//        rv.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    mShouldScroll = false
//                }
//            }
//            false
//        }


        binding.rv.adapter = object : BaseAdapter<TestBean>(this, getTestData()) {

            override fun getHolderType(position: Int, testBean: TestBean): Int {
                return R.layout.item_scroll_to_center
            }

            override fun bindViewHolder(holder: BaseViewHolder, testBean: TestBean, position: Int) {
                holder.setTextViewText(R.id.tv1, testBean.name)
                holder.setImageViewResource(R.id.iv, testBean.picture)
                holder.setOnItemClickListener(R.id.iv) { view, position ->
                    Toast.makeText(
                        this@ScrollToPositionActivity,
                        "onItemClick position=$position",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    private fun scrollToPosition() {
        val string = binding.etPosition.text?.toString()
        if (!string.isNullOrEmpty() && string.isNotBlank()) {
            val position = string.toInt()
            //smoothMoveToPosition(rv, position)
            binding.rv.smoothMoveToPosition(position)
        }

    }

    //目标项是否在最后一个可见项之后
    private var mShouldScroll = false

    //记录目标项位置
    private var mToPosition = 0

    /**
     * 滑动到指定位置
     */
    private fun smoothMoveToPosition(mRecyclerView: RecyclerView, position: Int) {
        // 第一个可见位置
        val firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0))
        // 最后一个可见位置
        val lastItem =
            mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.childCount - 1))
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前
            mRecyclerView.smoothScrollToPosition(position)
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后
            val movePosition = position - firstItem
            if (movePosition >= 0 && movePosition < mRecyclerView.childCount) {
                val top = mRecyclerView.getChildAt(movePosition).top
                mRecyclerView.smoothScrollBy(0, top)
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后
            mRecyclerView.smoothScrollToPosition(position)
            mToPosition = position
            mShouldScroll = true
        }
    }

    private fun getTestData(): List<TestBean>? {
        val mDatas: MutableList<TestBean> = ArrayList()
        val maxCount = 100
        val random = Random()

        val drawableList = arrayListOf<Int>()
        drawableList.add(R.drawable.pic)
        drawableList.add(R.drawable.pic_2)
        drawableList.add(R.drawable.pic_3)
        drawableList.add(R.drawable.pic_3)
        drawableList.add(R.drawable.pic_4)
        drawableList.add(R.drawable.pic_5)
        drawableList.add(R.drawable.pic_6)
        drawableList.add(R.drawable.pic_7)
        drawableList.add(R.drawable.pic_8)
        drawableList.add(R.drawable.pic_9)

        val descList: MutableList<String> = arrayListOf()

        descList.add("Android")
        descList.add("ios")
        descList.add("java")
        descList.add("kotlin")
        descList.add("c")
        descList.add("c++")
        descList.add("go")
        descList.add(".net")
        descList.add("html5")
        descList.add("js")

        for (i in 0 until maxCount) {
            val next = random.nextInt(maxCount) % 10
            mDatas.add(TestBean("dumingwei$i", descList[next], drawableList[next]))
        }
        return mDatas
    }
}