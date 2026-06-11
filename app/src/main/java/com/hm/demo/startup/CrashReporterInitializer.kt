package com.hm.demo.startup

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer

/**
 * 场景 4：多个组件共享同一个依赖（依赖去重）。
 *
 * CrashReporter 同样依赖 Logger。配合 [AnalyticsInitializer] 一起观察启动日志会发现：
 * 即使 LoggerInitializer 被 Analytics 和 CrashReporter 同时依赖，
 * 它的 create() 也只会执行一次 —— App Startup 内部会缓存每个组件实例。
 *
 * 这正是 App Startup 相比“各自起一个 ContentProvider 初始化”的核心优势：
 * 共用一个 InitializationProvider + 自动去重，避免重复初始化与启动开销。
 */
class CrashReporterInitializer : Initializer<CrashReporter> {

    override fun create(context: Context): CrashReporter {
        StartupLogger.log("CrashReporterInitializer", "开始初始化崩溃上报 (依赖 Logger)")

        // 复用已经初始化过的 Logger，不会触发第二次 LoggerInitializer.create()
        val logger = AppInitializer.getInstance(context)
            .initializeComponent(LoggerInitializer::class.java)

        val reporter = CrashReporter(logger)
        StartupLogger.log("CrashReporterInitializer", "崩溃上报初始化完成")
        return reporter
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(LoggerInitializer::class.java)
}
