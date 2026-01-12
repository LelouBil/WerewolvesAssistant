package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.ViewModel
import com.composeunstyled.Text
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.engine.RolesList
import net.leloubil.werewolvesassistant.ui.CardSide
import net.leloubil.werewolvesassistant.ui.Carte
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import werewolvesassistant.composeapp.generated.resources.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@KoinViewModel
class PreGameShowRolesViewModel(@InjectedParam val players: RolesList, @InjectedParam val showingIndex: UInt?) :
    ViewModel() {

    val currentRole = showingIndex?.let { players[it.toInt()] }

}

@Composable
fun PreGameShowRoles(viewModel: PreGameShowRolesViewModel, nextShowIndex: () -> Unit) =
    Column(Modifier.padding(Theme.spacing.large)) {
        val pair = viewModel.currentRole

        if (pair == null) {
            Column {
//            Text(stringResource(Res.string.show_roles_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.show_roles_title))

                Button(onClick = {
                    nextShowIndex()
                }) {
                    Text(stringResource(Res.string.show_roles_start_button))
                }
            }
        } else {
            val (player, role) = pair

            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(Res.string.show_roles_player_text, player.name),
                    style = Theme.typography.title
                )
                var shownSide by remember { mutableStateOf(CardSide.BackSide) }
                val progress = remember { Animatable(0f) }
                var isHeldDown by remember { mutableStateOf(false) }
                LaunchedEffect(isHeldDown) {
                    if (isHeldDown) {
                        progress.animateTo(
                            1f,
                            tween(durationMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing)
                        )
                        shownSide = CardSide.FrontSide
                        progress.snapTo(0f)
                    } else {
                        shownSide = CardSide.BackSide
                        progress.animateTo(
                            0f,
                            tween(
                                durationMillis = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
                                easing = LinearEasing
                            )
                        )
                    }

                }
                Box(Modifier.weight(0.7f).padding(Theme.spacing.medium)) {
                    Carte(
                        Modifier.fillMaxSize()
                            .pointerInput(role, shownSide) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    isHeldDown = true
                                    waitForUpOrCancellation()
                                    isHeldDown = false
                                }
                            }, role, shownSide
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize(1f)
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    blendMode = BlendMode.Exclusion
                                    scaleX = progress.value * 1.5f
                                    scaleY = progress.value * 1.5f
                                }
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                        }
                    }
                }

                Box(Modifier.weight(0.3f)) {
                    function(shownSide, role, nextShowIndex)
                }
            }
        }
    }

@Composable
private fun BoxScope.function(
    shownSide: CardSide,
    role: Role,
    nextShowIndex: () -> Unit,
) {
    AnimatedVisibility(
        shownSide == CardSide.FrontSide,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                pluralStringResource(role.name, 1),
                style = Theme.typography.buttonTitle
            )
            //todo description du role
            Button(
                modifier =  Modifier.align(Alignment.End),
                onClick = {
                    nextShowIndex()
                }) {
                Text(stringResource(Res.string.show_role_close))
            }
        }
    }
}
