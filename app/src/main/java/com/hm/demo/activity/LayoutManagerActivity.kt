package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hm.demo.R
import com.hm.demo.Util.ScreenUtil
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityLayoutBinding
import com.hm.demo.model.TestBean

/**
 * Created by dumingwei on 2020/6/9
 *
 * Desc: 测试RecyclerView的各种 layout manager
 */
class LayoutManagerActivity : BaseRecyclerViewAdapterActivity() {

    private var adapter: BaseAdapter<TestBean>? = null

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, LayoutManagerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = object : BaseAdapter<TestBean>(this, getTestData()) {

            override fun getHolderType(position: Int, testBean: TestBean): Int {
                return R.layout.item_diff
            }

            override fun bindViewHolder(holder: BaseViewHolder, testBean: TestBean, position: Int) {
                if (holder.itemViewType == R.layout.item_diff) {
                    holder.setTextViewText(R.id.tv1, testBean.name)
                    holder.setTextViewText(R.id.tv2, testBean.desc)
                    holder.setImageViewResource(R.id.iv, testBean.picture)
                    holder.setOnItemClickListener(R.id.iv) { view, position ->
                        Toast.makeText(
                            this@LayoutManagerActivity,
                            "onItemClick position=$position", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

//        binding.rvLayout.layoutManager = androidx.recyclerview.widget.GridLayoutManager(
//            this,
//            2,
//            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
//            false
//        )

        binding.rvLayout.layoutManager = LinearLayoutManager(this)

        binding.rvLayout.adapter = adapter

        binding.btnScrollWithOffset.setOnClickListener {
            val position = 10
            val offset = ScreenUtil.getScreenHeight(this) / 2
            (binding.rvLayout.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
            //binding.rvLayout.scrollToPosition(position)
        }

    }

    override fun getTestData(): MutableList<TestBean> {
        val mDatas: MutableList<TestBean> = ArrayList()
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "奖励", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei9", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("滚动到的位置", "测试", R.drawable.pic_5))
        for (i in 0..20) {
            mDatas.add(TestBean("dumingwei$i", "测试", R.drawable.pic_5))
        }
        return mDatas
    }

}
