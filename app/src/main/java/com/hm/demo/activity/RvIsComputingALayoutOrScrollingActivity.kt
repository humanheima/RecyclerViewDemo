package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.model.TestBean

/**
 * Created by p_dmweidu on 2023/6/1
 * Desc:测试 Cannot call this method while RecyclerView is computing a layout or scrolling
 * 的问题
 */
class RvIsComputingALayoutOrScrollingActivity : BaseRecyclerViewAdapterActivity() {

    private var adapter: BaseAdapter<TestBean>? = null
    private lateinit var rvLayout: RecyclerView

    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, RvIsComputingALayoutOrScrollingActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rv_is_computing_alayout_or_scrolling)

        rvLayout = findViewById(R.id.rvLayout)
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
                            this@RvIsComputingALayoutOrScrollingActivity,
                            "onItemClick position=$position", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        rvLayout.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        rvLayout.adapter = adapter

        adapter?.notifyDataSetChanged()


    }
}