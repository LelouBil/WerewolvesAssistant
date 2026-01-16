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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import androidx.lifecycle.ViewModel
import com.composeunstyled.Text
import kotlinx.coroutines.delay
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
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration
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
                var lastPos: PointerInputChange? by remember { mutableStateOf(null) }
                var lastCardSize: IntSize? by remember { mutableStateOf(null) }
                LaunchedEffect(isHeldDown) {
                    if (isHeldDown) {
                        if (shownSide == CardSide.FrontSide) {
                            shownSide = CardSide.BackSide
                            return@LaunchedEffect
                        }

//                        val duration = if(lastCardSize != null && lastPos != null) {
//                            val a = lastPos!!.position.round()
//                            println("lasttouchpos: $a")
//                            val b = lastCardSize!!.center
//                            println("lastcardcenter: $b")
//                            val distance = a - b
//                            println("distance a - b: $distance")
//                            val absed = IntOffset(abs(distance.x),abs(distance.y)).toOffset().getDistance()
//                            println("abs: $absed")
//                            val maxMagn = IntOffset(lastCardSize!!.width,lastCardSize!!.height).toOffset().getDistance() / 2f
//                            println("maxmagn: $maxMagn")
//                            val perc = absed / maxMagn
//                            println("perc: $perc")
//                            val time: Float = lerp(1f,2f,perc)
//                            println("time: $time seconds")
//
//                            time.toDouble().seconds
//                        } else {
                          val duration =  2.seconds
//                        }
                        progress.animateTo(
                            1f,
                            tween(durationMillis = duration.toInt(DurationUnit.MILLISECONDS), easing = LinearEasing)
                        )
                        shownSide = CardSide.FrontSide
                        delay(400.milliseconds)
                        progress.snapTo(0f)
                        lastPos = null
                    } else {
                        shownSide = CardSide.BackSide
                        progress.animateTo(
                            0f,
                            tween(
                                durationMillis = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
                                easing = LinearEasing
                            )
                        )
                        lastPos = null
                    }

                }

                Carte(
                    Modifier.weight(0.7f)
                        .padding(Theme.spacing.medium)
                        .onSizeChanged{
                            lastCardSize = it
                        }
                        .pointerInput(role, shownSide) {
                            awaitEachGesture {
                                val thisPos = awaitFirstDown()
                                if (lastPos != null) {
                                    return@awaitEachGesture
                                }
                                lastPos = thisPos
                                isHeldDown = true
                                waitForUpOrCancellation()
                                isHeldDown = false
                            }
                        }, role, shownSide, overBack = {

                        Box(
                            modifier = Modifier
                                .then(
                                    if (lastPos != null) {
                                        Modifier
                                            .align { size, size1, direction ->
                                                if(lastPos != null) {
                                                    IntOffset(
                                                        lastPos!!.position.x.roundToInt() - size.width / 2,
                                                        lastPos!!.position.y.roundToInt() - size.height / 2
                                                    )
                                                } else IntOffset.Zero
                                            }
                                    } else {
                                        Modifier.align(Alignment.Center)
                                    }
                                )

                                .fillMaxSize(1f)
//                                .align(Alignment.Center)
                                .graphicsLayer {
                                    blendMode = BlendMode.Exclusion


                                    val mult = if(lastCardSize != null && lastPos != null) {
                                        val a = lastPos!!.position.round()
//                                        println("lasttouchpos: $a")
                                        val b = lastCardSize!!.center
//                                        println("lastcardcenter: $b")
                                        val distance = a - b
//                                        println("distance a - b: $distance")
                                        val absed = IntOffset(abs(distance.x),abs(distance.y)).toOffset().getDistance()
//                                        println("abs: $absed")
                                        val maxMagn = IntOffset(lastCardSize!!.width,lastCardSize!!.height).toOffset().getDistance() / 2f
//                                        println("maxmagn: $maxMagn")
                                        val perc = absed / maxMagn
//                                        println("perc: $perc")
                                        lerp(1.5f,3f,perc)

                                    } else {
                                        1.5f
                                    }

                                    scaleX = progress.value * mult
                                    scaleY = progress.value * mult
                                }
                                .clip(CircleShape)
                                .background(Color.White)

                        ) {
                        }
                    }
                )

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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                pluralStringResource(role.name, 1),
                style = Theme.typography.buttonTitle
            )
            //todo description du role
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    nextShowIndex()
                }) {
                Text(stringResource(Res.string.show_role_close))
            }
        }
    }
}
