package com.pierre.gcperf

import android.content.SharedPreferences
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class ModuleEntry : XposedModule() {
    private val TAG = "GCPerf"
    private var hooksApplied = false

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val pkg = param.packageName
        val cl = param.classLoader

        if (pkg != "com.vivo.gamecube" && pkg != "com.vivo.gameassistant" && pkg != "com.vivo.gamewatch") {
            return
        }

        if (hooksApplied) return
        hooksApplied = true
        
        Log.e(TAG, ">>> Iniciando Hooks em $pkg <<<")

        // 1. Força o SharedPreferences a aceitar TUDO que for Monster, Performance ou Game
        try {
            val spClass = cl.loadClass("android.app.SharedPreferencesImpl")
            
            spClass.declaredMethods.filter { it.name == "getBoolean" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = (chain.getArg(0) as? String)?.lowercase()
                    if (key != null && (key.contains("monster") || key.contains("performance") || key.contains("game") || key.contains("touch") || key.contains("turbo"))) {
                        val original = chain.proceed() as? Boolean ?: false
                        if (!original) {
                            Log.e(TAG, "[Prefs] Forçando TRUE: $key")
                            return@intercept true
                        }
                    }
                    chain.proceed()
                }
            }

            spClass.declaredMethods.filter { it.name == "getInt" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = (chain.getArg(0) as? String)?.lowercase()
                    if (key != null && (key.contains("monster") || key.contains("fps") || key.contains("level"))) {
                        val original = chain.proceed() as? Int ?: 0
                        if (original == 0) {
                            Log.e(TAG, "[Prefs] Forçando INT: $key -> 1/144")
                            return@intercept if (key.contains("fps")) 144 else 1
                        }
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Erro Prefs: ${t.message}")
        }

        // 2. Hook na UI do ModeShiftBar (botão do Monster)
        try {
            val modeShiftBar = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeShiftBar")
            // Força qualquer método que retorne boolean a ser true
            modeShiftBar.declaredMethods.filter { it.returnType == Boolean::class.java }.forEach { m ->
                hook(m).intercept { 
                    Log.e(TAG, "[UI] ModeShiftBar.${m.name} -> TRUE")
                    true 
                }
            }
            // Força qualquer setVisibility a ser VISIBLE
            modeShiftBar.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0 // VISIBLE
                        chain.proceed(args.toTypedArray())
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Erro UI: ${t.message}")
        }

        // 3. Hook no ModeType para garantir que o Enum MONSTER_PLUS existe
        try {
            val modeType = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeType")
            modeType.declaredMethods.filter { it.name == "values" }.forEach { m ->
                hook(m).intercept { chain ->
                    val result = chain.proceed() as? Array<*>
                    if (result != null) {
                        Log.e(TAG, "[Enum] ModeType tem ${result.size} itens: ${result.joinToString()}")
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        Log.e(TAG, ">>> Hooks Finalizados em $pkg <<<")
    }
}
