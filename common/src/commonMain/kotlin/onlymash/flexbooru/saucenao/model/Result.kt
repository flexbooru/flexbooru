package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Result(

    @SerialName("header")
    val header: ResultHeader,

    @SerialName("data")
    val `data`: ResultData
)