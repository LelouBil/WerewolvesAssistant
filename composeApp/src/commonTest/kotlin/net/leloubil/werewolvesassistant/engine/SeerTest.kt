package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SeerTest : FunSpec({

    test("Seer can see a player's role") {
        val wolf = PlayerName("Wolf")
        val seer = PlayerName("Seer")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            seer to Role.Seer,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Seer turn
        game.nextPrompt shouldBe GameStepPrompt.SeerSee
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.SeerSee
            prompt.process(game, GameStepPrompt.SeerSee.Data(wolf)).bind()
        }.getOrElse { throw IllegalStateException("Error processing SeerSee: $it") }

        game.nextPrompt shouldBe GameStepPrompt.SeerShow
        val seerInfo = GameStepPrompt.SeerShow.getInfo(game)
        seerInfo.player shouldBe wolf
        seerInfo.role shouldBe Role.Werewolf

        game = either {
            GameStepPrompt.SeerShow.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing SeerShow: $it") }

        // Next should be WerewolvesKill
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Skip Mayor Election if it exists
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(seer)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection: $it") }
        }

        // Village Vote - kill the wolf
        game.nextPrompt shouldBe GameStepPrompt.VillagersKillVote
        val gameEnd = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(wolf))
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote: $it") }

        val result = when(gameEnd) {
            is arrow.core.Either.Left -> gameEnd.value
            is arrow.core.Either.Right -> throw IllegalStateException("Game should have ended")
        }

        result shouldBe GameEnd.VillagersWon(setOf(seer))
    }
})
