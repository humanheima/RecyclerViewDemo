package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.model.TestBean
import kotlinx.android.synthetic.main.activity_layout.*
import kotlinx.android.synthetic.main.footer_view_load_more.*

/**
 * Created by dumingwei on 2020/6/9
 *
 * Desc: 测试RecyclerView的各种 layout manager
 */
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

        val footView = LayoutInflater.from(this).inflate(R.layout.footer_view_load_more, null)



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

        rvLayout.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

        adapter?.addFootView(footView)
        rvLayout.adapter = adapter
    }


}
