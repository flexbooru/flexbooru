package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SauceNaoResponse(
    @SerialName("header")
    val header: Header,
    @SerialName("results")
    val results: List<Result>
)