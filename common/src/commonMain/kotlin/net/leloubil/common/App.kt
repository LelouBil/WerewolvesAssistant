@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import kotlinx.coroutines.*
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.createGameDefinition
import net.leloubil.common.gamelogic.roles.AngelRole
import net.leloubil.common.gamelogic.roles.WerewolfRole
import net.leloubil.common.gamelogic.roles.WitchRole
import org.lighthousegames.logging.logging
import ru.nsk.kstatemachine.visitors.exportToPlantUml

val log = logging()

@Composable
@Preview
fun App() {

    CoroutineScope(Dispatchers.Default).launch {
    val playerList = listOf("1","2","3","4")
    val rolesList = setOf(WerewolfRole(), WerewolfRole(), AngelRole(), WitchRole())
    val gamedef = createGameDefinition(playerList, rolesList)
    log.info { "Exporting state machine" }
        val exported = gamedef.buildStateMachine(this).stateMachine.exportToPlantUml()
        log.info { "Export finished" }
        log.info { "Exported: \n$exported" }
    }

    Button(onClick = {
    }) {
        Text("Salut")
    }

}
