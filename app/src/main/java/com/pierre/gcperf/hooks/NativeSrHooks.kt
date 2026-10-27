package com.pierre.gcperf.hooks

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val TAG = "GCPerf.NativeSr"

object NativeSrHooks {
    fun bind(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val clazz = lpparam.classLoader.loadClass("com.vivo.gameassistant.superresolution.a")
            XposedBridge.hookAllMethods(clazz, "a", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = true
                    Log.d(TAG, "Native SR forced true")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Error", e)
        }
    }
}
