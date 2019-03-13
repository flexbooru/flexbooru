package onlymash.flexbooru.entity.comment

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "comments")
data class CommentGelResponse(
    @Attribute
    val type: String,
    @Element
    val comments: MutableList<CommentGel>?
)