package com.pierre.gcperf

import android.database.CursorWrapper
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class ModuleEntry : XposedModule() {
    private val TAG = "GCPerf"
    private var isHooked = false

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val pkg = param.packageName
        val cl = param.classLoader

        if (pkg != "com.vivo.gamecube" && pkg != "com.vivo.gameassistant" && pkg != "com.vivo.gamewatch") return
        if (isHooked) return
        isHooked = true
        
        Log.e(TAG, ">>> Atacando banco de dados e UI no pacote $pkg <<<")

        // 1. Fode o SQLite (CursorWrapper) - A porra do GameCube lê daqui
        try {
            val cursorClass = cl.loadClass("android.database.CursorWrapper")
            
            // Força Inteiros (monster_plus_state, touch, etc)
            cursorClass.declaredMethods.filter { it.name == "getInt" }.forEach { m ->
                hook(m).intercept { chain ->
                    val result = chain.proceed() as? Int ?: 0
                    if (result == 0) {
                        StackTraceElement[] stack = Thread.currentThread().stackTrace
                        for (e in stack) {
                            if (e.className.contains("v6.d") || e.className.contains("Database_Impl") || e.className.contains("super_resolution")) {
                                Log.e(TAG, "[SQLite] Forçando getInt -> 1")
                                return@intercept 1
                            }
                        }
                    }
                    result
                }
            }

            // Força Strings (configurações de jogo)
            cursorClass.declaredMethods.filter { it.name == "getString" }.forEach { m ->
                hook(m).intercept { chain ->
                    val result = chain.proceed() as? String
                    StackTraceElement[] stack = Thread.currentThread().stackTrace
                    for (e in stack) {
                        if (e.className.contains("v6.d") || e.className.contains("Database_Impl")) {
                            if (result == null || result == "0" || result == "false") {
                                Log.e(TAG, "[SQLite] Forçando getString -> '1'")
                                return@intercept "1"
                            }
                        }
                    }
                    result
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Erro SQLite: ${t.message}")
        }

        // 2. Força a UI (ModeShiftBar, EdgeDockView)
        try {
            val modeShiftBar = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeShiftBar")
            modeShiftBar.declaredMethods.filter { it.returnType == Boolean::class.java }.forEach { m ->
                hook(m).intercept { true }
            }
            modeShiftBar.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0 // VISIBLE
                        chain.proceed(args.toTypedArray())
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        Log.e(TAG, ">>> Hooks cravados com sucesso! <<<")
    }
}
