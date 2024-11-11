package com.hm.demo.rv_nest_rv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.activity.RecyclerTheoryActivity
import com.hm.demo.activity.RecyclerTheoryActivity.Companion
import com.hm.demo.databinding.ActivityRvNestRvBinding
import com.hm.demo.test_payload.PayloadBean

/**
 * Created by p_dmweidu on 2024/11/11
 * Desc: 测试RecyclerView 嵌套 RecyclerView 。内部的 RecyclerView 的 item 是否能复用用。
 * 直接看有没有往缓存里加内部的 ViewHolder
 */
class RvNestRvActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRvNestRvBinding


    companion object {

        private const val TAG = "RvNestRvActivity"

        fun launch(context: Context) {
            val starter = Intent(context, RvNestRvActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvNestRvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sVerboseLoggingEnabledField = binding.rvOut.javaClass.getDeclaredField("sVerboseLoggingEnabled")

        sVerboseLoggingEnabledField.isAccessible = true
        Log.i(
            TAG,
            "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}"
        )
        sVerboseLoggingEnabledField.set(null, true)

        Log.i(
            TAG,
            "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}"
        )


        val list = arrayListOf<TestRvNestRVBean>()
        for (i in 0..10) {
            val bean = TestRvNestRVBean()
            bean.text1 = "测试数据 $i"
            val children = arrayListOf<PayloadBean>()

            for (j in 0..2) {
                children.add(PayloadBean(R.drawable.pic_2, "测试数据 $j", "测试数据 $j"))
            }

            bean.children = children
            list.add(bean)
        }
        val adapter = RvOutAdapter(list)

        binding.rvOut.adapter = adapter
        binding.rvOut.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvOut.addRecyclerListener(object :
            androidx.recyclerview.widget.RecyclerView.RecyclerListener {
            override fun onViewRecycled(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
                Log.d(
                    TAG,
                    "RvOutAdapter onViewRecycled: ViewHolder = $holder ${
                        Log.getStackTraceString(
                            Throwable()
                        )
                    }"
                )
            }
        })
    }
}