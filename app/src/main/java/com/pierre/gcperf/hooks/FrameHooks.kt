package com.pierre.gcperf.hooks

import android.util.Log
import android.view.SurfaceControl
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "GCPerf.FrameHooks"

object FrameHooks {
    fun bind(lpparam: XC_LoadPackage.LoadPackageParam) {
        val cl = lpparam.classLoader
        try {
            val clazz = cl.loadClass("com.vivo.gameassistant.framedisplay.e")
            XposedBridge.hookAllMethods(clazz, "a", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // Aqui resolvemos o erro do "until" fazendo a porra do cast pra Int
                    val args = param.args
                    if (args != null && args.size >= 2) {
                        val start = args[0] as? Int ?: 0
                        val end = args[1] as? Int ?: 0
                        for (i in start until end) {
                            // Faz alguma porra aqui se precisar
                        }
                    }
                    Log.d(TAG, "Frame hook executed")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Error hooking frame", e)
        }
    }
}
