package com.hm.demo.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.hm.demo.R

/**
 * 生成中弹窗
 * 基于Figma设计稿实现的加载中弹窗
 */
class GeneratingDialog(context: Context) : Dialog(context) {

    init {
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 设置背景透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 设置弹窗位置为居中
        window?.setGravity(Gravity.CENTER)
        // 设置弹窗背景半透明
        window?.setDimAmount(0.36f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.hm.demo.R.layout.dialog_generating)
        
        // 设置弹窗宽度为包裹内容，高度为包裹内容
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // 设置点击外部不可取消
        setCanceledOnTouchOutside(false)
        // 设置返回键不可取消
        setCancelable(false)
    }
    
    /**
     * 显示弹窗
     */
    fun showDialog() {
        if (!isShowing) {
            show()
        }
    }
    
    /**
     * 隐藏弹窗
     */
    fun hideDialog() {
        if (isShowing) {
            dismiss()
        }
    }
}