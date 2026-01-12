package net.leloubil.werewolvesassistant.ui

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent

actual fun plainTextDragDrop(data: String): DragAndDropTransferData = DragAndDropTransferData(
    ClipData(ClipDescription("a",arrayOf("text/plain")), ClipData.Item(data))
)



actual fun DragAndDropEvent.getStringData(): String? {
    val clipData = this.toAndroidDragEvent().clipData
    if(clipData == null) return null
    val data = if (clipData.itemCount == 1) {
        clipData.getItemAt(0).text.toString()
    } else return null
    return data
}
