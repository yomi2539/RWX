package io.github.rwx.bench

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class BenchFrameProbe(csvPath: String? = System.getenv("RWX_BENCH_CSV")) {
    val isEnabled: Boolean = !csvPath.isNullOrBlank()

    private var writer: BufferedWriter? = null
    private var lastStartNanos: Long = 0L
    private var framesSinceFlush: Int = 0

    init {
        if (isEnabled) {
            runCatching {
                val file = File(csvPath!!)
                file.parentFile?.mkdirs()
                val empty = !file.exists() || file.length() == 0L
                writer = BufferedWriter(FileWriter(file, true))
                if (empty) {
                    writer?.write("t_nanos,work_ms,interval_ms\n")
                    writer?.flush()
                }
            }.onFailure {
                runCatching { writer?.close() }
                writer = null
            }
        }
    }

    fun frame(workNanos: Long) {
        if (!isEnabled) return
        runCatching {
            synchronized(this) {
                val out = writer ?: return
                val now = System.nanoTime()
                val intervalNanos = if (lastStartNanos == 0L) 0L else now - lastStartNanos
                lastStartNanos = now
                out.write(
                    now.toString() + ',' +
                            (workNanos / 1_000_000.0).toString() + ',' +
                            (intervalNanos / 1_000_000.0).toString() + '\n'
                )
                framesSinceFlush++
                if (framesSinceFlush >= 60) {
                    framesSinceFlush = 0
                    out.flush()
                }
            }
        }
    }
}
