package net.leloubil.werewolvesassistant.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.draganddrop.awtTransferable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextDragDrop(data: String): DragAndDropTransferData = DragAndDropTransferData(
    DragAndDropTransferable(StringSelection(data)), supportedActions = listOf(
        DragAndDropTransferAction.Move
    )
)


@OptIn(ExperimentalComposeUiApi::class)
actual fun DragAndDropEvent.getStringData(): String? {
    val awtTransferable = this.awtTransferable
    val transferData = awtTransferable.getTransferData(DataFlavor.stringFlavor)

    if(transferData !is String) return null
    return transferData
}
