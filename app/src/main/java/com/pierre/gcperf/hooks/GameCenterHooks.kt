package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hooks into vivo's GameCenter app.
 * Runs in com.vivo.gamecenter / com.bbk.gamecenter process.
 *
 * Overrides:
 *  - Game rule FPS cap → use catalog values
 *  - Thermal-triggered FPS downgrade → suppress
 *  - GameCenter overlay FPS display → accurate
 *  - Per-app benchmark lease → grant
 */
class GameCenterHooks(lpp: XC_LoadPackage.LoadPackageParam) : BaseHook(lpp) {

    override fun install() {
        HookLog.i("GameCenter: loading in ${lpp.packageName}")
        hookGameRuleManager()
        hookFpsController()
        hookThermalDowngrade()
        hookBenchmarkLease()
    }

    private fun hookGameRuleManager() {
        val cls = findClassAny(
            "com.vivo.gamecenter.engine.GameRuleManager",
            "com.bbk.gamecenter.engine.GameRuleManager",
            "com.vivo.gamecenter.rule.AppRuleManager",
            "com.vivo.gamecenter.service.GameRuleService",
            "com.vivo.game.rule.RuleEngine"
        ) ?: run { HookLog.w("GameCenter: GameRuleManager not found"); return }

        HookLog.i("GameCenter: found ${cls.name}")

        // Hook the method that applies per-app rules (name varies per version)
        for (m in listOf("applyRule", "setGameRule", "applyGameRule",
                         "updateAppRule", "notifyRuleChanged")) {
            hook(cls, m, String::class.java,  // packageName
                after { p ->
                    HookLog.i("GameCenter[rule]: ${cls.simpleName}.$m(${p.args[0]})")
                })
        }

        // Hook getFpsLimit / getFrameRate to return catalog target
        for (m in listOf("getFpsLimit", "getMaxFps", "getGameFps",
                         "getTargetFps", "queryFpsLimit")) {
            hook(cls, m, String::class.java,
                after { p ->
                    val pkg = p.args[0] as? String ?: return@after
                    val profile = com.pierre.gcperf.core.ProfileDb.get(pkg) ?: return@after
                    val current = p.result as? Int ?: return@after
                    if (current in 1 until profile.gameFps) {
                        p.result = profile.gameFps
                        HookLog.i("GameCenter[fps]: override $m($pkg) $current → ${profile.gameFps}")
                    }
                })
        }
    }

    private fun hookFpsController() {
        val cls = findClassAny(
            "com.vivo.gamecenter.fps.FpsController",
            "com.bbk.gamecenter.fps.FpsController",
            "com.vivo.gamecenter.control.FpsManager",
            "com.vivo.game.fps.FrameRateController"
        ) ?: return

        HookLog.i("GameCenter: found FPS controller ${cls.name}")
        for (m in listOf("setFpsMode", "applyFpsCap", "limitFps",
                         "updateFpsTarget", "enforceFpsPolicy")) {
            hook(cls, m, String::class.java, Int::class.java,
                before { p ->
                    val pkg = p.args[0] as? String ?: return@before
                    val fps = p.args[1] as? Int ?: return@before
                    val profile = com.pierre.gcperf.core.ProfileDb.get(pkg) ?: return@before
                    if (fps in 1 until profile.gameFps) {
                        p.args[1] = profile.gameFps
                        HookLog.i("GameCenter[FpsCtrl]: override $m $fps → ${profile.gameFps}")
                    }
                })
        }
    }

    private fun hookThermalDowngrade() {
        val cls = findClassAny(
            "com.vivo.gamecenter.thermal.ThermalFpsPolicy",
            "com.bbk.gamecenter.thermal.ThermalController",
            "com.vivo.gamecenter.power.ThermalFpsManager",
            "com.vivo.game.thermal.ThermalPolicy"
        ) ?: run { HookLog.w("GameCenter: ThermalFpsPolicy not found"); return }

        HookLog.i("GameCenter: found thermal policy ${cls.name}")
        for (m in listOf("onThermalStatusChanged", "reduceFps", "applyThermalPolicy",
                         "downgradeForTemperature", "notifyThermalEvent")) {
            hook(cls, m,
                before { p ->
                    HookLog.d("GameCenter[thermal]: blocked ${cls.simpleName}.$m")
                    p.result = null
                })
        }
    }

    private fun hookBenchmarkLease() {
        val cls = findClassAny(
            "com.vivo.gamecenter.benchmark.BenchmarkLease",
            "com.bbk.gamecenter.benchmark.BenchmarkSession",
            "com.vivo.gamecenter.boost.BenchmarkBoost",
            "com.vivo.game.perf.BenchmarkManager"
        ) ?: return

        HookLog.i("GameCenter: found benchmark class ${cls.name}")
        // Ensure benchmark lease is always granted (isActive / canStart)
        for (m in listOf("isActive", "canActivate", "isGranted", "isEligible")) {
            hook(cls, m, replace { true })
                ?.also { HookLog.i("GameCenter[bench]: ${cls.simpleName}.$m → true") }
        }
    }
}
