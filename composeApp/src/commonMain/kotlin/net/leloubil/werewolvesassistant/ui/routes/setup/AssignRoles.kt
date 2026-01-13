package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import arrow.core.mapValuesNotNull
import com.composeunstyled.Text
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.engine.RolesList
import net.leloubil.werewolvesassistant.ui.CardSide
import net.leloubil.werewolvesassistant.ui.Carte
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam


@KoinViewModel
class AssignRolesViewModel(@InjectedParam val players: List<PlayerName>, @InjectedParam rolesList: List<Role>) :
    ViewModel() {
    val roles = rolesList.groupBy { it }.flatMap { (role, count) ->
        count.mapIndexed { i, c ->
            c to (
                    c::class.qualifiedName + i.toString()
                    )
        }
    }


    private val _assignments: MutableStateFlow<Map<PlayerName, Pair<Role, String>?>> =
        MutableStateFlow(players.associateWith { _ -> null })
    val assignments: StateFlow<Map<PlayerName, Pair<Role, String>?>> = _assignments.asStateFlow()

    fun assign(player: PlayerName, data: String) {
        _assignments.update {
            val mapRem = clearedMap(it, data)

            val role = roles.firstOrNull { (r, s) -> s == data }

            it + (
                    player to role
                    )
        }
    }

    private fun clearedMap(
        map: Map<PlayerName, Pair<Role, String>?>,
        data: String,
    ): Map<PlayerName, Pair<Role, String>?> {
        val playerWithOldData = map.firstNotNullOfOrNull { (k, v) ->
            if (v != null && v.second == data) {
                k
            } else null
        }
        return if (playerWithOldData != null) {
            map + (playerWithOldData to null)
        } else {
            map
        }
    }

    fun clearAssignment(data: String) {
        _assignments.update {
            clearedMap(it, data)
        }
    }


}


@Composable
fun AssignRolesMenu(
    viewModel: AssignRolesViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    preGame: (RolesList) -> Unit,
) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = Theme.spacing.small).fillMaxHeight()
    ) {
        val absentAlpha = 0.5f
        val presentAlpha = 1f
        val assignments by viewModel.assignments.collectAsState()

        val p1 = assignments.mapValuesNotNull { (_, r) -> r?.first }
        val rolesList = viewModel.players.mapNotNull { pname -> p1[pname]?.let { pname to it } }

        val visualRolesList = viewModel.roles

        val state = rememberDragAndDropState<String>()

        DragAndDropContainer(state) {
        //todo mieux https://github.com/MohamedRejeb/compose-dnd
        LazyVerticalGrid(
            GridCells.FixedSize(75.dp), modifier = Modifier
                .padding(horizontal = Theme.spacing.medium)
                .widthIn(max = 75.dp * (visualRolesList.size + 1) + Theme.spacing.small * (visualRolesList.size - 1)),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
        ) {
            items(
                visualRolesList,
                key = { (role, i) -> i }) { (role, key) ->
                val pickedByPlayer = assignments.any { (k, v) -> v?.second == key }
                val shownSide = CardSide.FrontSide
                val visibleRole = role
                    DraggableItem(
                        Modifier.then(
                            if(!pickedByPlayer) {
                                Modifier
                            } else {
                                Modifier
                            }
                        ),
                        key = key, data = key, state = state, enabled = !pickedByPlayer, draggableContent = {
                        Carte(Modifier.fillMaxHeight(),visibleRole, CardSide.FrontSide)
                    })  {
                        Carte(
                            Modifier.animateItem()
                                .fillMaxHeight().let {
                                    with(sharedTransitionScope) {
                                        it.sharedElement(
                                            this.rememberSharedContentState(key),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                    }
                                }
//                            .then(
//                                if (pickedByPlayer)
//                                    Modifier.dragAndDropTarget({
//                                        val data = it.getStringData()
//                                        data == key
//                                    }, target = target)
//                                else {
//                                    Modifier.dragAndDropSource { _ ->
//                                        plainTextDragDrop(key)
//                                    }
//                                }
//                            )
                                .graphicsLayer {
                                    alpha = if (pickedByPlayer) absentAlpha else presentAlpha
                                }, visibleRole, shownSide
                        )
                    }
//                }
                }
            }
        }



        Column {

            assignments.forEach { (player, p) ->
                println("assignment: $player, $p")
                Row(Modifier.height(85.dp)) {
                    Text(player.name, style = Theme.typography.buttonTitle)
//                    val target = remember(player) {
//                        object : DragAndDropTarget {
//                            override fun onDrop(event: DragAndDropEvent): Boolean {
//                                val data = event.getStringData()
//                                if (data != null) {
//                                    viewModel.assign(player, data)
//                                    return true
//                                }
//                                return false
//                            }
//
//                        }
//                    }

                                       Carte(
                        Modifier.graphicsLayer {
                            alpha = if (p == null) absentAlpha else presentAlpha
                        },
                        front = p?.first,
                        wantedSide = if (p == null) CardSide.BackSide else CardSide.FrontSide,
                        overBoth = {
                            Box(Modifier.fillMaxSize().dropTarget(state = state, key = player.name, onDrop = {
                                viewModel.assign(player,it.data)
                            }))
                            if(p != null) {
                                DraggableItem(Modifier.fillMaxSize(),
                                    key = player.name + p.second,
                                    state = state,
                                    dropTargets = viewModel.roles.map { it.second },
                                    data = p.second,
                                    draggableContent = {
                                        Carte(Modifier,p.first, CardSide.FrontSide)
                                    }
                                ) {

                                }
                            }


//                            Box(
//                                Modifier.fillMaxSize().dragAndDropTarget({
//                                    val data = it.getStringData()
//                                    data != null && visualRolesList.any { (r, k) -> k == data }
//
//                                }, target = target).then(
//                                    if (p != null) {
//                                        Modifier.dragAndDropSource { _ ->
//                                            plainTextDragDrop(p.second)
//                                        }
//                                    } else Modifier
//                                )) {
//
//                            }
                        })
                }
            }
        }

        Button(onClick = {

            if (rolesList.size == viewModel.players.size) {
                preGame(rolesList)
            }
        }, enabled = rolesList.size == viewModel.players.size, colorSet = Theme.colors.secondary) {
            Text("Valider")
        }


    }

