package onlymash.flexbooru.saucenao.presentation

import onlymash.flexbooru.common.BaseView
import onlymash.flexbooru.saucenao.model.SauceNaoResponse


interface SauceNaoView : BaseView {
    fun onUpdate(data: SauceNaoResponse)
}

interface SauceNaoActions {
    fun onRequestData(apiKey: String, imageUrl: String)
    fun onRequestData(
        apiKey: String,
        byteArray: ByteArray,
        fileExt: String
    )
}