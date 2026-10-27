package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Monitors thermal status changes for logging/diagnostics.
 * Runs in android process.
 */
class ThermalWatchHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    companion object {
        val STATUS_NAMES = mapOf(0 to "NONE", 1 to "LIGHT", 2 to "MODERATE",
            3 to "SEVERE", 4 to "CRITICAL", 5 to "EMERGENCY", 6 to "SHUTDOWN")
    }

    override fun install() {
        hookThermalStatusChanged()
    }

    private fun hookThermalStatusChanged() {
        // Hook the point where Android broadcasts thermal status to registered listeners
        hook("android.hardware.thermal.ThermalManager",
            "notifyThrottlingCallback",
            Boolean::class.java, Float::class.java,
            after { p ->
                val isThrottling = p.args[0] as Boolean
                val headroom = p.args[1] as Float
                HookLog.i("Thermal watch: throttling=$isThrottling headroom=$headroom")
            })

        // Monitor PowerManager thermal status changes
        hook("android.os.PowerManager",
            "notifyThermalStatusListeners",
            Int::class.java,
            after { p ->
                val status = p.args[0] as Int
                HookLog.i("Thermal watch: status=${STATUS_NAMES[status] ?: status}")
            })
    }
}
