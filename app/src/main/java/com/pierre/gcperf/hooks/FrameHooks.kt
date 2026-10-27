package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.GameProfile
import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Per-game frame rate hooks.
 * Runs inside each catalogued game process.
 *
 * Targets:
 *  - Surface.setFrameRate        → raise cap to profile.gameFps
 *  - Window.setPreferredMax/MinRefreshRate → enforce interFps
 *  - SurfaceControl.Transaction.setFrameRate
 *  - Choreographer VSync (legacy fallback)
 */
class FrameHooks(
    lpp: XC_LoadPackage.LoadPackageParam,
    private val profile: GameProfile
) : BaseHook(lpp) {

    override fun install() {
        if (profile.gameFps < 0) return  // SR-only profile
        hookSurfaceSetFrameRate()
        hookWindowRefreshRate()
        hookSurfaceControlTransaction()
    }

    // android.view.Surface.setFrameRate(float, int) — API 30
    // android.view.Surface.setFrameRate(float, int, int) — API 31+
    private fun hookSurfaceSetFrameRate() {
        val cls = findClass("android.view.Surface") ?: return

        val guard = before { p ->
            val req = p.args[0] as Float
            val target = profile.gameFps.toFloat()
            if (req in 0.01f until target) {
                p.args[0] = target
                HookLog.d("Frame[${profile.packageName}]: setFrameRate $req → $target")
            }
        }

        hook(cls, "setFrameRate", Float::class.java, Int::class.java, guard)
        hook(cls, "setFrameRate", Float::class.java, Int::class.java,
             Int::class.java, guard)
    }

    // android.view.Window.setPreferredMaxDisplayRefreshRate / setPreferredMinDisplayRefreshRate
    private fun hookWindowRefreshRate() {
        val cls = findClass("android.view.Window") ?: return
        val interFps = profile.interFps.toFloat()
        val depressFps = profile.depressFps.toFloat()

        hook(cls, "setPreferredMaxDisplayRefreshRate", Float::class.java,
            before { p ->
                val req = p.args[0] as Float
                if (req in 0.01f until interFps) {
                    p.args[0] = interFps
                    HookLog.d("Frame[${profile.packageName}]: setPreferredMax $req → $interFps")
                }
            })

        hook(cls, "setPreferredMinDisplayRefreshRate", Float::class.java,
            before { p ->
                val req = p.args[0] as Float
                if (req > depressFps) {
                    // Game requested a high min; keep it
                } else if (req < depressFps) {
                    p.args[0] = depressFps
                }
            })
    }

    // android.graphics.SurfaceControl.Transaction (API 31+)
    private fun hookSurfaceControlTransaction() {
        val cls = findClass("android.graphics.SurfaceControl\$Transaction") ?: return
        val target = profile.gameFps.toFloat()

        // setFrameRate(SurfaceControl sc, float frameRate, int compatibility, int strategy)
        hook(cls, "setFrameRate",
            android.graphics.SurfaceControl::class.java,
            Float::class.java, Int::class.java, Int::class.java,
            before { p ->
                val req = p.args[1] as Float
                if (req in 0.01f until target) {
                    p.args[1] = target
                    HookLog.d("Frame[SCTx][${profile.packageName}]: setFrameRate $req → $target")
                }
            })
    }
}
