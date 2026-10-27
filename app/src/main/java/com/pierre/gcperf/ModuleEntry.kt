package com.pierre.gcperf

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

        if (pkg != "com.vivo.gamecube" && pkg != "com.vivo.gameassistant" && pkg != "com.vivo.gamewatch") {
            return
        }

        if (isHooked) return
        isHooked = true
        
        Log.e(TAG, ">>> Cravando hooks no pacote $pkg <<<")

        // 1. Força o ModeShiftBar (Botão do Monster) a ficar VISIBLE e retornar TRUE
        try {
            val modeShiftBar = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeShiftBar")
            
            // Força qualquer método boolean a ser true
            modeShiftBar.declaredMethods.filter { it.returnType == Boolean::class.java }.forEach { m ->
                hook(m).intercept { 
                    Log.e(TAG, "[UI] ModeShiftBar.${m.name} -> TRUE")
                    true 
                }
            }
            
            // Força qualquer setVisibility a ser 0 (VISIBLE)
            modeShiftBar.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0
                        Log.e(TAG, "[UI] ModeShiftBar.setVisibility -> VISIBLE")
                        chain.proceed(args.toTypedArray())
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Falha ModeShiftBar: ${t.message}")
        }

        // 2. Força o EdgeDockView (A barra lateral inteira)
        try {
            val edgeDock = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.dock.view.EdgeDockView")
            edgeDock.declaredMethods.filter { it.returnType == Boolean::class.java }.forEach { m ->
                hook(m).intercept { 
                    Log.e(TAG, "[UI] EdgeDockView.${m.name} -> TRUE")
                    true 
                }
            }
            edgeDock.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0
                        chain.proceed(args.toTypedArray())
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        // 3. Força o ModeType (Enum) a retornar a porra do MONSTER_PLUS se existir
        try {
            val modeType = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeType")
            modeType.declaredMethods.filter { it.name == "values" }.forEach { m ->
                hook(m).intercept { chain ->
                    val result = chain.proceed() as? Array<*>
                    if (result != null) {
                        Log.e(TAG, "[ENUM] ModeType tem ${result.size} itens")
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        // 4. Força o SharedPreferences a mentir
        try {
            val spClass = cl.loadClass("android.app.SharedPreferencesImpl")
            spClass.declaredMethods.filter { it.name == "getBoolean" }.forEach { m ->
                hook(m).intercept { chain ->
                    val key = (chain.getArg(0) as? String)?.lowercase()
                    if (key != null && (key.contains("monster") || key.contains("support") || key.contains("touch"))) {
                        Log.e(TAG, "[Prefs] Forçado TRUE: $key")
                        return@intercept true
                    }
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        Log.e(TAG, ">>> Hooks Finalizados em $pkg <<<")
    }
}
