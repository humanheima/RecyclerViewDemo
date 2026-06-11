package com.hm.demo.startup

import android.content.Context
import android.os.SystemClock
import androidx.startup.Initializer

/**
 * 场景 5：懒加载 / 手动初始化（按需，不在启动时自动执行）。
 *
 * 这个组件初始化很“重”（模拟耗时 300ms），不应该拖慢冷启动。
 * 做法：
 * 1. 在 AndroidManifest 里用 tools:node="remove" 把它从 InitializationProvider 的
 *    meta-data 中移除，这样 App 启动时 **不会** 自动执行它。
 * 2. 真正需要用到时，再手动调用：
 *
 *        AppInitializer.getInstance(context)
 *            .initializeComponent(HeavyImageCacheInitializer::class.java)
 *
 *    手动调用同样会走 App Startup 的缓存与依赖机制，且只会初始化一次。
 *
 * 这是 App Startup 推荐的“延迟初始化重组件”的标准姿势。
 */
class HeavyImageCacheInitializer : Initializer<HeavyImageCache> {

    override fun create(context: Context): HeavyImageCache {
        StartupLogger.log("HeavyImageCacheInitializer", "开始初始化图片缓存 (手动触发 / 耗时操作)")
        // 模拟耗时初始化
        SystemClock.sleep(300)
        val cache = HeavyImageCache()
        StartupLogger.log(
            "HeavyImageCacheInitializer",
            "图片缓存初始化完成: ${cache.sizeMb}MB"
        )
        return cache
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
