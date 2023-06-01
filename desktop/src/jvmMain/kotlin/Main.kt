import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.leloubil.common.App


fun main()  {
    Napier.base(DebugAntilog())
    application {
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
