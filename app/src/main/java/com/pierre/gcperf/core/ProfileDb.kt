package com.pierre.gcperf.core

import com.pierre.gcperf.core.GameProfile.Tier.*

object ProfileDb {

    private val db: Map<String, GameProfile> = buildMap {
        fun p(pkg: String, idx: Int, gfps: Int, dfps: Int, ifps: Int,
              tier: GameProfile.Tier, sr: Boolean = false) =
            put(pkg, GameProfile(pkg, idx, gfps, dfps, ifps, tier, sr))

        // ── 120 fps power-mode games ───────────────────────────────────
        p("com.tencent.tmgp.sgame",        10, 120, 60, 120, POWER)
        p("com.tencent.tmgp.sgamece",      10, 120, 60, 120, POWER)
        p("com.tencent.tmgp.cf",           13, 120, 60, 120, POWER, sr = true)
        p("com.netease.hyxd.vivo",         21, 120, 60, 120, POWER)
        p("com.netease.hyxd",              21, 120, 60, 120, POWER)
        p("com.tencent.tmgp.speedmobile",  25, 120, 60, 120, POWER)
        p("com.tencent.KiHan",             27, 120, 60, 120, POWER, sr = true)
        p("com.tencent.mf.uam",            23, 120, 60, 120, POWER)
        p("com.tencent.tmgp.WePop",        28, 120, 60, 120, POWER)
        p("com.tencent.tmgp.cod",           9, 144, 60, 120, POWER)

        // ── 90 fps boost-mode games ────────────────────────────────────
        p("com.tencent.tmgp.pubgmhd",       0,  90, 60, 120, BOOST)
        p("com.netease.moba.vivo",          14,  90, 60, 120, BOOST)
        p("com.netease.moba",               14,  90, 60, 120, BOOST)
        p("com.tencent.lolm",               16,  90, 60, 120, BOOST)
        p("com.tencent.tmgp.gnyx",          35,  90, 60, 120, BOOST)

        // ── 60 fps boost-mode games ────────────────────────────────────
        p("com.miHoYo.hkrpg",              29,  60, 45,  90, BOOST)
        p("com.nebulajoy.nero.vivo",         5,  60, 60, 120, BOOST)
        p("com.nebulajoy.nero.opensdk",      5,  60, 60, 120, BOOST)
        p("com.netease.nshm.vivo",          32,  60, 45,  90, BOOST)
        p("com.netease.nshm",               32,  60, 45,  90, BOOST)
        p("com.netease.dfjs.vivo",          31,  60, 45,  90, BOOST)
        p("com.netease.race",               31,  60, 45,  90, BOOST)
        p("com.hermes.h1game",              20,  60, 45,  90, BOOST, sr = true)
        p("com.hermes.h1game.vivo",         20,  60, 45,  90, BOOST, sr = true)
        p("com.pwrd.hotta.laohu",           17,  60, 45,  90, BOOST)
        p("com.games.hotta.vivo",           17,  60, 45,  90, BOOST)
        p("com.netease.mrzh",               19,  60, 45,  90, BOOST)
        p("com.netease.mrzh.vivo",          19,  60, 45,  90, BOOST)
        p("com.netease.mrzh.m4399",         19,  60, 45,  90, BOOST)
        p("com.tencent.tmgp.djsy",          15,  60, 45,  90, BOOST)
        p("com.netease.aceracer",           18,  60, 60, 120, BOOST)
        p("com.netease.aceracer.vivo",      18,  60, 60, 120, BOOST)
        p("com.miHoYo.Yuanshen",             3,  60, 45,  90, BOOST)
        p("com.miHoYo.ys.bilibili",          3,  60, 45,  90, BOOST)
        p("com.tencent.letsgo",             36,  60, 60, 120, BOOST)

        // ── SR-only (no frame rule) ────────────────────────────────────
        p("com.tencent.tmaoe",             -1,  -1, -1,  -1, SR_ONLY, sr = true)
        p("com.tencent.wmsj",              -1,  -1, -1,  -1, SR_ONLY, sr = true)
        p("com.netease.party.vivo",        -1,  -1, -1,  -1, SR_ONLY, sr = true)
        p("com.netease.dwrg",              -1,  -1, -1,  -1, SR_ONLY, sr = true)
        p("com.netease.dwrg5.vivo",        -1,  -1, -1,  -1, SR_ONLY, sr = true)
    }

    fun get(pkg: String): GameProfile? = db[pkg]
    fun isKnownGame(pkg: String): Boolean = db.containsKey(pkg)
    fun all(): List<GameProfile> = db.values.toList()
        .sortedWith(compareByDescending<GameProfile> { it.tier }
            .thenByDescending { it.gameFps })
}
