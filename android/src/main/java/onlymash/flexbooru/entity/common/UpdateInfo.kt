package onlymash.flexbooru.entity.common

import com.google.gson.annotations.SerializedName


/**
 * data class for app/update.json
 * */
data class UpdateInfo(
    @SerializedName("version_code")
    val version_code: Long,
    @SerializedName("version_name")
    val version_name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("is_available_store")
    var is_available_store: Boolean = true
)