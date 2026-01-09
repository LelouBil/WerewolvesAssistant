package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix

actual fun makeNoiseShader(
    freqX: Float,
    freqY: Float,
    numOctaves: Int,
    seed: Float,
    lerp1: Color,
    lerp2: Color,
    lerpOffset: Float
): Shader {
    return org.jetbrains.skia.Shader.makeFractalNoise(
        freqX,
        freqY,
        numOctaves,
        seed
    ).makeWithColorFilter(
        ColorFilter.makeMatrix(

            ColorMatrix(
                (lerp2.red - lerp1.red) * 0.5f, 0f, 0f, 0f, lerp1.red - lerpOffset * (lerp2.red - lerp1.red) * 0.5f,
                0f, (lerp2.green - lerp1.green) * 0.5f, 0f, 0f, lerp1.green - lerpOffset * (lerp2.green - lerp1.green) * 0.5f,
                0f, 0f, (lerp2.blue - lerp1.blue) * 0.5f, 0f, lerp1.blue - lerpOffset * (lerp2.blue - lerp1.blue) * 0.5f,
                0f, 0f, 0f, (lerp2.alpha - lerp1.alpha) * 0.5f, lerp1.alpha - lerpOffset * (lerp2.alpha - lerp1.alpha) * 0.5f
            )
        )
    )
}
