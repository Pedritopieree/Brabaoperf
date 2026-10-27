package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Variable Sync Rate (VSR) backend hooks.
 * Runs in com.vivo.gamecenter process.
 *
 * Prevents VSR from reducing display refresh during heavy frames.
 */
class VsrBackendHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookVsrService()
        hookDisplayRatePolicy()
    }

    private fun hookVsrService() {
        val cls = findClassAny(
            "com.vivo.vsrservice.VsrBackend",
            "com.vivo.vsrservice.VsrService",
            "com.bbk.vsrservice.VsrManager",
            "com.vivo.gamecenter.vsr.VsrController",
            "com.vivo.display.VSRManager"
        ) ?: run { HookLog.w("VSR: backend class not found"); return }

        HookLog.i("VSR: found ${cls.name}")

        // Prevent VSR from lowering the refresh rate
        for (m in listOf("setVsrState", "applyVsrPolicy", "reduceRefreshRate",
                         "onFrameDrop", "notifyFramePacing", "switchToEco")) {
            hook(cls, m,
                before { p ->
                    HookLog.d("VSR: blocked ${cls.simpleName}.$m")
                    p.result = null
                })
        }

        // Force VSR to report "stable" pacing
        for (m in listOf("getFramePacingScore", "getStabilityScore",
                         "getSmoothness", "getVsrScore")) {
            hook(cls, m, replace { 100 })
                ?.also { HookLog.i("VSR: ${cls.simpleName}.$m → 100") }
        }
    }

    private fun hookDisplayRatePolicy() {
        val cls = findClassAny(
            "com.vivo.gamecenter.display.DisplayRatePolicy",
            "com.bbk.gamecenter.display.RatePolicy",
            "com.vivo.game.display.DisplayPolicyManager"
        ) ?: return

        HookLog.i("VSR: found DisplayRatePolicy ${cls.name}")
        for (m in listOf("getTargetRefreshRate", "computeOptimalRate",
                         "getRecommendedFps")) {
            hook(cls, m,
                after { p ->
                    val result = p.result as? Int ?: return@after
                    if (result < 60) {
                        p.result = 60
                        HookLog.d("VSR[DisplayRatePolicy]: $m $result → 60")
                    }
                })
        }
    }
}
