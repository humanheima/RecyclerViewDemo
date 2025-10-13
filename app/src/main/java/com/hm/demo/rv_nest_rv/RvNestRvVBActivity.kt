package com.hm.demo.rv_nest_rv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hm.demo.databinding.ActivityRvNestRvV2Binding

/**
 * A separate Activity that demonstrates RecyclerView nested RecyclerView using ViewBinding.
 * This file does not modify the original `RvNestRvActivity`.
 */
class RvNestRvVBActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvNestRvV2Binding

    companion object {
        fun launch(context: Context) {
            val starter = Intent(context, RvNestRvVBActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvNestRvV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prepare sample data (reuse ParentItem and ParentAdapter/ChildAdapter if present)
        val parentList = mutableListOf<ParentItem>()
        for (i in 1..15) {
            val children = mutableListOf<String>()
            for (j in 1..8) {
                children.add("VB Item $i - Child $j")
            }
            parentList.add(ParentItem(title = "VB Group $i", children = children))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ParentAdapter(parentList)
    }
}

