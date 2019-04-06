package onlymash.flexbooru.api

import android.util.Log
import androidx.annotation.Keep
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.Constants
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.VoteDan
import onlymash.flexbooru.entity.VoteSankaku
import onlymash.flexbooru.entity.comment.CommentResponse
import onlymash.flexbooru.entity.comment.CommentSankaku
import onlymash.flexbooru.entity.pool.PoolSankaku
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.entity.tag.TagSankaku
import onlymash.flexbooru.util.UserAgent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@Keep
interface SankakuApi {
    companion object {
        /**
         * return [SankakuApi]
         * */
        fun create(): SankakuApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("SankakuApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val interceptor = Interceptor {
                val scheme = it.request().url().scheme()
                var host = it.request().url().host()
                if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "chan.")
                it.proceed(it.request()
                    .newBuilder()
                    .addHeader("Origin", "$scheme://$host")
                    .addHeader(Constants.REFERER_KEY, "$scheme://$host/post")
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
                    .build())
            }
            val client = OkHttpClient.Builder().apply {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .addInterceptor(logger)
            }
                .build()
            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SankakuApi::class.java)
        }
    }

    @GET
    fun getPosts(@Url httpUrl: HttpUrl): Call<MutableList<PostSankaku>>

    @GET
    fun getUsers(@Url httpUrl: HttpUrl): Call<MutableList<User>>

    @GET
    fun getPools(@Url httpUrl: HttpUrl): Call<MutableList<PoolSankaku>>

    @GET
    fun getTags(@Url httpUrl: HttpUrl): Call<MutableList<TagSankaku>>

    @FormUrlEncoded
    @POST
    fun favPost(@Url url: String,
                @Field("id") postId: Int,
                @Field("login") username: String,
                @Field("password_hash") passwordHash: String): Call<VoteSankaku>

    @FormUrlEncoded
    @HTTP(method = "POST", hasBody = true)
    fun removeFavPost(@Url url: String,
                      @Field("id") postId: Int,
                      @Field("login") username: String,
                      @Field("password_hash") passwordHash: String): Call<VoteSankaku>


    @GET
    fun getComments(@Url url: HttpUrl): Call<MutableList<CommentSankaku>>

    /* comment/create.json
     */
    @POST
    @FormUrlEncoded
    fun createComment(@Url url: String,
                      @Field("comment[post_id]") postId: Int,
                      @Field("comment[body]") body: String,
                      @Field("comment[anonymous]") anonymous: Int,
                      @Field("login") username: String,
                      @Field("password_hash") passwordHash: String): Call<CommentResponse>



    /* comment/destroy.json
     */
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    fun destroyComment(@Url url: String,
                       @Field("id") commentId: Int,
                       @Field("login") username: String,
                       @Field("password_hash") passwordHash: String): Call<CommentResponse>
}