package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityScrollToCenterBinding
import com.hm.demo.model.TestBean
import com.hm.demo.widget.CenterLayoutManager

/**
 * Created by dumingwei on 2020/10/28
 *
 * Desc: 将RecyclerView的某个item滑动到RecyclerView的中间
 */
class ScrollToCenterActivity : AppCompatActivity() {

    private val TAG: String = "ScrollToCenterActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ScrollToCenterActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityScrollToCenterBinding

    private val centerLayoutManager = CenterLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollToCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rv.layoutManager = centerLayoutManager
        binding.rv.adapter = object : BaseAdapter<TestBean>(this, getTestData()) {

            override fun getHolderType(position: Int, testBean: TestBean): Int {
                return R.layout.item_scroll_to_center
            }

            override fun bindViewHolder(holder: BaseViewHolder, testBean: TestBean, position: Int) {
                holder.setTextViewText(R.id.tv1, testBean.name)
                holder.setImageViewResource(R.id.iv, testBean.picture)
                holder.setOnItemClickListener(R.id.iv) { view, position ->
                    Toast.makeText(
                        this@ScrollToCenterActivity,
                        "onItemClick position=$position",
                        Toast.LENGTH_SHORT
                    ).show()
                    centerLayoutManager.smoothScrollToPosition(
                        binding.rv,
                        androidx.recyclerview.widget.RecyclerView.State(),
                        position
                    )
                }
            }
        }
    }

    private fun getTestData(): List<TestBean>? {
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
        return mDatas
    }
}