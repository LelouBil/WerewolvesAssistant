package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import arrow.core.mapValuesNotNull
import com.composeunstyled.Text
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderState
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
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
import net.leloubil.werewolvesassistant.ui.theme.background
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

    fun assignSwap(player: PlayerName, data: String) {
        _assignments.update {
            val (mapRem, oldPlay) = clearedMap(it, data)

            val role = roles.firstOrNull { (r, s) -> s == data }
            val playerOld = it[player]

            val map = mapRem + (
                    player to role
                    )
            (if (oldPlay != null)
                map + (oldPlay to playerOld)
            else map
                    )
        }
    }

    private fun clearedMap(
        map: Map<PlayerName, Pair<Role, String>?>,
        data: String,
    ): Pair<Map<PlayerName, Pair<Role, String>?>, PlayerName?> {
        val playerWithOldData = map.firstNotNullOfOrNull { (k, v) ->
            if (v != null && v.second == data) {
                k
            } else null
        }
        return if (playerWithOldData != null) {
            map + (playerWithOldData to null) to playerWithOldData
        } else {
            map to null
        }
    }

    fun clearAssignment(data: String) {
        _assignments.update {
            clearedMap(it, data).first
        }
    }


}


@Composable
fun AssignRolesMenu(
    viewModel: AssignRolesViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    preGame: (RolesList) -> Unit,
) {

    val state = rememberReorderState<String>()
    val cardSize = 85.dp

    SharedTransitionScope { modifier ->
        ReorderContainer(state, modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(vertical = Theme.spacing.small).fillMaxHeight()
            ) {
                val absentAlpha = 0.5f
                val presentAlpha = 1f
                val assignments by viewModel.assignments.collectAsState()

                val p1 = assignments.mapValuesNotNull { (_, r) -> r?.first }
                val rolesList = viewModel.players.mapNotNull { pname -> p1[pname]?.let { pname to it } }

                val visualRolesList = viewModel.roles


                //todo mieux https://github.com/MohamedRejeb/compose-dnd
                LazyVerticalGrid(
                    GridCells.FixedSize(cardSize), modifier = Modifier
                        .padding(horizontal = Theme.spacing.medium)
                        .widthIn(max = cardSize * (visualRolesList.size + 1) + Theme.spacing.small * (visualRolesList.size - 1)),
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.small, Alignment.CenterHorizontally)
                ) {
                    items(
                        visualRolesList,
                        key = { (role, i) -> i }) { (role, key) ->
                        val pickedByPlayer = assignments.any { (k, v) -> v?.second == key }


                        Box(
                            Modifier
                                .height(cardSize).aspectRatio(1f).animateItem()

                        ) {
                            Carte(
                                Modifier.fillMaxSize()
                                    .let {
                                        with(sharedTransitionScope) {
                                            it.sharedElement(
                                                this.rememberSharedContentState(key),
                                                animatedVisibilityScope = animatedVisibilityScope
                                            ).then(
                                                if (!pickedByPlayer) {
                                                    Modifier.animateItem()
                                                } else Modifier
                                            )
                                        }
                                    }.alpha(absentAlpha), role
                            )
                            DraggableItemNotLeftBehind(
                                modifier = Modifier.fillMaxSize(),
                                key = key,
                                data = key,
                                state = state,
                                enabled = !pickedByPlayer,
                                dropTargets = viewModel.players.map { it.name },
                                onDrop = {
                                    println("Dropping ${it.data} on ${key} from ${it.key}")
                                    viewModel.clearAssignment(it.data)
                                }
                            ) {
                                if (!pickedByPlayer) {
                                    Carte(
                                        Modifier.fillMaxSize().animateItem(),
                                        role
                                    )
                                }

                            }
                        }

//                }
                    }
                }



                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Theme.spacing.small),
                    modifier = Modifier.fillMaxSize().padding(vertical = Theme.spacing.medium)
                ) {

                    assignments.forEach { (player, p) ->
                        Row(Modifier.height(cardSize), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(player.name, style = Theme.typography.buttonTitle)
                            Box(
                                Modifier.height(cardSize)
                                    .aspectRatio(1f)

                            ) {
                                Carte(Modifier.alpha(absentAlpha), front = null)
                                println("Dragdrop ${p?.second} for $player")
                                DraggableItemNotLeftBehind(
                                    modifier = Modifier.fillMaxSize()
                                        .sharedElementWithCallerManagedVisibility(
                                            this@SharedTransitionScope
                                                .rememberSharedContentState(p?.second ?: ""),
                                            p != null,
                                            zIndexInOverlay = 5f
                                        ),
                                    key = player.name,
                                    state = state,
                                    enabled = p != null,
                                    dropTargets = viewModel.players.filter { it != player }
                                        .map { it.name } + listOf(p?.second ?: ""),
                                    data = p?.second ?: "",
                                    onDrop = {
                                        println("Dropped :${it.data} on $player from ${it.key}")
                                        viewModel.assignSwap(player, it.data)
                                    }
                                ) {
                                    if (p != null) {
                                        Carte(
                                            Modifier.fillMaxSize(),
                                            p.first,
                                            CardSide.FrontSide
                                        )
                                    }
                                }
                            }
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

        }
    }
}

@Composable
fun <T> DraggableItemNotLeftBehind(
    state: ReorderState<T>,
    key: Any,
    data: T,
    enabled: Boolean = true,
    dropTargets: List<Any> = emptyList(),
    modifier: Modifier = Modifier,
    onDrop: (DraggedItemState<T>) -> Unit,
    content: @Composable () -> Unit,
) {
    ReorderableItem(
        modifier = modifier,
        key = key,
        enabled = enabled,
        dropTargets = dropTargets,
        data = data,
        onDrop = onDrop,
        state = state,
        draggableContent = {
            content()
        }
    ) {
        if (!this.isDragging) {
            content()
        }
    }
}

