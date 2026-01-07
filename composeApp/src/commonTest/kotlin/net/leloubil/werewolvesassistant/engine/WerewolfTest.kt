package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class WerewolfTest : FunSpec({

    test("Werewolves can win by killing all villagers") {
        val wolf = PlayerName("Wolf")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Wolf turn
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEndResult = either {
            GameStepPrompt.NightEnd.process(game)
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Wolf should win
        val result = nightEndResult.shouldBeInstanceOf<arrow.core.Either.Left<GameEnd.WerewolvesWon>>().value
        result.winningPlayers shouldBe setOf(wolf)
    }
})
