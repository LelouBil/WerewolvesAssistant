package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class SimpleVillagerTest : FunSpec({

    test("Simple villagers can win by voting out the wolf") {
        val wolf = PlayerName("Wolf")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")

        val players = listOf(
            wolf to Role.Werewolf,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night 1
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException() }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind() }.getOrElse { throw IllegalStateException() }
        game = either { GameStepPrompt.NightEnd.process(game).bind() }.getOrElse { throw IllegalStateException() }

        // Day 1
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either { GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(villager2)).bind() }.getOrElse { throw IllegalStateException() }
        }
        
        val voteResult = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(wolf))
        }.getOrElse { throw IllegalStateException() }

        // Villagers should win
        val result = voteResult.shouldBeInstanceOf<arrow.core.Either.Left<GameEnd.VillagersWon>>().value
        result.winningPlayers shouldBe setOf(villager2)
    }
})
