package net.leloubil.common.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp



// Set of Material typography styles to start with
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = boogalooFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    body1 = TextStyle(
        fontFamily = acmeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    button = TextStyle(
        fontFamily = acmeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = Background,
    ),
    /* Other default text styles to override
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)
