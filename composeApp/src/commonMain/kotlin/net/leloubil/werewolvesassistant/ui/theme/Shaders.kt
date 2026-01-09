package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader

expect fun makeNoiseShader(
    freqX: Float,
    freqY: Float,
    numOctaves: Int,
    seed: Float,
    lerp1: Color,
    lerp2: Color,
    lerpOffset: Float
): Shader
