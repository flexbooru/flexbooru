package onlymash.flexbooru.saucenao.di

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpCallValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import onlymash.flexbooru.saucenao.api.SauceNaoApi
import onlymash.flexbooru.saucenao.api.SauceNaoApiService
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider

@UnstableDefault
val kodeinSauceNao = Kodein {
    bind<String>("SauceNaoBaseUrl") with provider { "https://saucenao.com" }
    bind<HttpClient>() with provider {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json(
                        JsonConfiguration(
                            isLenient = true,
                            ignoreUnknownKeys = true,
                            serializeSpecialFloatingPointValues = true,
                            useArrayPolymorphism = true
                        )
                    )
                )
            }
            install(HttpCallValidator)
        }
    }
    bind<SauceNaoApi>("SauceNaoApi") with provider {
        val client by kodein.instance<HttpClient>()
        val baseUrl by kodein.instance<String>("SauceNaoBaseUrl")
        SauceNaoApiService(client = client, baseUrl = baseUrl)
    }
}