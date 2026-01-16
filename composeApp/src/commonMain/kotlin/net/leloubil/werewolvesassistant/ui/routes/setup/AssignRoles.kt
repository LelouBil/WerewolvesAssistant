package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
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
import org.jetbrains.compose.resources.stringResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.clear_button
import werewolvesassistant.composeapp.generated.resources.confirm_button
import werewolvesassistant.composeapp.generated.resources.randomize_button


@KoinViewModel
class AssignRolesViewModel(@InjectedParam val players: List<PlayerName>, @InjectedParam rolesList: List<Role>) :
    ViewModel() {
    val roles = rolesList.groupBy { it }.flatMap { (role, count) ->
        count.mapIndexed { i, c ->
            UniqueRole(c, i)
        }
    }

    private val _assignments: MutableStateFlow<Map<PlayerName, UniqueRole?>> =
        MutableStateFlow(players.associateWith { _ -> null })
    val assignments: StateFlow<Map<PlayerName, UniqueRole?>> = _assignments.asStateFlow()

    fun assignSwap(player: PlayerName, data: UniqueRole) {
        _assignments.update {
            val (mapRem, oldPlay) = clearedMap(it, data)

            val playerOld = it[player]

            val map = mapRem + (player to data)
            (if (oldPlay != null)
                map + (oldPlay to playerOld)
            else map)
        }
    }

    private fun clearedMap(
        map: Map<PlayerName, UniqueRole?>,
        data: UniqueRole,
    ): Pair<Map<PlayerName, UniqueRole?>, PlayerName?> {
        val playerWithOldData = map.firstNotNullOfOrNull { (k, v) ->
            if (v != null && v == data) {
                k
            } else null
        }
        return if (playerWithOldData != null) {
            map + (playerWithOldData to null) to playerWithOldData
        } else {
            map to null
        }
    }

    fun clearAssignment(data: UniqueRole) {
        _assignments.update {
            clearedMap(it, data).first
        }
    }

    fun clearAllAssignments() {
        _assignments.update {
            players.associateWith { _ -> null }
        }
    }

    fun assignPlayersRandomly() {
        val shuffledRoles = roles.shuffled()
        _assignments.value = players.mapIndexed { idx, playerName ->
            playerName to shuffledRoles[idx]
        }.toMap()
    }
}


@Composable
fun AssignRolesMenu(
    viewModel: AssignRolesViewModel,
    navAnimatedVisibilityScope: AnimatedVisibilityScope,
    navSharedTransitionScope: SharedTransitionScope,
    preGame: (RolesList) -> Unit,
) {

    val state = rememberReorderState<UniqueRole?>()
    val cardSize = 85.dp

    val assignments by viewModel.assignments.collectAsState()
    val isAssigned = assignments.mapValuesNotNull { (_, r) -> r }
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        val screenSharedTransitionScope = this@SharedTransitionLayout
        ReorderContainer(state, Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium),
                modifier = Modifier.fillMaxSize().padding(vertical = Theme.spacing.small).fillMaxHeight()
            ) {
                val absentAlpha = 0.5f


                val visualRolesList = viewModel.roles
                LazyVerticalGrid(
                    GridCells.FixedSize(cardSize),
                    horizontalArrangement = Arrangement.spacedBy(
                        Theme.spacing.medium,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium),
                    modifier = Modifier
                        .padding(horizontal = Theme.spacing.medium)
                        .widthIn(
                            max = cardSize * visualRolesList.size + Theme.spacing.medium * (visualRolesList.size - 1)
                        ),
                ) {
                    items(
                        visualRolesList,
                        key = { it.toString() }) {
                        DragDropSourceCard(
                            it,
                            assignments,
                            cardSize,
                            navSharedTransitionScope,
                            navAnimatedVisibilityScope,
                            absentAlpha,
                            state,
                            viewModel,
                            screenSharedTransitionScope,
                        )
                    }

                }

                LazyVerticalGrid(
                    columns = GridCells.FixedSize(cardSize),
                    horizontalArrangement = Arrangement.spacedBy(
                        Theme.spacing.medium,
                        Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .padding(horizontal = Theme.spacing.medium)
                        .widthIn(
                            max = cardSize * (assignments.size) + Theme.spacing.medium * (assignments.size - 1)
                        ),
                ) {
                    items(assignments.toList()) { (player, p) ->
                        PlayerRoleTarget(
                            cardSize,
                            absentAlpha,
                            p,
                            player,
                            state,
                            viewModel,
                            screenSharedTransitionScope,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.medium)
                ) {
                    Button(onClick = {
                        viewModel.assignPlayersRandomly()
                    }) {
                        Text(stringResource(Res.string.randomize_button))
                    }

                    Button(onClick = {
                        viewModel.clearAllAssignments()
                    }, enabled = assignments.any { it.value != null }) {
                        Text(stringResource(Res.string.clear_button))
                    }
                }


                val rolesList = viewModel.players.mapNotNull { pname -> isAssigned[pname]?.let { pname to it.role } }
                Button(onClick = {
                    if (rolesList.size == viewModel.players.size) {
                        preGame(rolesList)
                    }
                }, enabled = rolesList.size == viewModel.players.size, colorSet = Theme.colors.secondary) {
                    Text(stringResource(Res.string.confirm_button))
                }
            }
        }
    }
}

