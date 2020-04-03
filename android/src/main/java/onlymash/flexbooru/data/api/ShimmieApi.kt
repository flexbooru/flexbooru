package onlymash.flexbooru.data.api

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.common.Keys
import onlymash.flexbooru.common.Values
import onlymash.flexbooru.data.model.shimmie.PostShimmieResponse
import onlymash.flexbooru.extension.getUserAgent
import onlymash.flexbooru.util.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface ShimmieApi {
    companion object {
        
        operator fun invoke(): ShimmieApi {

            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.d("ShimmieApi", message)
                }
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val builder =  chain.request().newBuilder()
                    .removeHeader(Keys.HEADER_USER_AGENT)
                    .addHeader(Keys.HEADER_USER_AGENT, getUserAgent())
                chain.proceed(builder.build())
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
                .baseUrl(Values.BASE_URL)
                .client(client)
                .addConverterFactory(
                    TikXmlConverterFactory.create(
                        TikXml.Builder()
                            .exceptionOnUnreadXml(false)
                            .build()
                    )
                )
                .build()
                .create(ShimmieApi::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url httpUrl: HttpUrl): Response<PostShimmieResponse>
}