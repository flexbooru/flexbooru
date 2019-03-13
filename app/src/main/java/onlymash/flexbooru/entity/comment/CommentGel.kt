package onlymash.flexbooru.entity.comment

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "comment")
data class CommentGel(
    @Attribute
    val id: Int,
    @Attribute
    val created_at: String,
    @Attribute
    val post_id: Int,
    @Attribute
    val creator: String,
    @Attribute
    val creator_id: Int,
    @Attribute
    val body: String
)