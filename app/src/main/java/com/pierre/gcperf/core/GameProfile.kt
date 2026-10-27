package com.pierre.gcperf.core

data class GameProfile(
    val packageName: String,
    val ruleIndex: Int,       // vivo GameCube internal AppRule index
    val gameFps: Int,         // target FPS during gameplay
    val depressFps: Int,      // FPS floor under thermal pressure
    val interFps: Int,        // display Hz for menus / between scenes
    val tier: Tier,
    val srEnabled: Boolean,
) {
    enum class Tier { BOOST, POWER, SR_ONLY }
}
