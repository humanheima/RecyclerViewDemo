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
 * 解锁心声底部弹窗
 * 包含标题、副标题和两个操作按钮
 */
class HeartBottomDialog(
    context: Context,
    private val title: String = "确认解锁Ta的心声?",
    private val subtitle: String = "消耗 500 贝壳解锁Ta的心声",
    private val cancelText: String = "取消",
    private val confirmText: String = "确认",
    private val onCancel: () -> Unit = {},
    private val onConfirm: () -> Unit = {}
) : Dialog(context, R.style.BottomDialogStyle) {

    init {
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 设置弹窗位置为底部
        window?.setGravity(Gravity.BOTTOM)
        // 设置弹窗背景半透明
        window?.setDimAmount(0.5f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_heart_bottom)
        
        // 设置弹窗宽度为屏幕宽度，高度为包裹内容
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // 设置弹窗进入和退出动画
        window?.setWindowAnimations(android.R.style.Animation_Dialog)
        
        // 设置点击外部可取消
        setCanceledOnTouchOutside(true)
        
        // 初始化视图
        initView()
    }
    
    private fun initView() {
        // 设置标题和副标题
        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<TextView>(R.id.tv_subtitle).text = subtitle
        
        // 设置按钮文本和点击事件
        findViewById<Button>(R.id.btn_cancel).apply {
            text = cancelText
            setOnClickListener {
                onCancel()
                dismiss()
            }
        }
        
        findViewById<Button>(R.id.btn_confirm).apply {
            text = confirmText
            setOnClickListener {
                onConfirm()
                dismiss()
            }
        }
    }
    
    /**
     * 显示弹窗
     */
    fun showDialog() {
        if (!isShowing) {
            show()
        }
    }
}