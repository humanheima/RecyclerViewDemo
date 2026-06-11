package com.hm.demo.startup

/**
 * 这里放几个“假装很重要”的组件，模拟真实项目里需要在启动时初始化的库：
 * 日志库、配置中心、埋点统计、崩溃上报、图片缓存等。
 *
 * 每个 Initializer 初始化后会返回对应的组件实例，App Startup 会缓存这个实例，
 * 保证同一个 Initializer 在一次进程生命周期内只会执行一次。
 */

/** 日志组件（被多个其它组件依赖） */
class Logger {
    fun log(msg: String) = StartupLogger.log("Logger", "记录日志: $msg")
}

/** 配置组件 */
class AppConfig {
    val env: String = "production"
    val featureFlags: Map<String, Boolean> = mapOf(
        "new_home" to true,
        "dark_mode" to false
    )
}

/** 埋点统计组件，依赖 Logger 和 AppConfig */
class Analytics(private val logger: Logger, private val config: AppConfig) {
    fun track(event: String) =
        logger.log("埋点[$event] env=${config.env}")
}

/** 崩溃上报组件，依赖 Logger（和 Analytics 共享 Logger 这个依赖） */
class CrashReporter(private val logger: Logger) {
    fun report(t: Throwable) = logger.log("崩溃上报: ${t.message}")
}

/** 一个“很重”的图片缓存组件，初始化耗时较长，适合做懒加载/手动初始化 */
class HeavyImageCache {
    val sizeMb: Int = 256
}
