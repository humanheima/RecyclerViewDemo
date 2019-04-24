package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.widget.Toast
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.model.TestBean
import kotlinx.android.synthetic.main.activity_layout.*

class LayoutActivity : BaseRecyclerViewAdapterActivity() {


    private var adapter: BaseAdapter<TestBean>? = null

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, LayoutActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

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
                        Toast.makeText(this@LayoutActivity,
                                "onItemClick position=$position", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        rvLayout.layoutManager = GridLayoutManager(this, 2)
        rvLayout.adapter = adapter
    }
}
