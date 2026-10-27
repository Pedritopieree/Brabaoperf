package com.pierre.gcperf

import com.pierre.gcperf.core.HookLog
import com.pierre.gcperf.core.ProfileDb
import com.pierre.gcperf.hooks.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ModuleEntry : IXposedHookLoadPackage {

    companion object {
        val GAMECENTER_PKGS = setOf(
            "com.vivo.gamecenter",
            "com.bbk.gamecenter",
            "com.iqoo.gamecenter"
        )
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        val pkg = param.packageName
        HookLog.d("GCPerf: loading in $pkg")

        when {
            pkg == "android" -> {
                RrmDisplayHooks(param).install()
                ThermalFpsHooks(param).install()
                ThermalWatchHooks(param).install()
                BenchmarkHooks(param).install()
            }
            pkg in GAMECENTER_PKGS -> {
                GameCenterHooks(param).install()
                VsrBackendHooks(param).install()
                FpsOverlayHooks(param).install()
            }
            ProfileDb.isKnownGame(pkg) -> {
                val profile = ProfileDb.get(pkg) ?: return
                FrameHooks(param, profile).install()
                NativeSrHooks(param, profile).install()
                GeneralHooks(param).install()
                HookLog.i("GCPerf: hooks installed for $pkg (${profile.tier}, ${profile.gameFps}fps)")
            }
        }
    }
}
