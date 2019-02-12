package onlymash.flexbooru.exoplayer

import android.net.Uri

data class PlayerState(
    var window: Int = 0,
    var position: Long = 0,
    var whenReady: Boolean = true,
    var uri: Uri
)