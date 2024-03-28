package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.adapter.TestAnimatorAdapter
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

        rv.itemAnimator?.addDuration = 600
        rv.itemAnimator?.changeDuration = 1250
        rv.itemAnimator?.moveDuration = 1250
        rv.itemAnimator?.removeDuration = 600


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

        val debugField = LinearLayoutManager::class.java.getDeclaredField("DEBUG")
        debugField.isAccessible = true
        Log.i(TAG, "onCreate: debugField = ${debugField.getBoolean(null)}")
        debugField.set(null, true)
        Log.i(TAG, "onCreate: debugField = ${debugField.getBoolean(null)}")


        rv.layoutManager = LinearLayoutManager(this)
        val testAnimatorAdapter = TestAnimatorAdapter(this)
        rv.adapter = testAnimatorAdapter

        binding.btnNotifyItemChanged.setOnClickListener {
            val newArrayList = arrayListOf<CheckBoxModel>()
            for (i in 0 until 4) {
                newArrayList.add(CheckBoxModel("hi Hello$i", false))
            }
            testAnimatorAdapter.onDataSourceChanged(newArrayList)
            //testAnimatorAdapter.notifyDataSetChanged()
            for (index in 0 until 4) {
                //这里一定要注意了，这里的index是从0开始的，所以要加上HEAD_COUNT
                testAnimatorAdapter.notifyItemInserted(index)
            }
        }
    }

}


