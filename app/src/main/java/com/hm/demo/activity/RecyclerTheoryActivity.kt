package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.SimpleAdapterAdapter
import com.hm.demo.model.CheckBoxModel
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * 探索复用的原理
 */
class RecyclerTheoryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, RecyclerTheoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_theory)
        rv = findViewById(R.id.rv_theory)

        val nameField = rv.javaClass.getDeclaredField("DEBUG")
        nameField.isAccessible = true

        val modifiers: Field = nameField.javaClass.getDeclaredField("accessFlags")
        modifiers.isAccessible = true
        //modifiers.setInt(nameField, nameField.modifiers xor Modifier.FINAL.inv())

        nameField.set(rv, true)

        //modifiers.setInt(nameField, nameField.modifiers and Modifier.FINAL.inv())



        rv.layoutManager = LinearLayoutManager(this)
        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 20) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        rv.adapter = SimpleAdapterAdapter(this, arrayList)


    }

}


