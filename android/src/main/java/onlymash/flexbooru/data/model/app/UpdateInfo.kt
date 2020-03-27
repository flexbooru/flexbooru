package onlymash.flexbooru.data.model.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * data class for app/update.json
 * */
@Serializable
data class UpdateInfo(
    @SerialName("version_code")
    val versionCode: Long,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("url")
    val url: String,
    @SerialName("is_available_store")
    var isAvailableStore: Boolean = true
)