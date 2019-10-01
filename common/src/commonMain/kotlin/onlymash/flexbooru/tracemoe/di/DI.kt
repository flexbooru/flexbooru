package onlymash.flexbooru.tracemoe.di

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpCallValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import onlymash.flexbooru.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.tracemoe.api.TraceMoeApiService
import onlymash.flexbooru.tracemoe.model.TraceResponse
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider
import kotlin.reflect.typeOf

@UnstableDefault
@ExperimentalStdlibApi
val kodeinTraceMoe = Kodein {
    bind<String>("TraceMoeBaseUrl") with provider { "https://trace.moe" }
    bind<HttpClient>() with provider {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json.nonstrict).apply {
                    typeOf<TraceResponse>()
                }
            }
            install(HttpCallValidator)
        }
    }
    bind<TraceMoeApi>("TraceMoeApi") with provider {
        val client by kodein.instance<HttpClient>()
        val baseUrl by kodein.instance<String>("TraceMoeBaseUrl")
        TraceMoeApiService(client = client, baseUrl = baseUrl)
    }
}