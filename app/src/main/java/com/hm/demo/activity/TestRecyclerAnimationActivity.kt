package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.adapter.TestAnimatorAdapterAdapter
import com.hm.demo.databinding.ActivityRecyclerAnimationBinding
import com.hm.demo.model.CheckBoxModel

/**
 * Created by p_dmweidu on 2024/3/5
 * Desc: 测试RecyclerView的动画
 */
class TestRecyclerAnimationActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    companion object {

        private const val TAG = "RecyclerTheoryActivity"

        fun launch(context: Context) {
            val intent = Intent(context, TestRecyclerAnimationActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityRecyclerAnimationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerAnimationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rv = binding.rvTheory

//        rv.itemAnimator?.addDuration = 1200
//        rv.itemAnimator?.changeDuration = 2500
//        rv.itemAnimator?.moveDuration = 2500
//        rv.itemAnimator?.removeDuration = 1200


//        rv.javaClass.declaredFields.forEach {
//            Log.i(TAG, "onCreate:  field name = ${it.name}")
//        }
//
//        val sVerboseLoggingEnabledField = rv.javaClass.getDeclaredField("sVerboseLoggingEnabled")
//
//        sVerboseLoggingEnabledField.isAccessible = true
//        Log.i(
//            TAG,
//            "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}"
//        )
//        sVerboseLoggingEnabledField.set(null, true)
//
//        Log.i(
//            TAG,
//            "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}"
//        )

        rv.layoutManager = LinearLayoutManager(this)
        val arrayList = arrayListOf<CheckBoxModel>()
        val testAnimatorAdapterAdapter = TestAnimatorAdapterAdapter(this)
        rv.adapter = testAnimatorAdapterAdapter


//        val newArrayList = arrayListOf<CheckBoxModel>()
//        for (i in 0 until 10) {
//            newArrayList.add(CheckBoxModel("hi Hello$i", false))
//        }
//        testAnimatorAdapterAdapter.onDataSourceChanged(newArrayList)
//        for (index in 0 until 10) {
//            testAnimatorAdapterAdapter.notifyItemInserted(index)
//        }

//        binding.btnNotifyItemChanged.post {
//            val newArrayList = arrayListOf<CheckBoxModel>()
//            for (i in 0 until 10) {
//                newArrayList.add(CheckBoxModel("hi Hello$i", false))
//            }
//            testAnimatorAdapterAdapter.onDataSourceChanged(newArrayList)
//            for (index in 0 until 10) {
//                testAnimatorAdapterAdapter.notifyItemInserted(index)
//            }
//        }


//        binding.btnNotifyItemChanged.postDelayed({
//            val newArrayList = arrayListOf<CheckBoxModel>()
//            for (i in 0 until 10) {
//                newArrayList.add(CheckBoxModel("hi Hello$i", false))
//            }
//            testAnimatorAdapterAdapter.onDataSourceChanged(newArrayList)
//            for (index in 0 until 10) {
//                testAnimatorAdapterAdapter.notifyItemInserted(index)
//            }
//        }, 10)

        binding.btnNotifyItemChanged.setOnClickListener {
            val newArrayList = arrayListOf<CheckBoxModel>()
            for (i in 0 until 4) {
                newArrayList.add(CheckBoxModel("hi Hello$i", false))
            }
            testAnimatorAdapterAdapter.onDataSourceChanged(newArrayList)
            //testAnimatorAdapterAdapter.notifyDataSetChanged()
            for (index in 0 until 4) {
                //testAnimatorAdapterAdapter.notifyItemInserted(index)
                testAnimatorAdapterAdapter.mNotifyItemInserted(index)
            }

        }

    }

}


