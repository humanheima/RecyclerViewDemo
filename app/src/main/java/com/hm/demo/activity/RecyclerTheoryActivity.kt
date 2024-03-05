package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            val model = arrayList[1]
            model.isChecked = !model.isChecked
            model.description = "改变后的描述"
            rv.adapter?.notifyItemChanged(1)
        }
    }

}


