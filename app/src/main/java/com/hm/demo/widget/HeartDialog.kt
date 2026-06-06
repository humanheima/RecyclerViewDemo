package com.hm.demo.widget

import com.hm.demo.R

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

/**
 * HeartDialog
 * 一个居中的心形弹窗，带心形动效、标题、副标题和两个按钮（喜欢/关闭）
 */
class HeartDialog(
    context: Context,
    private val title: String? = "给 Ta 点个心",
    private val subtitle: String? = "你的喜欢会被记录哦",
    private val likeText: String = "喜欢",
    private val closeText: String = "关闭",
    private val onLike: () -> Unit = {},
    private val onClose: () -> Unit = {}
) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)
        window?.setDimAmount(0.36f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_heart)

        // 宽高为包裹内容
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setCanceledOnTouchOutside(true)

        initView()
    }

    private fun initView() {
        val ivHeart = findViewById<ImageView>(R.id.iv_heart)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)
        val btnLike = findViewById<Button>(R.id.btn_like)
        val btnClose = findViewById<Button>(R.id.btn_close)

        tvTitle?.text = title
        tvSubtitle?.text = subtitle
        btnLike?.text = likeText
        btnClose?.text = closeText

        // 开始心跳动画（小幅缩放）
        ivHeart?.let {
            val anim = AnimationUtils.loadAnimation(context, R.anim.heart_pulse)
            it.startAnimation(anim)
        }

        btnLike?.setOnClickListener {
            onLike()
            dismiss()
        }

        btnClose?.setOnClickListener {
            onClose()
            dismiss()
        }
    }

    fun showDialog() {
        if (!isShowing) show()
    }
}
