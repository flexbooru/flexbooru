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
) : BaseComment() {
    override fun getPostId(): Int = post_id
    override fun getCommentId(): Int = id
    override fun getCommentBody(): String = body
    override fun getCommentDate(): CharSequence = created_at
    override fun getCreatorId(): Int = creator_id
    override fun getCreatorName(): String = creator
}