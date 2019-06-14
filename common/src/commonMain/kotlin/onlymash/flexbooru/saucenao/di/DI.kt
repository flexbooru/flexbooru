package onlymash.flexbooru.saucenao.di

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpCallValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import onlymash.flexbooru.saucenao.api.SauceNaoApi
import onlymash.flexbooru.saucenao.api.SauceNaoApiService
import onlymash.flexbooru.saucenao.model.SauceNaoResponse
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider

@UnstableDefault
val kodein = Kodein {
    bind<String>("SauceNaoHost") with provider { "https://saucenao.com" }
    bind<HttpClient>() with provider {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json.nonstrict).apply {
                    setMapper(SauceNaoResponse::class, SauceNaoResponse.serializer())
                }
            }
            install(HttpCallValidator)
        }
    }
    bind<SauceNaoApi>("SauceNaoApi") with provider {
        val client by kodein.instance<HttpClient>()
        val host by kodein.instance<String>("SauceNaoHost")
        SauceNaoApiService(client = client, host = host)
    }
}