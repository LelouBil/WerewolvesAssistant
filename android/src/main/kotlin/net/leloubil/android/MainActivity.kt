package net.leloubil.android

import net.leloubil.common.App
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.leloubil.common.components.DefaultRootComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Napier.base(DebugAntilog())

        val root = DefaultRootComponent(componentContext = defaultComponentContext())
        setContent {
            MaterialTheme {
                App(root)
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App(DefaultRootComponent(DefaultComponentContext(LifecycleRegistry())))
}
