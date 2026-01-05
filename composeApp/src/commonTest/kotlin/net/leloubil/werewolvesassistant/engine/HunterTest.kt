package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class HunterTest : FunSpec({

    test("Hunter can kill someone when they are killed by the village") {
        val hunter = PlayerName("Hunter")
        val wolf = PlayerName("Wolf")
        val villager = PlayerName("Villager")

        val players = listOf(
            hunter to Role.Hunter,
            wolf to Role.Werewolf,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Skip to Day
        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Skip Werewolves Kill (no one killed for simplicity, or just process it)
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            // We need a victim, let's say no one is killed if possible?
            // Actually WerewolvesKill.Data requires a victim.
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Next should be MayorElection or VillagersKillVote
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(hunter)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection: $it") }
        }

        // Village Vote - kill the hunter
        game.nextPrompt shouldBe GameStepPrompt.VillagersKillVote
        val hunterKillResult = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(hunter))
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote: $it") }

        game = when(hunterKillResult) {
            is Either.Left -> throw IllegalStateException("Game ended prematurely: ${hunterKillResult.value}")
            is Either.Right -> hunterKillResult.value
        }

        // Now Hunter should have a turn to kill
        game.nextPrompt shouldBe GameStepPrompt.HunterKill
        val hunterTurnResult = either {
            val prompt = game.nextPrompt as GameStepPrompt.HunterKill
            prompt.process(game, GameStepPrompt.HunterKill.Data(wolf))
        }.getOrElse { throw IllegalStateException("Error processing HunterKill: $it") }

        val gameEndResult = when(hunterTurnResult) {
            is Either.Left -> hunterTurnResult.value
            is Either.Right -> throw IllegalStateException("Game should have ended")
        }

        // All wolves are dead, so Villagers should win.
        // But who are the winners? Villager was killed by wolves, Hunter was killed by village.
        // Wait, if Villager was killed by wolves, then only Wolf and Hunter were left.
        // If Hunter kills Wolf, then no one is left?
        // Let's re-examine the players and deaths.
        // 1. Villager killed by Werewolves.
        // 2. Hunter killed by Village.
        // 3. Wolf killed by Hunter.
        // Result: No one is alive.
        gameEndResult shouldBe GameEnd.VillagersWon(emptySet())
    }

    test("Hunter kill prompt does not appear if hunter is not killed") {
        val hunter = PlayerName("Hunter")
        val wolf = PlayerName("Wolf")
        val villager = PlayerName("Villager")

        val players = listOf(
            hunter to Role.Hunter,
            wolf to Role.Werewolf,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Skip to Day
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(hunter)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection: $it") }
        }

        // Village Vote - kill the wolf
        game.nextPrompt shouldBe GameStepPrompt.VillagersKillVote
        val wolfKillResult = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(wolf))
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote: $it") }

        val gameEnd = when(wolfKillResult) {
            is Either.Left -> wolfKillResult.value
            is Either.Right -> throw IllegalStateException("Game should have ended since the only wolf is dead")
        }

        gameEnd shouldBe GameEnd.VillagersWon(setOf(hunter))
    }
})
