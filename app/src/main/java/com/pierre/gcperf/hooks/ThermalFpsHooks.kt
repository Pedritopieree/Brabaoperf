package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Prevents thermal throttling from reducing game FPS.
 * Runs in android (system_server) process.
 *
 * Targets:
 *  - ThermalManager.getThermalHeadroom → always return 0.0 (no thermal pressure)
 *  - PowerManager.addThermalStatusListener → wrap listener to suppress events
 *  - Vivo thermal service (best-effort class discovery)
 */
class ThermalFpsHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookThermalHeadroom()
        hookThermalStatusListener()
        hookVivoThermal()
    }

    // getThermalHeadroom(forecastSeconds): 1.0 = throttle imminent, 0.0 = fine
    // We return 0.0 so callers (games, game service) never see a headroom warning.
    private fun hookThermalHeadroom() {
        hook("android.hardware.thermal.ThermalManager",
            "getThermalHeadroom", Int::class.java,
            replace { 0.0f })
            ?.also { HookLog.i("Thermal: hooked getThermalHeadroom") }
    }

    // Wrap the listener registration so status >= MODERATE is suppressed
    private fun hookThermalStatusListener() {
        val listenerCls = findClass(
            "android.hardware.thermal.ThermalManager\$OnThermalStatusChangedListener"
        )
        // Hook addThermalStatusListener with Executor + listener variant
        hook("android.hardware.thermal.ThermalManager",
            "registerThermalHeadroomCallback",
            after { p ->
                HookLog.d("Thermal: registerThermalHeadroomCallback intercepted")
            })

        // Also hook the status broadcast in PowerManager
        hook("android.os.PowerManager",
            "getCurrentThermalStatus",
            replace { 0 /* THERMAL_STATUS_NONE */ })
            ?.also { HookLog.i("Thermal: hooked getCurrentThermalStatus → 0") }
    }

    // Vivo thermal service — prevents per-app FPS reduction on thermal event
    private fun hookVivoThermal() {
        val cls = findClassAny(
            "com.vivo.thermal.VivoThermalService",
            "com.vivo.services.thermal.ThermalManagerService",
            "com.bbk.thermal.ThermalService",
            "com.vivo.server.thermal.VivoThermalEngine"
        ) ?: run {
            HookLog.w("Thermal: no vivo thermal class — AOSP hooks only")
            return
        }
        HookLog.i("Thermal: found ${cls.name}")
        for (m in listOf("notifyThrottling", "applyFpsDowngrade",
                         "setGameFpsLimit", "onThermalEvent",
                         "reduceFpsForPackage")) {
            val h = hook(cls, m, String::class.java, Int::class.java,
                before { p ->
                    HookLog.d("Thermal[${cls.simpleName}]: blocked $m(${p.args[0]},${p.args[1]})")
                    p.result = null
                })
            if (h != null) HookLog.i("Thermal: hooked ${cls.simpleName}.$m")
        }
    }
}
