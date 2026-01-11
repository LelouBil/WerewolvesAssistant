package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.theme.ColoredIndication
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import werewolvesassistant.composeapp.generated.resources.*


@Suppress("LongParameterList")
fun coloredIndication(
    base: Color,
    hoveredColor: Color = base.copy(alpha = 0.08f),
    focusedColor: Color = base.copy(alpha = 0.1f),
    pressedColor: Color = base.copy(alpha = 0.1f),
    draggedColor: Color = base.copy(alpha = 0.16f),
    animationSpecEnter: AnimationSpec<Float> = snap(),
    animationSpecExit: AnimationSpec<Float> = snap(),
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
    fun withAlpha(alpha: Float): ColorOrBrush

    companion object {
        val Unspecified = ColorOrBrush.ColorValue(Color.Unspecified)
    }

    data class ColorValue(val color: Color) : ColorOrBrush {
        override fun withAlpha(alpha: Float): ColorValue = ColorValue(color.copy(alpha = alpha))
    }

    data class BrushValue(val brush: Brush) : ColorOrBrush {
        override fun withAlpha(alpha: Float): ColorOrBrush {
            return BrushValue(
                Brush.composite(
                    brush,
                    ShaderBrush(staticShader(Color.White.copy(alpha = alpha))),
                    BlendMode.Multiply
                )
            )
        }
    }
}

