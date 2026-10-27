package com.pierre.gcperf.core

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("gcperf")

object ConfigKeys {
    val MODULE_ENABLED   = booleanPreferencesKey("module_enabled")
    val THERMAL_BYPASS   = booleanPreferencesKey("thermal_bypass")
    val DISPLAY_RATE_LOCK = booleanPreferencesKey("display_rate_lock")
    val SR_OVERRIDE      = booleanPreferencesKey("sr_override")
    val FRAME_HOOK       = booleanPreferencesKey("frame_hook")
    val LOG_ENABLED      = booleanPreferencesKey("log_enabled")
}

class Config(private val ctx: Context) {
    val isModuleEnabled: Flow<Boolean> =
        ctx.dataStore.data.map { it[ConfigKeys.MODULE_ENABLED] ?: true }
    val thermalBypass: Flow<Boolean> =
        ctx.dataStore.data.map { it[ConfigKeys.THERMAL_BYPASS] ?: true }
    val displayRateLock: Flow<Boolean> =
        ctx.dataStore.data.map { it[ConfigKeys.DISPLAY_RATE_LOCK] ?: true }
    val srOverride: Flow<Boolean> =
        ctx.dataStore.data.map { it[ConfigKeys.SR_OVERRIDE] ?: true }
    val frameHook: Flow<Boolean> =
        ctx.dataStore.data.map { it[ConfigKeys.FRAME_HOOK] ?: true }

    suspend fun set(key: Preferences.Key<Boolean>, value: Boolean) {
        ctx.dataStore.edit { it[key] = value }
    }
}
