package com.pierre.gcperf

import android.content.SharedPreferences
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class ModuleEntry : XposedModule() {
    private val TAG = "GCPerf"

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val pkg = param.packageName
        val cl = param.classLoader

        // Log.e pra forçar a porra do log a aparecer na tela do LSPosed
        Log.e(TAG, ">>> onPackageReady START: $pkg <<<")

        if (pkg != "com.vivo.gamecube" && pkg != "com.vivo.gameassistant" && pkg != "com.vivo.gamewatch" && pkg != "com.android.settings") {
            return
        }

        // 1. Força o SharedPreferences do GameCube a aceitar o Monster+ e Features
        try {
            val spClass = cl.loadClass("android.app.SharedPreferencesImpl")
            
            spClass.declaredMethods.filter { it.name == "getBoolean" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = chain.getArg(0) as? String
                    if (key != null && (key.contains("monster") || key.contains("support") || key.contains("touch"))) {
                        Log.e(TAG, "[Prefs] Forçado TRUE pra: $key")
                        return@intercept true
                    }
                    chain.proceed()
                }
            }

            spClass.declaredMethods.filter { it.name == "getInt" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = chain.getArg(0) as? String
                    if (key != null && (key.contains("monster") || key.contains("touch") || key.contains("resolution") || key.contains("fps"))) {
                        Log.e(TAG, "[Prefs] Forçado 1/100 pra: $key")
                        return@intercept if (key.contains("fps")) 144 else 1
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Erro no Prefs: ${t.message}")
        }

        // 2. Força o Settings Global/System a liberar o Monster
        try {
            val settingsClass = cl.loadClass("android.provider.Settings\$System")
            settingsClass.declaredMethods.filter { it.name == "getInt" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = chain.getArg(1) as? String
                    if (key != null && key.contains("monster")) {
                        Log.e(TAG, "[Settings.System] Forçado 1 pra: $key")
                        return@intercept 1
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        // 3. Hook na UI do ModeShiftBar (botão Monster na tela)
        try {
            // Como o nome pode ter mudado, a gente procura qualquer coisa com "modeshift" ou "performance"
            val classes = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeShiftBar")
            classes.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0 // Força VISIBLE
                        Log.e(TAG, "[UI] ModeShiftBar VISIBLE FORÇADO")
                        chain.proceed(args.toTypedArray())
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "ModeShiftBar não encontrada: ${t.message}")
        }

        // 4. Hook pra forçar o values() do ModeType
        try {
            val modeType = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeType")
            modeType.declaredMethods.filter { it.name == "values" }.forEach { m ->
                hook(m).intercept { chain ->
                    val result = chain.proceed() as Array<*>
                    Log.e(TAG, "[Enum] ModeType values() interceptado. Tamanho: ${result.size}")
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        Log.e(TAG, ">>> onPackageReady END: $pkg <<<")
    }
}
