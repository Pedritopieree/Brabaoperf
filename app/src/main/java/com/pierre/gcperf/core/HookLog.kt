package com.pierre.gcperf.core

import android.util.Log

object HookLog {
    private const val TAG = "GCPerf"

    // Written by hooks into a shared file readable from app UI
    private const val LOG_PATH = "/data/local/tmp/gcperf_hook.log"

    fun d(msg: String) { Log.d(TAG, msg); append("D", msg) }
    fun i(msg: String) { Log.i(TAG, msg); append("I", msg) }
    fun w(msg: String) { Log.w(TAG, msg); append("W", msg) }
    fun e(msg: String) { Log.e(TAG, msg); append("E", msg) }

    private fun append(level: String, msg: String) {
        try {
            val ts = System.currentTimeMillis()
            java.io.FileWriter(LOG_PATH, true).use {
                it.appendLine("[$level $ts] $msg")
            }
        } catch (_: Exception) {}
    }

    fun readLines(n: Int = 500): List<String> =
        try { java.io.File(LOG_PATH).readLines().takeLast(n) }
        catch (_: Exception) { emptyList() }
}
