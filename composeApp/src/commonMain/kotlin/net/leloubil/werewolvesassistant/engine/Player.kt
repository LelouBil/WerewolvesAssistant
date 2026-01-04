package net.leloubil.werewolvesassistant.engine

import kotlin.jvm.JvmInline

@JvmInline
value class PlayerName(val name: String)

data class PlayerData(
    val name: PlayerName,
    val role: Role
)
