package net.leloubil.werewolvesassistant.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.ui.theme.LocalAccentColorSet
import net.leloubil.werewolvesassistant.ui.theme.Theme
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
    frontSide: Role?,
    wantedSide: CardSide = CardSide.FrontSide,
    overCard: (@Composable BoxScope.() -> Unit) = { },
) {

    val shape = Theme.shapes.card
    val spec = tween<Float>(500, easing = EaseInOutQuad)

    val rotationTransition = remember { Animatable(0f) }
    val shownSide = remember { mutableStateOf(wantedSide) }

    val flipped = shownSide.value == CardSide.BackSide

    val actualRole = when (shownSide.value) {
        CardSide.FrontSide -> frontSide
        CardSide.BackSide -> null
    }
    val scope = rememberCoroutineScope()


    LaunchedEffect(wantedSide) {
        println("Wantedside update, wanted: $wantedSide, shown: ${shownSide.value}, isRunning: ${rotationTransition.isRunning}")
        if(wantedSide == shownSide.value && !rotationTransition.isRunning){
            return@LaunchedEffect
        }

        if (wantedSide == CardSide.FrontSide) {
            scope.launch {
                rotationTransition.animateTo(0f, spec) {
                    if (this.value <= 90f) {
                        shownSide.value = CardSide.FrontSide
                    }
                }
            }
        } else {
            scope.launch {
                rotationTransition.animateTo(180f, spec) {
                    if (this.value > 90f) {
                        shownSide.value = CardSide.BackSide
                    }
                }
            }
        }


    }
    Box(
        Modifier
            .graphicsLayer {
                rotationY = rotationTransition.value
                scaleX = if (flipped) -1f else 1f
                cameraDistance = 25 * density
            }
            .aspectRatio(1f, true)
            .clip(shape)
            .border(
                Theme.spacing.small, LocalAccentColorSet.current.border,
                shape
            )
            .then(modifier)
    ) {
        val (image, desc) = actualRole?.let { it.image to pluralStringResource(it.name, 1) }
            ?: (Res.drawable.card_back to "Face cachée")
        Image(
            painterResource(image), desc,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()

        )

        overCard()

    }
}
