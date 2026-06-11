package com.hm.demo.startup

import android.content.Context
import androidx.startup.Initializer

/**
 * 场景 1：最基础的 Initializer（无依赖，启动时自动初始化）。
 *
 * 要点：
 * 1. 实现 [Initializer]<T>，泛型 T 是这个 Initializer 产出的组件类型。
 * 2. [create] 里完成初始化逻辑，并返回组件实例；这个实例会被 App Startup 缓存。
 * 3. [dependencies] 返回空列表，表示它不依赖任何其它 Initializer。
 *
 * 该 Initializer 会被注册在 AndroidManifest 的 InitializationProvider 中，
 * 所以 App 进程一启动就会自动执行（早于第一个 Activity）。
 */
class LoggerInitializer : Initializer<Logger> {

    override fun create(context: Context): Logger {
        StartupLogger.log("LoggerInitializer", "开始初始化日志组件 (自动 / 无依赖)")
        val logger = Logger()
        StartupLogger.log("LoggerInitializer", "日志组件初始化完成")
        return logger
    }

    // 没有依赖
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
