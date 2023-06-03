package net.leloubil.common.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.leloubil.common.ui.theme.Typography

@Composable
fun Title(text: String) {
    Text(text, style = Typography.h1, modifier = Modifier.padding(vertical = 20.dp))
}