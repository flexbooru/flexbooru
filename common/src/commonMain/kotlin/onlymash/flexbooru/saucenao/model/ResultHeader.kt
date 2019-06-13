package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultHeader(

    @SerialName("similarity")
    val similarity: String,

    @SerialName("thumbnail")
    val thumbnail: String,

    @SerialName("index_id")
    val indexId: Int,

    @SerialName("index_name")
    val indexName: String
)