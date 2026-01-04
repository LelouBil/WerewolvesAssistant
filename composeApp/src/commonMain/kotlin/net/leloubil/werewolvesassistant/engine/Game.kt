package net.leloubil.werewolvesassistant.engine

import arrow.optics.optics

@optics
data class Game(
    val players: Map<PlayerName, PlayerData>,
    val steps: List<GameStepData>,
    val nextPrompt: GameStepPrompt<*,*>,
    val end: GameEnd?
) {
    companion object;
    sealed interface LivingState {
        data class Alive(val cause: GameStepData.MarksAlive?) : LivingState
        data class Dead(val cause: GameStepData.MarksKilled) : LivingState
    }

    fun getLivingState(player: PlayerName): LivingState {
        return steps.asReversed().firstNotNullOfOrNull {
            when (it) {
                is GameStepData.MarksKilled if it.killed.contains(player) -> {
                    LivingState.Dead(it)
                }

                is GameStepData.MarksAlive if it.alive.contains(player) -> {
                    LivingState.Alive(it)
                }

                else -> null
            }
        } ?: LivingState.Alive(null)
    }
}
