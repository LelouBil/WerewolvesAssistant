package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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

    val state = rememberReorderState<String>()
    val cardSize = 85.dp

    val vmAssignments by viewModel.assignments.collectAsState()
    val p1 = vmAssignments.mapValuesNotNull { (_, r) -> r?.first }
    val rolesList = viewModel.players.mapNotNull { pname -> p1[pname]?.let { pname to it } }
    SharedTransitionScope { modifier ->
            val screenSharedTransitionScope = this@SharedTransitionScope
            ReorderContainer(state, modifier.fillMaxSize()) {
                AnimatedContent(vmAssignments,Modifier.fillMaxSize(), transitionSpec = {EnterTransition.None.togetherWith(
                    ExitTransition.None)}) { assignments ->
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
                                key = { (role, i) -> i }) {
                                DragDropSourceCard(
                                    it.first, it.second,
                                    assignments,
                                    cardSize,
                                    navSharedTransitionScope,
                                    navAnimatedVisibilityScope,
                                    absentAlpha,
                                    state,
                                    viewModel,
                                    screenSharedTransitionScope,
                                    this@AnimatedContent
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
                                    this@AnimatedContent
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
}

@Composable
private fun LazyGridItemScope.DragDropSourceCard(
    role: Role,
    key: String,
    assignments: Map<PlayerName, Pair<Role, String>?>,
    cardSize: Dp,
    navSharedTransitionScope: SharedTransitionScope,
    navAnimatedVisibilityScope: AnimatedVisibilityScope,
    absentAlpha: Float,
    state: ReorderState<String>,
    viewModel: AssignRolesViewModel,
    screenSharedTransitionScope: SharedTransitionScope,
    screenAnimatedContentScope: AnimatedContentScope
) {
    val pickedByPlayer = assignments.any { (k, v) -> v?.second == key }

    Box(
        Modifier.height(cardSize).aspectRatio(1f).animateItem()
    ) {
        Carte(
            Modifier.fillMaxSize().animateItem()
                .let {
                    with(navSharedTransitionScope) {
                        it.sharedElement(
                            this.rememberSharedContentState(key),
                            animatedVisibilityScope = navAnimatedVisibilityScope
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
                println("Dropping ${it.data} on $key from ${it.key}")
                viewModel.clearAssignment(it.data)
            }
        ) {
            if(!pickedByPlayer) {
                Carte(
                    Modifier.animateItem()
                        .then(with(screenSharedTransitionScope) {
                            println("sharedelem role: $key, ${it != null}")
                                Modifier.sharedElement(
                                    screenSharedTransitionScope
                                        .rememberSharedContentState(key),
                                    screenAnimatedContentScope
                                )
                        })
                        .fillMaxSize(),
                    role
                )
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.PlayerRoleTarget(
    cardSize: Dp,
    absentAlpha: Float,
    p: Pair<Role, String>?,
    player: PlayerName,
    state: ReorderState<String>,
    viewModel: AssignRolesViewModel,
    screenSharedTransitionScope: SharedTransitionScope,
    screenAnimatedContent: AnimatedContentScope
) {
    Column(
        Modifier.width(cardSize).animateItem(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.height(cardSize).aspectRatio(1f)
        ) {
            Carte(Modifier.alpha(absentAlpha), front = null)
            println("Dragdrop ${p?.second} for $player")
            DraggableItemNotLeftBehind(
                modifier = Modifier.fillMaxSize(),
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
                if(p != null) {
                    Carte(
                        it
                            .then(with(screenSharedTransitionScope) {
                                Modifier.sharedElement(
                                    screenSharedTransitionScope.rememberSharedContentState(p?.second ?: ""),
                                    screenAnimatedContent,
                                )
                            })
                            .fillMaxSize(),
                        p?.first,
                        CardSide.FrontSide
                    )
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
    content: @Composable (Modifier) -> Unit,
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
            content(Modifier)
        }
    ) {
        content(if(this.isDragging) {Modifier.alpha(0f)} else Modifier)
    }
}

