package net.leloubil.common.ui.theme;

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual val acmeFontFamily = FontFamily(
    Font(
        resource = "/font/acme_regular.ttf",
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    )
)

actual val boogalooFontFamily = FontFamily(
    Font(
        resource = "/font/boogaloo_regular.ttf",
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    )

)
