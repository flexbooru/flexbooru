package onlymash.flexbooru.entity.comment

data class CommentSankaku(
    val body: String,
    val created_at: String,
    val creator: String,
    val creator_avatar: String?,
    val creator_avatar_rating: String?,
    val creator_id: Int,
    val id: Int,
    val post_id: Int,
    val score: Int
) : CommentBase() {

    var scheme = "https"
    var host = ""

    fun getAvatarUrl() = checkUrl(creator_avatar ?: "")

    private fun checkUrl(url: String): String {
        var u = url
        if (u.contains("""\/""")) {
            u = u.replace("""\/""", "/")
        }
        return when {
            u.startsWith("http") -> u
            u.startsWith("//") -> "$scheme:$u"
            u.startsWith("/") -> "$scheme://$host$u"
            else -> u
        }
    }

    override fun getPostId(): Int = post_id

    override fun getCommentId(): Int = id

    override fun getCommentBody(): String = body

    override fun getCommentDate(): CharSequence = created_at

    override fun getCreatorId(): Int = creator_id

    override fun getCreatorName(): String = creator

}