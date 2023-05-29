import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.leloubil.common.App


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
