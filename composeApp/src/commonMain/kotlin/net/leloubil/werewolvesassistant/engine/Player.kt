package net.leloubil.werewolvesassistant.engine

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PlayerName(val name: String)

data class PlayerData(
    val name: PlayerName,
    val role: Role
)
