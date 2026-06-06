package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.adapter.FloatItemAdapter
import com.hm.demo.databinding.ActivityFloatItemBinding
import com.hm.demo.model.TestBean

/**
 * Created by dumingwei on 2020/4/27
 *
 * Desc:
 */
class FloatItemActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FloatItemActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityFloatItemBinding

    private val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFloatItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = FloatItemAdapter(this, getTestData())

        binding.recyclerView.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                rv: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(rv, dx, dy)
                val firstVisibleItemPos = linearLayoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPos >= 2) {
                    binding.includeViewFloatLayout.clRoot.visibility = View.VISIBLE
                } else {
                    binding.includeViewFloatLayout.clRoot.visibility = View.GONE
                }
            }

        })
    }

    private fun getTestData(): List<TestBean> {
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
        return mDatas
    }
}
