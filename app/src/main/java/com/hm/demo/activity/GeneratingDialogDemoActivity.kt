package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.databinding.ActivityGeneratingDialogDemoBinding
import com.hm.demo.widget.GeneratingDialog

/**
 * 展示"生成中"弹窗的示例Activity
 */
class GeneratingDialogDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeneratingDialogDemoBinding
    private lateinit var generatingDialog: GeneratingDialog

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, GeneratingDialogDemoActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratingDialogDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化弹窗
        generatingDialog = GeneratingDialog(this)

        // 设置按钮点击事件
        binding.btnShowDialog.setOnClickListener {
            // 显示弹窗
            generatingDialog.showDialog()
            
            // 模拟3秒后自动关闭弹窗
            Handler(Looper.getMainLooper()).postDelayed({
                generatingDialog.hideDialog()
            }, 3000)
        }
    }
}