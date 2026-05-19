package net.lateinint

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.math.round

class SystemMetricsCollector(private val config: AppConfig) {
    private val runtime = Runtime.getRuntime()
    private val runtimeBean = ManagementFactory.getRuntimeMXBean()
    private val osBean = ManagementFactory.getOperatingSystemMXBean()
    private val extendedOsBean = osBean as? OperatingSystemMXBean
    private val hostName: String = runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("unknown")

    fun collect(): SystemSnapshot {
        val now = System.currentTimeMillis()
        val physicalTotal = extendedOsBean?.totalMemorySize ?: 0L
        val physicalFree = extendedOsBean?.freeMemorySize ?: 0L
        val physicalUsed = (physicalTotal - physicalFree).coerceAtLeast(0)

        val jvmMax = runtime.maxMemory()
        val jvmTotal = runtime.totalMemory()
        val jvmFree = runtime.freeMemory()
        val jvmUsed = (jvmTotal - jvmFree).coerceAtLeast(0)

        return SystemSnapshot(
            timestamp = now,
            host = hostName,
            uptimeSeconds = runtimeBean.uptime / 1000,
            loadAverage = osBean.systemLoadAverage.takeIf { it >= 0 },
            cpu = CpuMetrics(
                systemPercent = percentFromRatio(extendedOsBean?.cpuLoad),
                processPercent = percentFromRatio(extendedOsBean?.processCpuLoad),
                availableProcessors = osBean.availableProcessors,
            ),
            memory = MemoryMetrics(
                totalBytes = physicalTotal,
                usedBytes = physicalUsed,
                freeBytes = physicalFree,
                usedPercent = percent(physicalUsed, physicalTotal),
            ),
            jvm = JvmMetrics(
                maxBytes = jvmMax,
                totalBytes = jvmTotal,
                freeBytes = jvmFree,
                usedBytes = jvmUsed,
                usedPercent = percent(jvmUsed, jvmMax),
            ),
            disks = config.checks.diskPaths.distinct().mapNotNull { path ->
                runCatching {
                    val store = Files.getFileStore(path)
                    val total = store.totalSpace
                    val usable = store.usableSpace
                    val used = (total - usable).coerceAtLeast(0)
                    DiskMetrics(
                        path = path.absolutePathString(),
                        totalBytes = total,
                        usedBytes = used,
                        usableBytes = usable,
                        usedPercent = percent(used, total),
                    )
                }.getOrNull()
            },
        )
    }

    private fun percentFromRatio(value: Double?): Double? =
        value?.takeIf { it >= 0 }?.let { roundToOne(it * 100.0) }

    private fun percent(used: Long, total: Long): Double? =
        if (total <= 0) null else roundToOne((used.toDouble() / total.toDouble()) * 100.0)

    private fun roundToOne(value: Double): Double = round(value * 10.0) / 10.0
}
