package onlymash.flexbooru.entity.post

import com.google.gson.annotations.SerializedName

data class HydrusAuth(
    @SerializedName("basic_permissions")
    val basic_permissions: List<Int>,
    @SerializedName("human_description")
    val human_description: String
)