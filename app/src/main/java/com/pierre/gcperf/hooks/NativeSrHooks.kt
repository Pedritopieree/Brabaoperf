package com.pierre.gcperf.hooks

import com.pierre.gcperf.core.GameProfile
import com.pierre.gcperf.core.HookLog
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Super-Resolution hooks for SR-eligible game processes.
 *
 * The original app's libgamecube_tuning.so bridges to vivo's native
 * SR renderer. Here we hook the Java-side entry points that request SR:
 *  - SurfaceView.setResizeMode (scaling trigger)
 *  - android.media.MediaCodec (video upscale path)
 *  - vivo SR service ContentProvider
 */
class NativeSrHooks(
    lpp: XC_LoadPackage.LoadPackageParam,
    private val profile: GameProfile
) : BaseHook(lpp) {

    override fun install() {
        if (!profile.srEnabled) return
        HookLog.i("SR: enabling for ${profile.packageName}")
        hookSurfaceViewScaling()
        hookVivoSrBridge()
    }

    private fun hookSurfaceViewScaling() {
        val cls = findClass("android.view.SurfaceView") ?: return
        // RESIZE_MODE_SCALE_TO_FIT (2) enables vivo SR upscaling
        hook(cls, "setResizeMode", Int::class.java,
            after { p ->
                HookLog.d("SR[SurfaceView]: setResizeMode(${p.args[0]})")
            })
    }

    // Try to call vivo's SR ContentProvider to enable SR for this package
    private fun hookVivoSrBridge() {
        // Hook Application.onCreate to inject SR enable call after app init
        hook("android.app.Application", "onCreate",
            after { _ ->
                try {
                    val ctx = XposedHelpers.callStaticMethod(
                        findClass("android.app.ActivityThread"),
                        "currentApplication"
                    ) as? android.content.Context ?: return@after

                    val uri = android.net.Uri.parse(
                        "content://com.vivo.gamecenter.provider/sr_config"
                    )
                    val cv = android.content.ContentValues().apply {
                        put("package", profile.packageName)
                        put("sr_enabled", 1)
                    }
                    ctx.contentResolver.insert(uri, cv)
                    HookLog.i("SR: injected ContentProvider SR enable for ${profile.packageName}")
                } catch (e: Exception) {
                    HookLog.w("SR: ContentProvider inject failed: ${e.message}")
                }
            })
    }
}
