package onlymash.flexbooru.tracemoe.presentation

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import onlymash.flexbooru.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.common.CoroutinePresenter
import onlymash.flexbooru.tracemoe.di.kodeinTraceMoe
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

@UnstableDefault
class TraceMoePresenter(
    uiContext: CoroutineContext,
    val view: TraceMoeView
) : CoroutinePresenter(uiContext, view), TraceMoeActions {

    val api: TraceMoeApi by kodeinTraceMoe.instance("TraceMoeApi")

    override fun onRequestData(base64Image: String) {

        view.isUpdating = true

        GlobalScope.launch(coroutineContext) {
            val response = api.fetch(base64Image)
            view.onUpdate(response)
        }.invokeOnCompletion {
            view.isUpdating = false
        }
    }
}