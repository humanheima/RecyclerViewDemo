package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityRecyclerViewTimeLineBinding
import com.hm.demo.model.TestBean
import com.hm.demo.widget.LinearLayoutItemDecoration
import com.hm.demo.widget.TimeLineItemDecoration

/**
 * Crete by dumingwei on 2019/3/15
 * Desc: 使用ItemDecoration实现实现时间线
 *
 */
open class RecyclerViewTimeLineActivity : AppCompatActivity() {

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, RecyclerViewTimeLineActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityRecyclerViewTimeLineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerViewTimeLineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(
            LinearLayoutItemDecoration(
                this,
                LinearLayoutItemDecoration.VERTICAL_LIST
            )
        )
        binding.recyclerView.addItemDecoration(TimeLineItemDecoration())
        binding.recyclerView.adapter = object : BaseAdapter<TestBean>(this, getTestData()) {

            override fun getHolderType(position: Int, testBean: TestBean): Int {
                return R.layout.item_diff
            }

            override fun bindViewHolder(holder: BaseViewHolder, testBean: TestBean, position: Int) {
                if (holder.itemViewType == R.layout.item_diff) {
                    holder.setTextViewText(R.id.tv1, testBean.name)
                    holder.setTextViewText(R.id.tv2, testBean.desc)
                    holder.setImageViewResource(R.id.iv, testBean.picture)
                    holder.setOnItemClickListener(R.id.iv) { _, pos ->
                        Toast.makeText(
                            this@RecyclerViewTimeLineActivity,
                            "onItemClick position=$pos", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun getTestData(): List<TestBean> {
        val mDatas = ArrayList<TestBean>()
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
