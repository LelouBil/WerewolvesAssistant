package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class WhiteWolfTest : FunSpec({

    test("White wolf can kill a werewolf on second night and win alone") {
        val whiteWolf = PlayerName("WhiteWolf")
        val wolf = PlayerName("Wolf")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")
        val villager3 = PlayerName("Villager 3")
        val villager4 = PlayerName("Villager 4")
        val villager5 = PlayerName("Villager 5")

        val players = listOf(
            whiteWolf to Role.WhiteWolf,
            wolf to Role.Werewolf,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager,
            villager3 to Role.SimpleVillager,
            villager4 to Role.SimpleVillager,
            villager5 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // --- First Night ---
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NightBegin 1") }

        // Werewolves turn (White Wolf is part of werewolves)
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind()
        }.getOrElse { throw IllegalStateException("WerewolvesKill 1") }

        // White wolf turn should NOT exist on first night
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEnd1Result = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NightEnd 1") }
        game = nightEnd1Result.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // --- Day 1 ---
        // Left: WW, Wolf, V2, V3, V4, V5 (6 players)

        val mayorElection = either { (game.nextPrompt as GameStepPrompt.MayorElection).process(game, GameStepPrompt.MayorElection.Data(villager1))}.getOrElse { throw IllegalStateException("MayorElection 1") }
        game = mayorElection.shouldBeInstanceOf<Either.Right<Game>>().value

        val dayResult1Result = either { (game.nextPrompt as GameStepPrompt.VillagersKillVote).process(game, GameStepPrompt.VillagersKillVote.Data(villager2)) }.getOrElse { throw IllegalStateException("VKV1") }
        game = dayResult1Result.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // --- Second Night ---
        // Left: WW, Wolf, V3, V4, V5 (5 players)
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NightBegin 2") }

        // Werewolves turn
        game.nextPrompt shouldBe GameStepPrompt.WerewolvesKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WerewolvesKill
            prompt.process(game, GameStepPrompt.WerewolvesKill.Data(villager3)).bind()
        }.getOrElse { throw IllegalStateException("WerewolvesKill 2") }

        // White wolf turn SHOULD exist on second night
        game.nextPrompt shouldBe GameStepPrompt.WhiteWolfKill
        game = either {
            val prompt = game.nextPrompt as GameStepPrompt.WhiteWolfKill
            prompt.process(game, GameStepPrompt.WhiteWolfKill.Data(wolf)).bind()
        }.getOrElse { throw IllegalStateException("WhiteWolfKill 2") }

        // Night 2 End: villager 3 and wolf are killed.
        // Left: whiteWolf, villager 4, villager 5. (3 players)
        val finalResult = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NightEnd 2") }
        game = finalResult.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // Day 2: Villagers kill villager 4
        // Left: WW, V5 (2 players)
        val dayResult2Result = either { (game.nextPrompt as GameStepPrompt.VillagersKillVote).process(game, GameStepPrompt.VillagersKillVote.Data(villager4)) }.getOrElse { throw IllegalStateException("VKV2") }
        game = dayResult2Result.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // Night 3: WW kills V5
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NightBegin 3") }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager5)).bind() }.getOrElse { throw IllegalStateException("WerewolvesKill 3") }

        // Night 3 End
        val nightEnd3Result = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NightEnd 3") }

        // White Wolf should win alone
        val win = nightEnd3Result.shouldBeInstanceOf<arrow.core.Either.Left<GameEnd.WhiteWolfWon>>().value
        win.whiteWolf shouldBe whiteWolf
    }

    test("White wolf win condition") {
        val whiteWolf = PlayerName("WhiteWolf")
        val wolf = PlayerName("Wolf")
        val villager1 = PlayerName("Villager 1")
        val villager2 = PlayerName("Villager 2")
        val villager3 = PlayerName("Villager 3")
        val villager4 = PlayerName("Villager 4")
        val villager5 = PlayerName("Villager 5")

        val players = listOf(
            whiteWolf to Role.WhiteWolf,
            wolf to Role.Werewolf,
            villager1 to Role.SimpleVillager,
            villager2 to Role.SimpleVillager,
            villager3 to Role.SimpleVillager,
            villager4 to Role.SimpleVillager,
            villager5 to Role.SimpleVillager
        )

        var game = Game(players)!!

        // Night 1: Werewolves kill villager 1
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NB1") }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager1)).bind() }.getOrElse { throw IllegalStateException("WK1") }
        // White wolf turn skipped on night 1 (nightCount = 1)
        game.nextPrompt shouldBe GameStepPrompt.NightEnd
        val nightEnd1 = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NE1") }
        game = nightEnd1.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        val mayorElection = either { (game.nextPrompt as GameStepPrompt.MayorElection).process(game, GameStepPrompt.MayorElection.Data(villager1))}.getOrElse { throw IllegalStateException("MayorElection 1") }
        game = mayorElection.shouldBeInstanceOf<Either.Right<Game>>().value


        // Day 1: Villagers kill Villager 2
        val dayResult1 = either { (game.nextPrompt as GameStepPrompt.VillagersKillVote).process(game, GameStepPrompt.VillagersKillVote.Data(villager2)) }.getOrElse { throw IllegalStateException("VKV1") }
        game = dayResult1.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // Night 2: White wolf kills the other wolf
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NB2") }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager3)).bind() }.getOrElse { throw IllegalStateException("WK2") }

        game.nextPrompt shouldBe GameStepPrompt.WhiteWolfKill
        game = either { (game.nextPrompt as GameStepPrompt.WhiteWolfKill).process(game, GameStepPrompt.WhiteWolfKill.Data(wolf)).bind() }.getOrElse { throw IllegalStateException("WWK2") }

        // Night 2 End: villager 3 and wolf are killed.
        // Left: whiteWolf, villager 4, villager 5.
        val result = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NE2") }
        game = result.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // Day 2: Villagers kill villager 4
        val dayResult2 = either { (game.nextPrompt as GameStepPrompt.VillagersKillVote).process(game, GameStepPrompt.VillagersKillVote.Data(villager4)) }.getOrElse { throw IllegalStateException("VKV2") }
        game = dayResult2.shouldBeInstanceOf<arrow.core.Either.Right<Game>>().value

        // Night 3: WW kills V5
        game = either { GameStepPrompt.NightBegin.process(game).bind() }.getOrElse { throw IllegalStateException("NB3") }
        game = either { (game.nextPrompt as GameStepPrompt.WerewolvesKill).process(game, GameStepPrompt.WerewolvesKill.Data(villager5)).bind() }.getOrElse { throw IllegalStateException("WK3") }

        // Night 3 End
        val nightEnd3Result = either { GameStepPrompt.NightEnd.process(game) }.getOrElse { throw IllegalStateException("NE3") }

        // White Wolf should win alone
        val win = nightEnd3Result.shouldBeInstanceOf<arrow.core.Either.Left<GameEnd.WhiteWolfWon>>().value
        win.whiteWolf shouldBe whiteWolf
    }
})
