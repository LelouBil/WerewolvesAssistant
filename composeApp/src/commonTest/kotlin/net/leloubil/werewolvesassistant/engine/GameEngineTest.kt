package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GameEngineTest : FunSpec({

    test("1 wolf and 2 villagers game flow") {
        val wolf = PlayerName("Wolf")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")

        val players = listOf(
            wolf to Role.Werewolf,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Initial state
        game.nextPrompt shouldBe GameStepPrompt.NightBegin

        // Start Night
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Checks that no special roles prompt is called
        // Since we only have Werewolf and SimpleVillagers, those shouldn't exist
        GameStepPrompt.CupidSetLovers.exists(game) shouldBe false
        GameStepPrompt.GuardProtect.exists(game) shouldBe false
        GameStepPrompt.SeerSee.exists(game) shouldBe false
        GameStepPrompt.WitchShow.exists(game) shouldBe false

        // Next should be WerewolvesKill
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill

        // Wolf kills Villager 1
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            val data = GameStepPrompt.WerewolvesKill.Data(villager1)
            prompt.process(game, data).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Next should be NightEnd (since no other roles exist)
        game.nextPrompt shouldBe GameStepPrompt.NightEnd

        // Morning comes
        val nightEndInfo = GameStepPrompt.NightEnd.getInfo(game)
        nightEndInfo.killed shouldBe setOf(villager1)

        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Assert villager 1 is dead by the morning
        game.getLivingState(villager1).shouldBeInstanceOf<Game.LivingState.Dead>()
        game.getLivingState(villager2).shouldBeInstanceOf<Game.LivingState.Alive>()
        game.getLivingState(wolf).shouldBeInstanceOf<Game.LivingState.Alive>()

        // Next should be MayorElection or VillagersKillVote
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
             game = either {
                 GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(villager2)).bind()
             }.getOrElse { throw IllegalStateException("Error processing MayorElection: $it") }
        }

        game.nextPrompt shouldBe GameStepPrompt.VillagersKillVote

        // Kill the wolf with the villager vote
        val gameEnd = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            val data = GameStepPrompt.VillagersKillVote.Data(wolf)
            prompt.process(game, data)
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote: $it") }

        // Assert that the game ends with the remaining villager winning
        val result = gameEnd.shouldBeInstanceOf<Either.Left<GameEnd.VillagersWon>>().value
        result.winningPlayers shouldBe setOf(villager2)
    }

})
