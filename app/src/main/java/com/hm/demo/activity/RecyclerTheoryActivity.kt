package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.SimpleAdapterAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_theory)
        rv = findViewById(R.id.rv_theory)


        rv.javaClass.declaredFields.forEach {
            Log.i(TAG, "onCreate:  field name = ${it.name}")
        }

        val sVerboseLoggingEnabledField = rv.javaClass.getDeclaredField("sVerboseLoggingEnabled")

        sVerboseLoggingEnabledField.isAccessible = true
        Log.i(TAG, "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}")
        sVerboseLoggingEnabledField.set(null, true)

        Log.i(TAG, "onCreate: sVerboseLoggingEnabledField = ${sVerboseLoggingEnabledField.getBoolean(null)}")

        rv.layoutManager = LinearLayoutManager(this)
        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 20) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        rv.adapter = SimpleAdapterAdapter(this, arrayList)


    }

}


