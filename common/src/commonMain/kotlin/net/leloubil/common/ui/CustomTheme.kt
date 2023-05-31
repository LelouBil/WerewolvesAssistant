package net.leloubil.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.leloubil.common.ui.theme.Background
import net.leloubil.common.ui.theme.Dark
import net.leloubil.common.ui.theme.Shapes
import net.leloubil.common.ui.theme.Typography


@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().background(colors.background),
            ) {
                content()
            }
        }
    )
}


private val LightColorPalette = lightColors(
    primary = Dark,
//    primaryVariant = Dark.apply { copy(alpha = 0.5f) },
//    secondary = Teal200,
    background = Background,
    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,     */
)

private val DarkColorPalette = LightColorPalette