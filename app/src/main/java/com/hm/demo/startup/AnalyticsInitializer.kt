package com.hm.demo.startup

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer

/**
 * 场景 3：带依赖的 Initializer。
 *
 * Analytics 依赖 Logger 和 AppConfig。通过 [dependencies] 声明依赖后：
 * - App Startup 会保证 [LoggerInitializer] 和 [ConfigInitializer] 先于本类执行；
 * - 想拿到依赖产出的实例，用 [AppInitializer.initializeComponent] 即可（已初始化过的会直接返回缓存，不会重复执行）。
 *
 * 注意：在 manifest 中只需要声明本 Initializer（叶子节点），
 * 它依赖的 Logger / Config 会被自动拉起，无需重复声明。
 */
class AnalyticsInitializer : Initializer<Analytics> {

    override fun create(context: Context): Analytics {
        StartupLogger.log("AnalyticsInitializer", "开始初始化埋点 (依赖 Logger + Config)")

        // 获取依赖实例：此时依赖一定已经初始化完成
        val logger = AppInitializer.getInstance(context)
            .initializeComponent(LoggerInitializer::class.java)
        val config = AppInitializer.getInstance(context)
            .initializeComponent(ConfigInitializer::class.java)

        val analytics = Analytics(logger, config)
        analytics.track("app_start")
        StartupLogger.log("AnalyticsInitializer", "埋点初始化完成")
        return analytics
    }

    // 声明依赖：Logger 和 Config 会先被初始化
    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(LoggerInitializer::class.java, ConfigInitializer::class.java)
}