data class ColorSet(
    val background: ColorOrBrush,
    val border: ColorOrBrush,
    val content: Color,
    val textSelection: TextSelectionColors,
    val indication: Indication,

    ) {
    constructor(
        background: Color,
        border: ColorOrBrush,
        content: Color,
        textSelection: TextSelectionColors,
        indication: Indication = coloredIndication(background),
    ) : this(
        background = ColorOrBrush.ColorValue(background),
        border = border,
        content = content,
        textSelection = textSelection,
        indication = indication
    )

    companion object {
        val Unspecified = ColorSet(
            background = Color.Unspecified,
            content = Color.Unspecified,
            border = ColorOrBrush.Unspecified,
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
    val secondary: ColorSet,
) {
    companion object {
        val Unspecified = ColorScheme(
            background = ColorSet.Unspecified,
            primary = ColorSet.Unspecified,
            secondary = ColorSet.Unspecified,
        )
    }
}


data class Typography(
    val body: TextStyle,
    val title: TextStyle,
    val buttonTitle: TextStyle,
) {
    companion object {
        val Unspecified = Typography(
            body = TextStyle.Default,
            title = TextStyle.Default,
            buttonTitle = TextStyle.Default,
        )
    }
}


data class Spacing(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
) {
    companion object {
        val Unspecified = Spacing(
            small = Dp.Unspecified,
            medium = Dp.Unspecified,
            large = Dp.Unspecified,
        )
    }
}

data class Shapes(
    val button: Shape,
    val surface: Shape,
) {
    companion object {
        val Unspecified = Shapes(
            button = RectangleShape,
            surface = RectangleShape,
        )
    }
}

private val LocalColorScheme = compositionLocalOf { ColorScheme.Unspecified }
private val LocalTypography = compositionLocalOf { Typography.Unspecified }
private val LocalSpacing = compositionLocalOf { Spacing.Unspecified }
private val LocalShapes = compositionLocalOf { Shapes.Unspecified }

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

    val shapes: Shapes
        @Composable
        get() = LocalShapes.current
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
    content: @Composable (Modifier) -> Unit,
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

private fun staticShader(color: Color) = LinearGradientShader(
    Offset(0f, 0f),
    Offset(1f, 1f),
    listOf(color, color),
    colorStops = listOf(0f, 1f)
)

@Composable
private fun parchmentShader(intensity: Float, inverted: Boolean, lighten: Float) =
    CompositeShader(
        CompositeShader(
            CompositeShader(
                staticShader(Color.White),
                ImageShader(
                    imageResource(Res.drawable.parchment),
                    tileModeX = TileMode.Repeated,
                    tileModeY = TileMode.Repeated
                ),
                blendMode = BlendMode.Luminosity
            ), staticShader(
                Color(
                    red = lighten,
                    green = lighten,
                    blue = lighten
                )
            ),
            blendMode = BlendMode.Plus
        ).let {
            if (!inverted) {
                it
            } else {
                CompositeShader(staticShader(Color.White), it, BlendMode.Exclusion)
            }
        },
        staticShader(
            Color(
                red = 1f - intensity,
                green = 1f - intensity,
                blue = 1f - intensity,
            )
        ),
        blendMode = BlendMode.Lighten
    )


@Composable
private fun coloredParchmentShader(
    color: Color,
    intensity: Float = 1f,
    inverted: Boolean = false,
    lighten: Float = 0f,
) =
    CompositeShader(
        staticShader(color),
        parchmentShader(intensity, inverted, lighten),
        blendMode = BlendMode.Multiply
    )

private fun lighten(shader: Shader, lighten: Float) =
    CompositeShader(
        staticShader(
            Color(
                red = lighten,
                green = lighten,
                blue = lighten,
            )
        ),
        shader,
        blendMode = BlendMode.Plus
    )

fun Modifier.border(
    width: Dp,
    color: ColorOrBrush,
    shape: Shape,
) = when (color) {
    is ColorOrBrush.BrushValue -> this.border(width, color.brush, shape)
    is ColorOrBrush.ColorValue -> this.border(width, color.color, shape)
}


class SpikyRectangle(
    private val spikeOffsetDp: Dp,
    private val spikeHeight: Dp,
    private val spikeSpacingDp: Dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val spikePx = with(density) { spikeHeight.toPx() }
        val spikeOffset = with(density) { spikeOffsetDp.toPx() }
        val spikeSpacing = with(density) { spikeSpacingDp.toPx() }

        val rectWidth = size.width
        val rectHeight = size.height


        fun spikySide(maxLen: Float, spikePx: Float, offset: Float, draw: (Float, Float) -> Unit) {

            val step = if (maxLen > 0) spikeSpacing else -spikeSpacing
            var accum = 0f
            val comp: (Float, Float) -> Boolean = if (maxLen > 0) { a, b -> a < b } else { a, b -> a > b }
            while (comp(accum + (step * 2), maxLen)) {
                draw(step, 0f)

                draw(-offset, spikePx)
                draw(offset + step, -spikePx)

                accum += step * 2
            }
            draw(maxLen - (accum), 0f)
        }
        spikySide(rectWidth, -spikePx, spikeOffset) { da, db ->
            path.relativeLineTo(da, db)
        }

        spikySide(rectHeight, spikePx, spikeOffset) { da, db ->
            path.relativeLineTo(db, da)
        }
        spikySide(-rectWidth, spikePx, -spikeOffset) { da, db ->
            path.relativeLineTo(da, db)
        }

//        path.relativeLineTo(rectWidth,0f)
//        path.relativeLineTo(0f, rectHeight)
//        path.relativeLineTo(-rectWidth, 0f)
//        path.relativeLineTo(0f, -rectHeight)


        spikySide(-rectHeight, -spikePx, -spikeOffset) { da, db ->
            path.relativeLineTo(db, da)
        }
//        path.moveTo(0f,0f)
//        path.lineTo(rectWidth,0f)
//        path.lineTo(rectHeight,rectWidth)
//        path.lineTo(0f,rectHeight)
//        path.lineTo(0f,0f)


//        path.close()

        return Outline.Generic(path)
    }


}


@Composable
fun WerewolvesTheme(content: @Composable () -> Unit) {


    val defaultSpacing = Spacing(
        small = Dp(4f),
        medium = Dp(8f),
        large = Dp(16f),
    )

    val scalaSansFamily = FontFamily(
        Font(
            Res.font.ScalaSans_Regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        ),
        Font(
            Res.font.ScalaSans_Bold,
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        ),
        Font(
            Res.font.ScalaSans_BoldItalic,
            weight = FontWeight.Bold,
            style = FontStyle.Italic
        )
    )

    val defaultTypography = Typography(
        body = TextStyle(
            fontFamily = scalaSansFamily,
            fontSize = 14.sp,
        ),
        title = TextStyle(
            fontFamily = scalaSansFamily,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        ),
        buttonTitle = TextStyle(
            fontFamily = scalaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    )


    val defaultColorScheme = ColorScheme(
        background = ColorSet(
//        background = Color(0xFF813434),
//            background = ColorOrBrush.BrushValue(
//                ShaderBrush(
//                    makeNoiseShader(
//                        freqX = 0.05f,
//                        freqY = 0.05f,
//                        numOctaves = 100,
//                        seed = 0.5f,
//                        lerp1 = Color(0xFF813434),
//                        lerp2 = Color(0xFF813434),
////            lerp2 = Color.Black,
//                        lerpOffset = .42f
//                    )
//                )
//            ),
            background = ColorOrBrush.BrushValue(
                ShaderBrush(
                    coloredParchmentShader(
                        Color(0xFFF2C88D),
                        intensity = 1f,
                        lighten = 0.25f,
                        inverted = false
                    ),
                )
            ),
            border = ColorOrBrush.ColorValue(Color(0xFF0B1316)),
            content = Color(0xFF0B1316),
            textSelection = TextSelectionColors(
                handleColor = Color.Blue,
                backgroundColor = Color.LightGray
            ),
            indication = coloredIndication(Color.Gray)
        ),
        primary = ColorSet(
            background = ColorOrBrush.BrushValue(
                ShaderBrush(
                    coloredParchmentShader(
                        Color(0xFFB34747),
                        intensity = 0.8f,
                        inverted = false
                    )
                )
            ),

            content = Color(0xFFD7B893),
            border = ColorOrBrush.ColorValue(Color(0xFF0B1316)),
//            content = Color(0xFFD7B893),
            textSelection = TextSelectionColors(
                handleColor = Color.Yellow,
                backgroundColor = Color.DarkGray
            ),
            indication = coloredIndication(Color.Gray)
        ),
        secondary = ColorSet(
            background = Color(0xFF55215A),
            content = Color(0xFFCD4021),
            border = ColorOrBrush.ColorValue(Color(0xFF0B1316)),
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

    val defaultShapes = Shapes(
        button = SpikyRectangle(4.dp, 4.dp, 3.dp),
        surface = RectangleShape,
    )

    CompositionLocalProvider(
        LocalColorScheme provides defaultColorScheme,
        LocalTypography provides defaultTypography,
        LocalSpacing provides defaultSpacing,
        LocalShapes provides defaultShapes,
        LocalTextStyle provides defaultTypography.body
    ) {
        content()
    }
}
