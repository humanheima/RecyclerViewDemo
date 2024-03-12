package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.adapter.TestAnimatorAdapterAdapter
import com.hm.demo.databinding.ActivityRecyclerTheoryBinding
import com.hm.demo.model.CheckBoxModel

/**
 * 探索复用的原理
 */
class RecyclerTheoryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    companion object {

        private const val TAG = "RecyclerTheoryActivity"

        fun launch(context: Context) {
            val intent = Intent(context, RecyclerTheoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityRecyclerTheoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerTheoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rv = binding.rvTheory


        rv.javaClass.declaredFields.forEach {
            Log.i(TAG, "onCreate:  field name = ${it.name}")
        }

        val sVerboseLoggingEnabledField = rv.javaClass.getDeclaredField("sVerboseLoggingEnabled")

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

        rv.layoutManager = LinearLayoutManager(this)
        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 8) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        rv.adapter = TestAnimatorAdapterAdapter(this, arrayList)

        binding.btnNotifyItemChanged.setOnClickListener {
            testNotifyItemInserted(arrayList)
//            binding.rvTheory.postDelayed({
//                val childCount = rv.childCount
//                for (i in 0 until childCount) {
//                    val itemView = rv.getChildAt(i)
//                    if (itemView is ViewGroup) {
//                        Log.i(TAG,
//                            "onCreate: itemView.getChildAt(2).toString() = " + itemView.getChildAt(2)
//                                .toString()
//                        )
//                    }
//
//                }
//
//            }, 3000)
            //testTranslationY()
        }
    }

    private fun testTranslationY() {
        val layoutManager = rv.layoutManager
        val itemView = layoutManager?.findViewByPosition(1)
        itemView?.let {
            it.translationY = -300f
            it.animate().translationY(0f)
                .setDuration(2000)
                .start()
        }
    }

    private fun testNotifyItemInserted(arrayList: ArrayList<CheckBoxModel>) {
        arrayList.add(1, CheckBoxModel("插入进来的数据", true))
        rv.adapter?.notifyItemInserted(1)
    }


}


