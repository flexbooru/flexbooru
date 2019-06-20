package onlymash.flexbooru.common

interface BaseView {
    var isUpdating: Boolean
    fun showError(error: Throwable)
}