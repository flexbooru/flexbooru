package onlymash.flexbooru.entity.common

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("activated")
    val activated: Boolean
)
