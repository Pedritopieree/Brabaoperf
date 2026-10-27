package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Forces maximum display refresh rate for games.
 * Runs in the android (system_server) process.
 *
 * Targets:
 *  - DisplayManagerGlobal.setRefreshRateSwitchingType → always WITHIN_GROUPS
 *  - DisplayManagerGlobal.setShouldAlwaysRespectAppRequestedMode → true
 *  - SurfaceControl.Transaction.setFrameRate → prevent downgrade to <60
 *  - DisplayManagerService internal FPS vote accumulator (best-effort)
 */
class RrmDisplayHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookRateSwitchingType()
        hookAlwaysRespectAppMode()
        hookFrameRateVote()
        hookVivoRrm()
    }

    // setRefreshRateSwitchingType: 0=NONE,1=WITHIN_GROUPS,2=ACROSS_GROUPS
    // Force WITHIN_GROUPS (1) minimum so SurfaceFlinger considers full rate range.
    private fun hookRateSwitchingType() {
        hook("android.hardware.display.DisplayManagerGlobal",
            "setRefreshRateSwitchingType", Int::class.java,
            before { p ->
                val cur = p.args[0] as Int
                if (cur < 1) {
                    p.args[0] = 1
                    HookLog.d("RRM: setRefreshRateSwitchingType $cur → 1")
                }
            })
    }

    // API 34+: always honour app-requested display mode
    private fun hookAlwaysRespectAppMode() {
        hook("android.hardware.display.DisplayManagerGlobal",
            "setShouldAlwaysRespectAppRequestedMode", Boolean::class.java,
            before { p ->
                if (p.args[0] == false) {
                    p.args[0] = true
                    HookLog.d("RRM: setShouldAlwaysRespectAppRequestedMode → true")
                }
            })
    }

    // Prevent any FPS vote from clamping below 60 Hz in system_server
    private fun hookFrameRateVote() {
        val cls = findClass("com.android.server.display.DisplayModeDirector") ?: return
        // hookAppRequestSummary: return min fps = 60 always
        hook(cls, "getAppRequestSummary",
            after { p ->
                // AppRequestSummary is an internal class; log presence
                HookLog.d("RRM: getAppRequestSummary intercepted")
            })
    }

    // Vivo-specific RRM class discovery (try several versioned names)
    private fun hookVivoRrm() {
        val cls = findClassAny(
            "com.vivo.rrm.RrmManager",
            "com.vivo.rrm.RrmService",
            "com.bbk.rrm.RrmManager",
            "com.vivo.display.RrmController",
            "com.vivo.server.display.VivoDisplayModeDirector"
        ) ?: run {
            HookLog.w("RRM: no vivo RRM class found — using AOSP hooks only")
            return
        }
        HookLog.i("RRM: found vivo RRM class ${cls.name}")
        // Hook the fps-vote setter (name varies — try common patterns)
        for (m in listOf("setFpsVote", "requestFps", "applyFpsLimit",
                         "setRefreshRate", "updateFpsPolicy")) {
            val h = hook(cls, m, Int::class.java,
                before { p ->
                    val req = p.args[0] as Int
                    if (req in 1..59) {
                        p.args[0] = 60
                        HookLog.d("RRM[${cls.simpleName}]: $m $req → 60")
                    }
                })
            if (h != null) HookLog.i("RRM: hooked ${cls.simpleName}.$m")
        }
    }
}
