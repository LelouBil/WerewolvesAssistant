package net.leloubil.werewolvesassistant.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.launch
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.ui.theme.LocalAccentColorSet
import net.leloubil.werewolvesassistant.ui.theme.border
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.card_back


enum class CardSide {
    FrontSide,
    BackSide,
}

@Composable
fun Carte(
    modifier: Modifier = Modifier,
    front: Role?,
    wantedSide: CardSide = CardSide.FrontSide,
    overFront: (@Composable BoxScope.() -> Unit) = {},
    overBack: (@Composable BoxScope.() -> Unit) = { },
    overBoth: (@Composable BoxScope.() -> Unit) = {},
) {

    val shape = RoundedCornerShape(6)
    val spec = tween<Float>(500, easing = EaseInOutQuad)


    var shownSide by remember { mutableStateOf(wantedSide) }
    val rotationTransition = remember {
        Animatable(
            when (shownSide) {
                CardSide.FrontSide -> 0f
                CardSide.BackSide -> 180f
            }
        )
    }

    val flipped = shownSide == CardSide.BackSide

    val actualRole = when (shownSide) {
        CardSide.FrontSide -> front
        CardSide.BackSide -> null
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(wantedSide) {
        if (wantedSide == shownSide) {
            return@LaunchedEffect
        }

        if (wantedSide == CardSide.FrontSide) {
            scope.launch {
                rotationTransition.animateTo(0f, spec) {
                    if (this.value <= 90f) {
                        shownSide = CardSide.FrontSide
                    }
                }
            }
        } else {
            scope.launch {
                rotationTransition.animateTo(180f, spec) {
                    if (this.value > 90f) {
                        shownSide = CardSide.BackSide
                    }
                }

            }
        }


    }
    val borderPercent = 0.035f
    var borderSize by remember { mutableStateOf(0.dp) }
    val dens = LocalDensity.current
    Box(
        Modifier
            .graphicsLayer {
                rotationY = rotationTransition.value
                scaleX = if (flipped) -1f else 1f
                cameraDistance = 25 * density
            }
            .then(modifier)
            .aspectRatio(1f, true)
            .clip(shape)
            .border(
                borderSize, LocalAccentColorSet.current.border,
                shape
            )
            .onSizeChanged {
                with(dens) {
                    val base = min(it.width.toDp(), it.height.toDp())
                    borderSize = base * borderPercent
                }
            }
    ) {
        val (image, desc) = actualRole?.let { it.image to pluralStringResource(it.name, 1) }
            ?: (Res.drawable.card_back to "Face cachée")
        Image(
            painterResource(image), desc,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()

        )
        if (shownSide == CardSide.BackSide) {
            overBack()
        } else {
            overFront()
        }
        overBoth()

    }
}
