package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.hm.demo.adapter.TestAnimatorAdapter
import com.hm.demo.databinding.ActivityRecyclerSnapHelperBinding
import com.hm.demo.model.CheckBoxModel


/**
 * Created by p_dmweidu on 2024/3/5
 * Desc: 测试RecyclerView的SnapHelper
 */
class SnapHelperActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    companion object {

        private const val TAG = "SnapHelperActivity"

        fun launch(context: Context) {
            val intent = Intent(context, SnapHelperActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityRecyclerSnapHelperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerSnapHelperBinding.inflate(layoutInflater)

        setContentView(binding.root)

        rv = binding.rvTheory

        rv.layoutManager = LinearLayoutManager(this)
        val testAnimatorAdapter = TestAnimatorAdapter(this)
        rv.adapter = testAnimatorAdapter

        val list = mutableListOf<CheckBoxModel>()

        for (i in 0 until 20) {
            list.add(CheckBoxModel("Hello$i", false))
        }
        testAnimatorAdapter.onDataSourceChanged(list)

        //val snapHelper: SnapHelper = LinearSnapHelper()


        /**
         * 使用PagerSnapHelper，ItemView 的宽高要设置为 match_parent
         */
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rv)


        binding.btnNotifyItemChanged.setOnClickListener {

        }
    }

}


