package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.User


@Serializable
data class UserSankaku(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("current_user")
    val currentUser: CurrentUser,
    @SerialName("filter_content")
    val filterContent: Boolean,
    @SerialName("has_mail")
    val hasMail: Boolean,
    @SerialName("password_hash")
    val passwordHash: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("success")
    val success: Boolean,
    @SerialName("token_type")
    val tokenType: String
) {
    fun toUser(): User {
        return User(
            id = currentUser.id,
            name = currentUser.name,
            token = passwordHash,
            accessToken = accessToken,
            refreshToken = refreshToken,
            avatar = currentUser.avatarUrl
        )
    }
}

@Serializable
data class CurrentUser(
    @SerialName("artist_update_count")
    val artistUpdateCount: Int,
    @SerialName("avatar_rating")
    val avatarRating: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("blacklist_is_hidden")
    val blacklistIsHidden: Boolean,
    @SerialName("blacklisted")
    val blacklisted: List<String>,
    @SerialName("blacklisted_tags")
    val blacklistedTags: List<List<String>>,
    @SerialName("comment_count")
    val commentCount: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("email")
    val email: String,
    @SerialName("email_verification_status")
    val emailVerificationStatus: String,
    @SerialName("favorite_count")
    val favoriteCount: Int,
    @SerialName("favs_are_private")
    val favsArePrivate: Boolean,
    @SerialName("filter_content")
    val filterContent: Boolean,
    @SerialName("forum_post_count")
    val forumPostCount: Int,
    @SerialName("has_mail")
    val hasMail: Boolean,
    @SerialName("hide_ads")
    val hideAds: Boolean,
    @SerialName("id")
    val id: Int,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("last_logged_in_at")
    val lastLoggedInAt: String,
    @SerialName("level")
    val level: Int,
    @SerialName("name")
    val name: String,
    @SerialName("note_update_count")
    val noteUpdateCount: Int,
    @SerialName("pool_favorite_count")
    val poolFavoriteCount: Int,
    @SerialName("pool_update_count")
    val poolUpdateCount: Int,
    @SerialName("pool_upload_count")
    val poolUploadCount: Int,
    @SerialName("pool_vote_count")
    val poolVoteCount: Int,
    @SerialName("post_favorite_count")
    val postFavoriteCount: Int,
    @SerialName("post_update_count")
    val postUpdateCount: Int,
    @SerialName("post_upload_count")
    val postUploadCount: Int,
    @SerialName("post_vote_count")
    val postVoteCount: Int,
    @SerialName("receive_dmails")
    val receiveDmails: Boolean,
    @SerialName("recommended_posts_for_user")
    val recommendedPostsForUser: Int,
    @SerialName("subscription_level")
    val subscriptionLevel: Int,
    @SerialName("upload_limit")
    val uploadLimit: Int,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("wiki_update_count")
    val wikiUpdateCount: Int
)