package onlymash.flexbooru.entity

data class SankakuTag(
    val count: Int,
    val id: Int,
    val locale: String,
    val name: String,
    val name_en: String,
    val name_ja: String?,
    val rating: String?,
    val type: Int
)