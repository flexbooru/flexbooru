package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Header(

    @SerialName("user_id")
    val userId: String,

    @SerialName("account_type")
    val accountType: String,

    @SerialName("short_limit")
    val shortLimit: String,

    @SerialName("long_limit")
    val longLimit: String,

    @SerialName("long_remaining")
    val longRemaining: Int,

    @SerialName("short_remaining")
    val shortRemaining: Int,

    @SerialName("status")
    val status: Int,

    @SerialName("results_requested")
    val resultsRequested: String,

//    @SerialName("index")
//    val index: List<HeaderIndex>,

    @SerialName("search_depth")
    val searchDepth: String,

    @SerialName("minimum_similarity")
    val minimumSimilarity: Double,

    @SerialName("query_image_display")
    val queryImageDisplay: String,

    @SerialName("query_image")
    val queryImage: String,

    @SerialName("results_returned")
    val resultsReturned: Int
)