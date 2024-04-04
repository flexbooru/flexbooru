package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.User


@Serializable
data class UserSankaku(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("access_token_ttl")
    val accessTokenTtl: Int, // 604800
    @SerialName("current_user")
    val currentUser: CurrentUser,
    @SerialName("filter_content")
    val filterContent: Boolean, // false
    @SerialName("has_mail")
    val hasMail: Boolean, // true
    @SerialName("password_hash")
    val passwordHash: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("refresh_token_ttl")
    val refreshTokenTtl: Int, // 7776000
    @SerialName("success")
    val success: Boolean, // true
    @SerialName("token_type")
    val tokenType: String // Bearer
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
    val artistUpdateCount: Int, // 0
    @SerialName("avatar_rating")
    val avatarRating: String? = null, // q
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("blacklist_is_hidden")
    val blacklistIsHidden: Boolean, // false
    @SerialName("blacklisted")
    val blacklisted: List<String>,
    @SerialName("blacklisted_tags")
    val blacklistedTags: List<List<String>>,
    @SerialName("comment_count")
    val commentCount: Int, // 1
    @SerialName("created_at")
    val createdAt: String? = null, // 2019-02-09T10:06:16.343Z
    @SerialName("credits")
    val credits: Int, // 0
    @SerialName("credits_subs")
    val creditsSubs: Int, // 0
    @SerialName("email")
    val email: String? = null,
    @SerialName("email_verification_status")
    val emailVerificationStatus: String? = null, // unverified
    @SerialName("favorite_count")
    val favoriteCount: Int, // 7469
    @SerialName("favs_are_private")
    val favsArePrivate: Boolean, // false
    @SerialName("filter_content")
    val filterContent: Boolean, // false
    @SerialName("forum_post_count")
    val forumPostCount: Int, // 0
    @SerialName("has_mail")
    val hasMail: Boolean, // true
    @SerialName("hide_ads")
    val hideAds: Boolean, // true
    @SerialName("id")
    val id: Int, // 885242
    @SerialName("is_verified")
    val isVerified: Boolean, // false
    @SerialName("last_logged_in_at")
    val lastLoggedInAt: String? = null, // 2024-04-04T14:23:34.988Z
    @SerialName("level")
    val level: Int, // 20
    @SerialName("mfa_invalid_times")
    val mfaInvalidTimes: Int, // 0
    @SerialName("mfa_method")
    val mfaMethod: Int, // 0
    @SerialName("name")
    val name: String = "",
    @SerialName("note_update_count")
    val noteUpdateCount: Int, // 0
    @SerialName("pool_favorite_count")
    val poolFavoriteCount: Int, // 0
    @SerialName("pool_update_count")
    val poolUpdateCount: Int, // 0
    @SerialName("pool_upload_count")
    val poolUploadCount: Int, // 0
    @SerialName("pool_vote_count")
    val poolVoteCount: Int, // 0
    @SerialName("post_favorite_count")
    val postFavoriteCount: Int, // 7469
    @SerialName("post_update_count")
    val postUpdateCount: Int, // 0
    @SerialName("post_upload_count")
    val postUploadCount: Int, // 0
    @SerialName("post_vote_count")
    val postVoteCount: Int, // 3
    @SerialName("real_id")
    val realId: Int,
    @SerialName("recommended_posts_for_user")
    val recommendedPostsForUser: Int, // 0
    @SerialName("series_update_count")
    val seriesUpdateCount: Int, // 0
    @SerialName("subscription_level")
    val subscriptionLevel: Int, // 0
    @SerialName("tag_update_count")
    val tagUpdateCount: Int, // 0
    @SerialName("upload_limit")
    val uploadLimit: Int, // 288
    @SerialName("verifications_count")
    val verificationsCount: Int, // 1
    @SerialName("vote_count")
    val voteCount: Int, // 3
    @SerialName("wiki_update_count")
    val wikiUpdateCount: Int // 0
)