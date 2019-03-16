package onlymash.flexbooru.entity.tag

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "tag")
data class TagGel(
    @Attribute
    val id: Int,
    @Attribute
    val name: String,
    @Attribute
    val count: Int,
    @Attribute
    val type: Int,
    @Attribute
    val ambiguous: Boolean
) : BaseTag() {
    override fun getTagId(): Int = id
    override fun getTagName(): String = name
}