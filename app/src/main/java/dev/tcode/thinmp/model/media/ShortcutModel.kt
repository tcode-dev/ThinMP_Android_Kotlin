package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId

data class ShortcutModel(
    var itemId: ShortcutItemId,
    var primaryText: String,
    var secondaryText: String,
    var imageUri: Uri
)