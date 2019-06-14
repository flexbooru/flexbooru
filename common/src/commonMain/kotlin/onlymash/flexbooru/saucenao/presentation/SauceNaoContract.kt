package onlymash.flexbooru.saucenao.presentation

import onlymash.flexbooru.saucenao.model.SauceNaoResponse


interface SauceNaoView : BaseView {
    var isUpdating: Boolean
    fun onUpdate(data: SauceNaoResponse)
}

interface SauceNaoActions {
    fun onRequestData(imageUrl: String, apiKey: String)
}