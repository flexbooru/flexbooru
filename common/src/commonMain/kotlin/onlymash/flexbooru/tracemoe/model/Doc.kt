package onlymash.flexbooru.tracemoe.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Doc(
    @SerialName("anilist_id")
    val anilistId: Int,
    @SerialName("anime")
    var anime: String? = null,
//    @SerialName("episode")
//    val episode: String,
    @SerialName("filename")
    var filename: String? = null,
    @SerialName("from")
    val from: Float,
    @SerialName("to")
    val to: Float,
    @SerialName("at")
    val at: Float,
    @SerialName("is_adult")
    var isAdult: Boolean = false,
    @SerialName("mal_id")
    var malId: Int? = null,
    @SerialName("season")
    var season: String? = null,
    @SerialName("similarity")
    val similarity: Double,
    @SerialName("synonyms")
    var synonyms: List<String> = emptyList(),
    @SerialName("synonyms_chinese")
    var synonymsChinese: List<String> = emptyList(),
    @SerialName("title")
    var title: String? = null,
    @SerialName("title_chinese")
    var titleChinese: String? = null,
    @SerialName("title_english")
    var titleEnglish: String? = null,
    @SerialName("title_native")
    var titleNative: String? = null,
    @SerialName("title_romaji")
    var titleRomaji: String? = null,
    @SerialName("tokenthumb")
    var tokenthumb: String? = null
)