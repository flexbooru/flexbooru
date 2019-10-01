package onlymash.flexbooru.saucenao.presentation

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import onlymash.flexbooru.common.CoroutinePresenter
import onlymash.flexbooru.saucenao.api.SauceNaoApi
import onlymash.flexbooru.saucenao.di.kodeinSauceNao
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

@UnstableDefault
class SauceNaoPresenter(
    uiContext: CoroutineContext,
    val view: SauceNaoView
) : CoroutinePresenter(uiContext, view), SauceNaoActions {

    @ExperimentalStdlibApi
    val api: SauceNaoApi by kodeinSauceNao.instance("SauceNaoApi")

    @ExperimentalStdlibApi
    override fun onRequestData(apiKey: String, imageUrl: String) {

        view.isUpdating = true

        GlobalScope.launch(coroutineContext) {
            val response = api.searchByUrl(imageUrl, apiKey)
            view.onUpdate(response)
        }.invokeOnCompletion {
            view.isUpdating = false
        }
    }

    @ExperimentalStdlibApi
    override fun onRequestData(
        apiKey: String,
        byteArray: ByteArray,
        fileExt: String
    ) {

        view.isUpdating = true

        GlobalScope.launch(coroutineContext) {
            val response = api.searchByImage(
                apiKey = apiKey,
                byteArray = byteArray,
                fileExt = fileExt
            )
            view.onUpdate(response)
        }.invokeOnCompletion {
            view.isUpdating = false
        }
    }
}