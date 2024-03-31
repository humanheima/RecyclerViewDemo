package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hm.demo.Util.ScreenUtil
import com.hm.demo.adapter.TestLayoutManagerAdapter
import com.hm.demo.databinding.ActivityLayoutBinding
import com.hm.demo.model.CheckBoxModel

/**
 * Created by dumingwei on 2020/6/9
 *
 * Desc: 测试RecyclerView的各种 layout manager
 */
class LayoutManagerActivity : BaseRecyclerViewAdapterActivity() {

    private var adapter: TestLayoutManagerAdapter? = null

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

        val listOf = arrayListOf<CheckBoxModel>()
        for (i in 0..10) {
            listOf.add(CheckBoxModel("Hello Gird $i", false))
        }
        adapter = TestLayoutManagerAdapter(this, listOf)

        binding.rvLayout.layoutManager = GridLayoutManager(this, 2)
        binding.rvLayout.adapter = adapter

        binding.btnScrollWithOffset.setOnClickListener {

        }

    }

}
