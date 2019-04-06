package onlymash.flexbooru.entity.tag

data class TagSankaku(
    val child_tags: String?,
    val count: Int,
    val id: Int,
    val locale: String,
    val name: String,
    val name_en: String,
    val name_ja: String,
    val parent_tags: String?,
    val rating: String?,
    val related_tags: String?,
    val type: Int
) : TagBase() {
    override fun getTagId(): Int = id
    override fun getTagName(): String = name
}