package net.leloubil.werewolvesassistant.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.uikit.fromString
import androidx.compose.ui.uikit.loadString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import platform.UIKit.UIDragItem

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextDragDrop(data: String): DragAndDropTransferData = DragAndDropTransferData(
    listOf(UIDragItem.fromString(data))
)
@OptIn(ExperimentalComposeUiApi::class)
actual fun DragAndDropEvent.getStringData(): String? {
    if(this.items.size != 1) return null
    val it = items.first()
    val channel = Channel<String?>()
    return runBlocking {
        launch(Dispatchers.IO) {
            it.loadString { string, error ->
                if (string == null || error != null) {
                    channel.trySend(null)
                } else {
                    channel.trySend(string)
                }
            }
        }
        withContext(Dispatchers.IO) {
            channel.receive()
        }
    }
}
