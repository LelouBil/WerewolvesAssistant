package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GuardTest : FunSpec({

    test("Guard can protect a player from werewolves") {
        val wolf = PlayerName("Wolf")
        val guard = PlayerName("Guard")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            guard to Role.Guard,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Guard turn
        game.nextPrompt shouldBe GameStepPrompt.GuardProtect
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.GuardProtect
            prompt.process(game, GameStepPrompt.GuardProtect.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing GuardProtect: $it") }

        // Wolf turn
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Guard Resurrect (logic says if protected player was targeted by wolves, they are resurrected/saved)
        game.nextPrompt shouldBe GameStepPrompt.GuardResurrect
        val guardResurrectInfo = GameStepPrompt.GuardResurrect.getInfo(game)
        guardResurrectInfo.resurrected shouldBe villager

        game = either {
            GameStepPrompt.GuardResurrect.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing GuardResurrect: $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEndInfo = GameStepPrompt.NightEnd.getInfo(game)
        nightEndInfo.killed shouldBe emptySet()

        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Villager should be alive
        game.getLivingState(villager).shouldBeInstanceOf<Game.LivingState.Alive>()
    }

    test("Guard cannot protect the same player twice in a row") {
        val wolf = PlayerName("Wolf")
        val guard = PlayerName("Guard")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")

        val players = listOf(
            wolf to Role.Werewolf,
            guard to Role.Guard,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager,
            PlayerName("Villager 3") to Role.SimpleVillager
        )

        var game = Game(players)!!

        // --- First Night ---
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NightBegin 1") }
        game = either { (game.nextPrompt as GameStepPrompt.GuardProtect).process(game, GameStepPrompt.GuardProtect.Data(villager1)).bind() }.getOrElse { throw IllegalStateException("GuardProtect 1") }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager2)).bind() }.getOrElse { throw IllegalStateException("WerewolvesKill 1") }

        game.nextPrompt shouldBe GameStepPrompt.GuardResurrect
        game = either { GameStepPrompt.GuardResurrect.process(game).bind() }.getOrElse { throw IllegalStateException("GuardResurrect 1: $it") }

        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        game = either { GameStepPrompt.NightEnd.process(game).bind() }.getOrElse { throw IllegalStateException("NightEnd 1: $it") }

        // --- Second Night ---
        // Skip day vote
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(wolf)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection") }
        }
        val dayVoteResult = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(PlayerName("Villager 3")))
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote: $it") }

        game = when(dayVoteResult) {
            is arrow.core.Either.Left -> throw IllegalStateException("Game ended at VillagersKillVote: ${dayVoteResult.value}")
            is arrow.core.Either.Right -> dayVoteResult.value
        }

        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NightBegin 2") }

        // Try to protect villager1 again
        val result = either {
            val prompt = game.nextPrompt as GameStepPrompt.GuardProtect
            prompt.process(game, GameStepPrompt.GuardProtect.Data(villager1)).bind()
        }
        result.isLeft() shouldBe true
    }
})
