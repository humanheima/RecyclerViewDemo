package com.hm.demo.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.hm.demo.R

/**
 * HeartDialog2
 * 根据设计图实现的底部弹窗（顶部圆角、标题、副标题、说明行、左右按钮）
 */
class HeartDialog2(
    context: Context,
    private val title: String = "确认解锁Ta的心声?",
    private val subtitle: String = "",
    private val extra: String = "消耗 50 贝壳 / 余额 3450 贝壳",
    private val cancelText: String = "取消",
    private val confirmText: String = "确认",
    private val onCancel: () -> Unit = {},
    private val onConfirm: () -> Unit = {}
) : Dialog(context, R.style.BottomDialogStyle) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // bottom style 已设置透明背景，下面再设置位置
        window?.setGravity(Gravity.BOTTOM)
        window?.setDimAmount(0.5f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_heart2)

        // 宽度匹配父容器，高度包裹
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        // 可点击外部关闭
        setCanceledOnTouchOutside(true)

        initView()
    }

    private fun initView() {
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)
        val tvExtra = findViewById<TextView>(R.id.tv_extra)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)

        tvTitle?.text = title
        tvSubtitle?.text = subtitle
        tvExtra?.text = extra
        btnCancel?.text = cancelText
        btnConfirm?.text = confirmText

        btnCancel?.setOnClickListener {
            onCancel()
            dismiss()
        }

        btnConfirm?.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    fun showDialog() {
        if (!isShowing) show()
    }
}

