package com.hm.demo.startup

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.startup.AppInitializer
import com.hm.demo.R

/**
 * App Startup 演示页。
 *
 * 当这个 Activity 被创建时，下列“自动初始化”的组件其实早已执行完毕：
 *   - LoggerInitializer（被 Analytics / CrashReporter 共同依赖，只跑一次）
 *   - ConfigInitializer
 *   - AnalyticsInitializer
 *   - CrashReporterInitializer
 *
 * 而 HeavyImageCacheInitializer 因为在 manifest 里被 tools:node="remove" 移除，
 * 启动时不会执行；点击按钮才会手动初始化。整个过程通过 [StartupLogger] 记录并展示。
 */
class AppStartupDemoActivity : AppCompatActivity() {

    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_startup_demo)
        title = "App Startup 演示"

        logView = findViewById(R.id.tv_startup_log)

        findViewById<Button>(R.id.btn_init_heavy).setOnClickListener {
            manuallyInitHeavyComponent()
        }
        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            refreshLog()
        }

        refreshLog()
    }

    /**
     * 场景 5：手动触发懒加载组件的初始化。
     * 第一次点击会真正执行 create()（耗时 ~300ms）；
     * 再次点击会直接返回缓存实例，不会重复初始化 —— 观察日志即可验证。
     */
    private fun manuallyInitHeavyComponent() {
        val start = SystemClock.elapsedRealtime()
        val cache = AppInitializer.getInstance(this)
            .initializeComponent(HeavyImageCacheInitializer::class.java)
        val cost = SystemClock.elapsedRealtime() - start

        Toast.makeText(
            this,
            "图片缓存已就绪: ${cache.sizeMb}MB（本次耗时 ${cost}ms）",
            Toast.LENGTH_SHORT
        ).show()
        refreshLog()
    }

    private fun refreshLog() {
        logView.text = StartupLogger.dump()
    }
}
