package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.ColoredIndication


@Suppress("LongParameterList")
fun coloredIndication(
    base: Color,
    hoveredColor: Color = base.copy(alpha = 0.08f),
    focusedColor: Color = base.copy(alpha = 0.1f),
    pressedColor: Color = base.copy(alpha = 0.1f),
    draggedColor: Color = base.copy(alpha = 0.16f),
    animationSpecEnter: AnimationSpec<Float> = snap(),
    animationSpecExit: AnimationSpec<Float> = snap()
): Indication =
    ColoredIndication(
        hoveredColor = hoveredColor,
        focusedColor = focusedColor,
        pressedColor = pressedColor,
        draggedColor = draggedColor,
        animationSpecEnter = animationSpecEnter,
        animationSpecExit = animationSpecExit
    )

sealed interface ColorOrBrush {
    data class ColorValue(val color: Color) : ColorOrBrush
    data class BrushValue(val brush: Brush) : ColorOrBrush
}

data class ColorSet(
    val background: ColorOrBrush,
    val content: Color,
    val textSelection: TextSelectionColors,
    val indication: Indication

) {
    constructor(
        background: Color,
        content: Color,
        textSelection: TextSelectionColors,
        indication: Indication = coloredIndication(background)
    ) : this(
        background = ColorOrBrush.ColorValue(background),
        content = content,
        textSelection = textSelection,
        indication = indication
    )

    companion object {
        val Unspecified = ColorSet(
            background = Color.Unspecified,
            content = Color.Unspecified,
            textSelection = TextSelectionColors(
                handleColor = Color.Unspecified,
                backgroundColor = Color.Unspecified
            ),
        )
    }
}

data class ColorScheme(
    val background: ColorSet,

    val primary: ColorSet,
    val secondary: ColorSet
)

private val defaultColorScheme = ColorScheme(
    background = ColorSet(
//        background = Color(0xFF813434),
        background = ColorOrBrush.BrushValue(
            ShaderBrush(
                makeNoiseShader(
                    freqX = 0.05f,
                    freqY = 0.05f,
                    numOctaves = 100,
                    seed = 0.5f,
                    lerp1 = Color(0xFF813434),
                    lerp2 = Color(0xFF813434),
//            lerp2 = Color.Black,
                    lerpOffset = .42f
                )
            )
        ),
        content = Color(0xFFD7B893),
        textSelection = TextSelectionColors(
            handleColor = Color.Blue,
            backgroundColor = Color.LightGray
        ),
        indication = coloredIndication(Color.Gray)
    ),
    primary = ColorSet(
        background = Color(0xFF0B1316),
        content = Color(0xFFD7B893),
        textSelection = TextSelectionColors(
            handleColor = Color.Yellow,
            backgroundColor = Color.DarkGray
        )
    ),
    secondary = ColorSet(
        background = Color(0xFF55215A),
        content = Color(0xFFCD4021),
        textSelection = TextSelectionColors(
            handleColor = Color.Green,
            backgroundColor = Color.Gray
        ),
        indication = coloredIndication(
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red
        )
    )
)

data class Typography(
    val body: TextStyle,
    val title: TextStyle,
)

private val defaultTypography = Typography(
    body = TextStyle(),
    title = TextStyle(),
)

data class Spacing(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
)

private val defaultSpacing = Spacing(
    small = Dp(4f),
    medium = Dp(8f),
    large = Dp(16f),
)

private val LocalColorScheme = compositionLocalOf { defaultColorScheme }
private val LocalTypography = compositionLocalOf { defaultTypography }
private val LocalSpacing = compositionLocalOf { defaultSpacing }

val LocalAccentColorSet = compositionLocalOf { ColorSet.Unspecified }

object Theme {
    val colors: ColorScheme
        @Composable
        get() = LocalColorScheme.current

    val typography: Typography
        @Composable
        get() = LocalTypography.current

    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current
}

@Composable
fun Modifier.background(colorOrBrush: ColorOrBrush): Modifier = when (colorOrBrush) {
    is ColorOrBrush.ColorValue -> this.background(colorOrBrush.color)
    is ColorOrBrush.BrushValue -> this.background(colorOrBrush.brush)
}


@Composable
fun ProvideContentColorSet(
    colorSet: ColorSet,
    accentColor: ColorSet? = null,
    content: @Composable (Modifier) -> Unit
) {
    val providers = mutableListOf(
        LocalContentColor provides colorSet.content,
        LocalTextSelectionColors provides colorSet.textSelection,
        LocalIndication provides colorSet.indication
    )

    if (accentColor != null) {
        providers.add(LocalAccentColorSet provides accentColor)
    }
    @Suppress("SpreadOperator")
    CompositionLocalProvider(*providers.toTypedArray()) {
        content(Modifier.background(colorSet.background))
    }
}

@Composable
fun WerewolvesTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalColorScheme provides defaultColorScheme,
        LocalTypography provides defaultTypography,
        LocalSpacing provides defaultSpacing
    ) {
        content()
    }
}
