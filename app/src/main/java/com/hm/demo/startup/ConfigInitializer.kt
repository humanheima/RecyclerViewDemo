package com.hm.demo.startup

import android.content.Context
import androidx.startup.Initializer

/**
 * 场景 2：又一个无依赖的基础 Initializer。
 *
 * 它和 [LoggerInitializer] 互相独立，App Startup 会按依赖拓扑顺序去执行，
 * 两个互不依赖的根 Initializer 谁先谁后，取决于 manifest 中 meta-data 的声明顺序
 * 以及谁先被依赖方触发。
 */
class ConfigInitializer : Initializer<AppConfig> {

    override fun create(context: Context): AppConfig {
        StartupLogger.log("ConfigInitializer", "开始加载配置 (自动 / 无依赖)")
        val config = AppConfig()
        StartupLogger.log(
            "ConfigInitializer",
            "配置加载完成: env=${config.env}, flags=${config.featureFlags}"
        )
        return config
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
