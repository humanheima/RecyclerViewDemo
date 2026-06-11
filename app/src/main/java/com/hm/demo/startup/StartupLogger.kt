package com.hm.demo.startup

import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 启动初始化日志收集器。
 *
 * App Startup 的初始化发生在 Activity 创建之前（ContentProvider 阶段），
 * 我们没法直接在界面上观察到执行过程，所以用这个对象把每个 Initializer 的
 * 执行顺序、所在线程、耗时都记录下来，之后在 [com.hm.demo.startup.AppStartupDemoActivity] 里展示。
 *
 * 真实项目里你不需要这个类，它只是为了把“看不见的启动过程”可视化。
 */
object StartupLogger {

    private const val TAG = "AppStartup"

    data class Record(
        val name: String,          // Initializer 名字
        val message: String,       // 描述：开始/结束/手动触发等
        val thread: String,        // 执行所在线程
        val sinceStartMs: Long     // 距离进程启动的相对毫秒数
    )

    /** 进程启动的基准时间，用来计算相对耗时 */
    private val processStartElapsed = SystemClock.elapsedRealtime()

    /** 用并发安全的 List，因为 Initializer 可能在不同线程触发 */
    private val records = CopyOnWriteArrayList<Record>()

    fun log(name: String, message: String) {
        val isMain = Looper.myLooper() == Looper.getMainLooper()
        val threadDesc = if (isMain) "主线程(main)" else Thread.currentThread().name
        val record = Record(
            name = name,
            message = message,
            thread = threadDesc,
            sinceStartMs = SystemClock.elapsedRealtime() - processStartElapsed
        )
        records.add(record)
        Log.d(TAG, "[+${record.sinceStartMs}ms][$threadDesc] $name -> $message")
    }

    fun snapshot(): List<Record> = records.toList()

    /** 格式化成适合展示的多行文本 */
    fun dump(): String {
        if (records.isEmpty()) return "（暂无初始化记录）"
        return records.joinToString(separator = "\n") { r ->
            "[+${r.sinceStartMs}ms] ${r.name}\n    ↳ ${r.message}  @${r.thread}"
        }
    }
}
