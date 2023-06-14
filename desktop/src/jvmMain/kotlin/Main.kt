import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.leloubil.common.App
import net.leloubil.common.components.DefaultRootComponent
import javax.swing.SwingUtilities


@ExperimentalDecomposeApi
fun main()  {
    Napier.base(DebugAntilog())
    val lifecycle = LifecycleRegistry()
    val root = runOnUiThread { DefaultRootComponent(componentContext = DefaultComponentContext(lifecycle)) }

    application {
        val windowState = rememberWindowState()

        // Bind the registry to the life cycle of the window
        LifecycleController(lifecycle, windowState)
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            App(root)
        }
    }
}

internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
