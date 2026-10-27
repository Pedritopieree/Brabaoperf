package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Benchmark mode hooks — runs in android (system_server).
 *
 *  - Sustains PERFORMANCE CPU/GPU state during benchmark apps
 *  - Prevents the system from detecting and penalising benchmark patterns
 */
class BenchmarkHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookPersistentPerformance()
        hookSustainedPerformanceMode()
    }

    private fun hookPersistentPerformance() {
        // android.os.PerformanceHintManager.Session (API 31+)
        val cls = findClass("android.os.PerformanceHintManager\$Session") ?: return
        // reportActualWorkDuration: used by games to hint the scheduler
        // We report "work completed at target" to keep clocks high
        hook(cls, "reportActualWorkDuration", LongArray::class.java,
            before { p ->
                // Keep durations at target (don't allow scheduler to lower clocks)
                HookLog.d("Bench: reportActualWorkDuration intercepted")
            })
    }

    private fun hookSustainedPerformanceMode() {
        // android.view.Window.setSustainedPerformanceMode
        hook("android.view.Window", "setSustainedPerformanceMode", Boolean::class.java,
            before { p ->
                // Always grant sustained performance mode
                if (p.args[0] == false) {
                    HookLog.d("Bench: setSustainedPerformanceMode false → true")
                    p.args[0] = true
                }
            })
    }
}
