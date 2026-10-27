package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class BaseHook(protected val lpp: XC_LoadPackage.LoadPackageParam) {
    protected val cl: ClassLoader get() = lpp.classLoader

    abstract fun install()

    protected fun findClass(name: String): Class<*>? =
        try { XposedHelpers.findClass(name, cl) }
        catch (e: XposedHelpers.ClassNotFoundError) {
            HookLog.w("${javaClass.simpleName}: class not found: $name"); null
        }

    /** Try several candidate class names, return first match. */
    protected fun findClassAny(vararg names: String): Class<*>? =
        names.firstNotNullOfOrNull { runCatching { XposedHelpers.findClass(it, cl) }.getOrNull() }

    protected fun hook(
        cls: Class<*>, method: String,
        vararg argsAndHook: Any
    ): XC_MethodHook.Unhook? = runCatching {
        XposedHelpers.findAndHookMethod(cls, method, *argsAndHook)
    }.onFailure {
        HookLog.w("${javaClass.simpleName}: failed to hook $method: ${it.message}")
    }.getOrNull()

    protected fun hook(
        className: String, method: String,
        vararg argsAndHook: Any
    ): XC_MethodHook.Unhook? {
        val cls = findClass(className) ?: return null
        return hook(cls, method, *argsAndHook)
    }

    protected fun before(block: (XC_MethodHook.MethodHookParam) -> Unit) =
        object : XC_MethodHook() {
            override fun beforeHookedMethod(p: MethodHookParam) = block(p)
        }

    protected fun after(block: (XC_MethodHook.MethodHookParam) -> Unit) =
        object : XC_MethodHook() {
            override fun afterHookedMethod(p: MethodHookParam) = block(p)
        }

    protected fun replace(block: (XC_MethodHook.MethodHookParam) -> Any?) =
        object : XC_MethodHook() {
            override fun beforeHookedMethod(p: MethodHookParam) {
                p.result = block(p)
            }
        }
}
