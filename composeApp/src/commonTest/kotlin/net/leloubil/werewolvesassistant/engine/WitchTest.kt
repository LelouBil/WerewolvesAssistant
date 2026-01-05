package net.leloubil.werewolvesassistant.engine

import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class WitchTest : FunSpec({

    test("Witch can heal a player") {
        val wolf = PlayerName("Wolf")
        val witch = PlayerName("Witch")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            witch to Role.Witch,
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

        // Witch turn
        game.nextPrompt shouldBe GameStepPrompt.WitchShow
        val witchShowInfo = GameStepPrompt.WitchShow.getInfo(game)
        witchShowInfo.killedByWolves shouldBe villager

        game = either {
            GameStepPrompt.WitchShow.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing WitchShow: $it") }

        game.nextPrompt shouldBe GameStepPrompt.WitchStep
        // Witch heals Villager
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WitchStep
            prompt.process(game, GameStepPrompt.WitchStep.Data.Heal(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WitchStep (Heal): $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEndInfo = GameStepPrompt.NightEnd.getInfo(game)
        nightEndInfo.killed shouldBe emptySet()

        game = either {
            GameStepPrompt.NightEnd.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        // Villager should be alive
        game.getLivingState(villager).shouldBeInstanceOf<Game.LivingState.Alive>()

        // Skip Mayor Election if it exists
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(witch)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection") }
        }

        // Village Vote - kill the wolf
        game.nextPrompt shouldBe GameStepPrompt.VillagersKillVote
        val gameEnd = either {
            val prompt = game.nextPrompt as GameStepPrompt.VillagersKillVote
            prompt.process(game, GameStepPrompt.VillagersKillVote.Data(wolf))
        }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote") }

        val result = when(gameEnd) {
            is arrow.core.Either.Left -> gameEnd.value
            is arrow.core.Either.Right -> throw IllegalStateException("Game should have ended")
        }

        result shouldBe GameEnd.VillagersWon(setOf(witch, villager))
    }

    test("Witch can kill a player") {
        val wolf = PlayerName("Wolf")
        val witch = PlayerName("Witch")
        val villager = PlayerName("Villager")

        val players = listOf(
            wolf to Role.Werewolf,
            witch to Role.Witch,
            villager to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night Begin
        game = either {
            GameStepPrompt.NightBegin.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing NightBegin: $it") }

        // Wolf turn (kills no one for simplicity, or just pick someone)
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WerewolvesKill: $it") }

        // Witch turn
        game = either {
            GameStepPrompt.WitchShow.process(game).bind()
        }.getOrElse { throw IllegalStateException("Error processing WitchShow: $it") }

        game.nextPrompt shouldBe GameStepPrompt.WitchStep
        // Witch kills the wolf
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WitchStep
            prompt.process(game, GameStepPrompt.WitchStep.Data.Kill(wolf)).bind()
        }.getOrElse { throw IllegalStateException("Error processing WitchStep (Kill): $it") }

        // Night End
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEndInfo = GameStepPrompt.NightEnd.getInfo(game)
        nightEndInfo.killed shouldBe setOf(villager, wolf)

        val gameEnd = either {
            GameStepPrompt.NightEnd.process(game)
        }.getOrElse { throw IllegalStateException("Error processing NightEnd: $it") }

        val result = when(gameEnd) {
            is arrow.core.Either.Left -> gameEnd.value
            is arrow.core.Either.Right -> throw IllegalStateException("Game should have ended")
        }

        // Only witch remains
        result shouldBe GameEnd.VillagersWon(setOf(witch))
    }

    test("Witch cannot use heal twice") {
        val wolf = PlayerName("Wolf")
        val witch = PlayerName("Witch")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")

        val players = listOf(
            wolf to Role.Werewolf,
            witch to Role.Witch,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // --- First Night ---
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException() }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind() }.getOrElse { throw IllegalStateException() }
        game = either { GameStepPrompt.WitchShow.process(game).bind() }.getOrElse { throw IllegalStateException() }
        // Use heal
        game = either { (game.nextPrompt as GameStepPrompt.WitchStep).process(game, GameStepPrompt.WitchStep.Data.Heal(villager1)).bind() }.getOrElse { throw IllegalStateException() }
        game = either { GameStepPrompt.NightEnd.process(game).bind() }.getOrElse { throw IllegalStateException() }

        // --- Second Night ---
        // Skip day vote
        if (game.nextPrompt == GameStepPrompt.MayorElection) {
            game = either {
                GameStepPrompt.MayorElection.process(game, GameStepPrompt.MayorElection.Data(villager2)).bind()
            }.getOrElse { throw IllegalStateException("Error processing MayorElection") }
        }
        game = either { (game.nextPrompt as GameStepPrompt.VillagersKillVote).process(game, GameStepPrompt.VillagersKillVote.Data(villager2)).bind() }.getOrElse { throw IllegalStateException("Error processing VillagersKillVote") }
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException() }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind() }.getOrElse { throw IllegalStateException() }
        game = either { GameStepPrompt.WitchShow.process(game).bind() }.getOrElse { throw IllegalStateException() }

        // Try to heal again
        val result = either {
            val prompt = game.nextPrompt as GameStepPrompt.WitchStep
            prompt.process(game, GameStepPrompt.WitchStep.Data.Heal(villager1)).bind()
        }
        result.isLeft() shouldBe true
    }
})
