package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * General game-process hooks:
 *  - Audio latency reduction (AudioTrack buffer minimisation)
 *  - Anti-cheat / integrity bypass helpers
 *  - Power hint for game sessions
 */
class GeneralHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        hookAudioLatency()
        hookPowerHint()
    }

    // Force AudioTrack minimum buffer to reduce audio latency in games
    private fun hookAudioLatency() {
        val cls = findClass("android.media.AudioTrack") ?: return
        hook(cls, "getMinBufferSize",
            Int::class.java, Int::class.java, Int::class.java,
            after { p ->
                val orig = p.result as? Int ?: return@after
                val reduced = maxOf(orig / 2, 256)
                if (reduced < orig) {
                    p.result = reduced
                    HookLog.d("General: AudioTrack.getMinBufferSize $orig → $reduced")
                }
            })
    }

    // Deliver a SUSTAINED_PERFORMANCE or GAME power hint for game sessions
    private fun hookPowerHint() {
        val cls = findClass("android.os.PowerManager") ?: return
        // Note: PowerManager.GAME_MODE constant = 6 (API 33+)
        hook(cls, "setGameMode", Int::class.java,
            before { p ->
                val mode = p.args[0] as Int
                HookLog.d("General: PowerManager.setGameMode($mode)")
                // Ensure PERFORMANCE mode (1) not BATTERY_SAVER (4)
                if (mode == 4) p.args[0] = 1
            })
    }
}
