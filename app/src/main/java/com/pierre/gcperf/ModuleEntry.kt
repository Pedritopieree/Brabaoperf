package com.pierre.gcperf

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

// O CONSTRUTOR NÃO PRECISA DE ARGUMENTOS NA API 102
class ModuleEntry : XposedModule() {
    private val TAG = "GCPerf"

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val pkg = param.packageName
        val cl = param.classLoader
        
        if (pkg != "com.vivo.gamecube" && pkg != "com.vivo.gameassistant" && pkg != "com.vivo.gamewatch") {
            return
        }

        Log.d(TAG, "Injetando hooks em $pkg")

        // 1. Hook na classe v6.d (Banco de dados do GameCube)
        try {
            val v6d = cl.loadClass("com.vivo.gamecube.v6.d")
            
            v6d.declaredMethods.filter { it.name == "getInt" }.forEach { m ->
                hook(m).intercept { chain ->
                    val res = chain.proceed() as? Int ?: return@intercept chain.proceed()
                    if (res == 0) {
                        Log.d(TAG, "[v6.d] Forçando retorno de 0 para 1")
                        1
                    } else res
                }
            }
            
            v6d.declaredMethods.filter { it.name == "getString" }.forEach { m ->
                hook(m).intercept { chain ->
                    val res = chain.proceed() as? String
                    if (res == null || res == "0" || res == "false") {
                        Log.d(TAG, "[v6.d] Forçando String para '1'")
                        "1"
                    } else res
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "v6.d não encontrada neste pacote", t)
        }

        // 2. Hook no i9.a (Controlador do Monster+)
        try {
            val i9a = cl.loadClass("com.vivo.gamecube.i9.a")
            i9a.declaredMethods.filter { it.name == "a" }.forEach { m ->
                hook(m).intercept { 
                    Log.d(TAG, "[i9.a] Forçado true")
                    true 
                }
            }
        } catch (t: Throwable) {}

        // 3. Hook na common.a do sistema
        try {
            val commonA = cl.loadClass("com.vivo.common.a")
            commonA.declaredMethods.filter { it.name == "isFeatureSupport" }.forEach { m ->
                hook(m).intercept { chain ->
                    val feature = chain.getArg(0) as? String
                    if (feature != null && feature.lowercase().contains("monster")) {
                        Log.d(TAG, "[common.a] Forçado true pra: $feature")
                        true
                    } else chain.proceed()
                }
            }
        } catch (t: Throwable) {}

        // 4. Hook na UI (ModeShiftBar) pra forçar o botão a aparecer
        try {
            val modeShiftBar = cl.loadClass("com.vivo.gameassistant.homegui.sideslide.panels.performance.modeshift.ModeShiftBar")
            modeShiftBar.declaredMethods.filter { it.name == "setVisibility" }.forEach { m ->
                hook(m).intercept { chain ->
                    val args = chain.args
                    if (args != null && args.isNotEmpty()) {
                        args[0] = 0 // Força VISIBLE (0)
                    }
                    Log.d(TAG, "[ModeShiftBar] setVisibility forçado para VISIBLE (0)")
                    // CORREÇÃO DO TYPEMISMATCH: Passando array em vez de lista
                    chain.proceed(args?.toTypedArray()) 
                }
            }
        } catch (t: Throwable) {}
        
        Log.d(TAG, "Hooks injetados com sucesso!")
    }
}
