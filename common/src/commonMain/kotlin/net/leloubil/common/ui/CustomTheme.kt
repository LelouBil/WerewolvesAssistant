package net.leloubil.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.leloubil.common.ui.theme.*


@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = @Composable {

            Column (
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
    onPrimary = Color.White,
    primaryVariant = Dark,

    secondary = NightBlue,
    onSecondary = Color.Black,
    secondaryVariant = Dark,

    background = Background,
    onBackground = Dark,

    surface = Dark,
    onSurface = Dark,

    error = Color.Black,
    onError = Color.White
)

private val DarkColorPalette = LightColorPalette
