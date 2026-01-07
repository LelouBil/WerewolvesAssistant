package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CupidTest : FunSpec({

    test("Cupid can set lovers and they die together") {
        val wolf = PlayerName("Wolf")
        val cupid = PlayerName("Cupid")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")

        val players = listOf(
            wolf to Role.Werewolf,
            cupid to Role.Cupid,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Cupid turn
        game.nextPrompt shouldBe GameStepPrompt.CupidSetLovers
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.CupidSetLovers
            prompt.process(game, GameStepPrompt.CupidSetLovers.Data(villager1, villager2)).bind()
        }.getOrElse { throw IllegalStateException("Error processing CupidSetLovers: $it") }

        // Wolf turn
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Morning: Villager 1 died by wolf, so Villager 2 should die by love
        game.nextPrompt.shouldBeInstanceOf<GameStepPrompt.DeathByLove>()
        (game.nextPrompt as GameStepPrompt.DeathByLove).dead shouldBe villager2

        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.DeathByLove
            prompt.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing DeathByLove: $it") }

        // Both should be dead
        game.getLivingState(villager1).shouldBeInstanceOf<Game.LivingState.Dead>()
        game.getLivingState(villager2).shouldBeInstanceOf<Game.LivingState.Dead>()
    }

    test("Lovers can win alone") {
        val wolf = PlayerName("Wolf")
        val cupid = PlayerName("Cupid")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            cupid to Role.Cupid,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night 1
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException() }
        game = either { (game.nextPrompt as GameStepPrompt.CupidSetLovers).process(game, GameStepPrompt.CupidSetLovers.Data(wolf, cupid)).bind() }.getOrElse { throw IllegalStateException() }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind() }.getOrElse { throw IllegalStateException() }

        val nightEndResult = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException() }

        val result = nightEndResult.shouldBeInstanceOf<arrow.core.Either.Left<GameEnd.LoversWon>>().value
        result.winningPlayers shouldBe setOf(wolf, cupid)
    }
})
