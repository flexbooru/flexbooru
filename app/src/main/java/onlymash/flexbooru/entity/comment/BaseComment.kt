package onlymash.flexbooru.entity.comment

abstract class BaseComment {
    abstract fun getPostId(): Int
    abstract fun getCommentId(): Int
    abstract fun getCommentBody(): String
    abstract fun getCommentDate(): CharSequence
    abstract fun getCreatorId(): Int
    abstract fun getCreatorName(): String
}