data class CarteSharedKey(
    val role: UniqueRole,
)

@Composable
private fun LazyGridItemScope.DragDropSourceCard(
    role: UniqueRole,
    assignments: Map<PlayerName, UniqueRole?>,
    cardSize: Dp,
    navSharedTransitionScope: SharedTransitionScope,
    navAnimatedVisibilityScope: AnimatedVisibilityScope,
    absentAlpha: Float,
    state: ReorderState<UniqueRole?>,
    viewModel: AssignRolesViewModel,
    screenSharedTransitionScope: SharedTransitionScope,
) {
    val pickedByPlayer = assignments.any { (k, v) -> v == role }

    Box(
        Modifier.height(cardSize).aspectRatio(1f).animateItem()
    ) {
        Carte(
            Modifier.fillMaxSize()
                .let {
                    with(navSharedTransitionScope) {
                        it.sharedElement(
                            this.rememberSharedContentState(role),
                            animatedVisibilityScope = navAnimatedVisibilityScope
                        )
                    }
                }.alpha(absentAlpha), role.role
        )
        DraggableItemNotLeftBehind(
            modifier = Modifier.fillMaxSize(),
            key = role,
            data = role,
            state = state,
            enabled = !pickedByPlayer,
            dropTargets = viewModel.players.map { it.name },
            onDrop = {
                val data = it.data
                println("Dropping ${it.data} on $role from ${it.key}")
                if (data != null)
                    viewModel.clearAssignment(data)
            }
        ) { isPlaceHolder, isDragging, modifier ->
//            if (!pickedByPlayer || screenSharedTransitionScope.isTransitionActive) {
            println("Role: $role, picked: $pickedByPlayer, isDragging: $isDragging")
            val sharedContentState = screenSharedTransitionScope
                .rememberSharedContentState(CarteSharedKey(role))
            Carte(
                modifier
                    .alpha(if(state.draggedItem?.data == role && !isPlaceHolder) 0f else 1f)
                    .then(with(screenSharedTransitionScope) {
                        if (!isPlaceHolder  && state.draggedItem?.data != role) {
                            Modifier.sharedElementWithCallerManagedVisibility(
                                sharedContentState,
                                !pickedByPlayer
                            )
                        } else Modifier
                    })
                    .fillMaxSize(),
                role.role
            )
//            }
        }
    }
}

@Composable
private fun LazyGridItemScope.PlayerRoleTarget(
    cardSize: Dp,
    absentAlpha: Float,
    p: UniqueRole?,
    player: PlayerName,
    state: ReorderState<UniqueRole?>,
    viewModel: AssignRolesViewModel,
    screenSharedTransitionScope: SharedTransitionScope,
) {
    Column(
        Modifier.width(cardSize).animateItem(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.height(cardSize).aspectRatio(1f)
        ) {
            Carte(Modifier.alpha(absentAlpha), front = null)
            println("Dragdrop ${p} for $player")

            DraggableItemNotLeftBehind(
                modifier = Modifier.fillMaxSize(),
                key = player.name,
                state = state,
                enabled = p != null,
                dropTargets = viewModel.players.filter { it != player }
                    .map { it.name } + p?.let { listOf(p) }.orEmpty(),
                data = p,
                onDrop = {
                    val data = it.data
                    if (data != null) {
                        println("Dropped :${data} on $player from ${it.key}")
                        viewModel.assignSwap(player, data)
                    }
                }
            ) { isPlaceHolder, isDragging, modifier ->
                Box(modifier) {
                    viewModel.roles.forEach { shownRole ->
                        val sharedContentState =
                            screenSharedTransitionScope.rememberSharedContentState(
                                CarteSharedKey(shownRole),
                            )
                        println("player: ${player}, p: ${p}, showRole: ${shownRole}")
                        if (!isPlaceHolder || p == shownRole) {
                            Carte(
                                Modifier
                                    .alpha(if(state.draggedItem?.data == shownRole && !isPlaceHolder) 0f else 1f)
                                    .then(with(screenSharedTransitionScope) {
                                        println("Shown role ${player} : ${shownRole} : ${p == shownRole}")
                                        if (!isPlaceHolder && state.draggedItem?.data != shownRole) {
                                            Modifier.sharedElementWithCallerManagedVisibility(
                                                sharedContentState,
                                                p == shownRole,
                                            )
                                        } else Modifier
                                    })
                                    .fillMaxSize(),
                                shownRole.role,
                                CardSide.FrontSide
                            )
                        }
                    }
                }
            }
        }
        Text(
            player.name,
            style = Theme.typography.buttonTitle,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
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
    content: @Composable (isPlaceHolder: Boolean, isDragging: Boolean, modifier: Modifier) -> Unit,
) {
    ReorderableItem(
        modifier = modifier,
        requireFirstDownUnconsumed = false,
        key = key,
        enabled = enabled,
        dropTargets = dropTargets,
        data = data,
        onDrop = onDrop,
        state = state,
        draggableContent = {
            println("drag begin")
            content(true, true, Modifier)
            println("drag end")
        }
    ) {
        content(
            false, this.isDragging,
            if (this.isDragging) {
                Modifier.alpha(0f)
            } else Modifier
        )
    }
}

