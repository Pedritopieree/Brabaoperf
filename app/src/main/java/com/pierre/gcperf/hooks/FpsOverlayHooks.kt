package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * FPS counter overlay hooks — runs in GameCenter process.
 * Ensures the overlay shows accurate frame rate after our modifications.
 */
class FpsOverlayHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookFpsReader()
        hookOverlayRenderer()
    }

    private fun hookFpsReader() {
        val cls = findClassAny(
            "com.vivo.gamecenter.overlay.FpsReader",
            "com.bbk.gamecenter.overlay.FpsCounter",
            "com.vivo.gamecenter.fps.FpsMonitor",
            "com.vivo.game.overlay.FpsDisplay"
        ) ?: return

        HookLog.i("FpsOverlay: found ${cls.name}")
        // Log FPS readings for diagnostics
        for (m in listOf("getCurrentFps", "getSmoothedFps", "getInstantFps")) {
            hook(cls, m, after { p ->
                HookLog.d("FpsOverlay: $m = ${p.result}")
            })
        }
    }

    private fun hookOverlayRenderer() {
        val cls = findClassAny(
            "com.vivo.gamecenter.overlay.GameOverlay",
            "com.bbk.gamecenter.overlay.GameHud",
            "com.vivo.game.overlay.OverlayView"
        ) ?: return

        HookLog.i("FpsOverlay: found overlay renderer ${cls.name}")
    }
}
