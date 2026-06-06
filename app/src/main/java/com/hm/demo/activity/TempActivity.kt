package com.hm.demo.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.TempAdapter
import com.hm.demo.widget.HeartDialog2
import com.hm.demo.widget.HeartDialog3

class TempActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TempAdapter
    private lateinit var btnShowDialog: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 创建示例数据
        val dataList = List(20) { "Item $it" }
        
        // 设置适配器
        adapter = TempAdapter(dataList)
        recyclerView.adapter = adapter
        
        // 初始化按钮并设置点击事件
        btnShowDialog = findViewById(R.id.btn_show_dialog)
        btnShowDialog.setOnClickListener {
            showHeartBottomDialog()
        }
    }
    
    /**
     * 显示解锁心声居中弹窗 -> 展示 HeartDialog3
     */
    private fun showHeartBottomDialog() {
        val dialog = HeartDialog3(
            context = this,
            title = "确认解锁Ta的心声?",
            subtitle = "消耗 50贝壳 / 余额 3450贝壳",
            cancelText = "取消",
            confirmText = "确认",
            onCancel = {
                Toast.makeText(this, "取消", Toast.LENGTH_SHORT).show()
            },
            onConfirm = {
                Toast.makeText(this, "确认", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.showDialog()
    }
}