package onlymash.flexbooru.util

import android.util.Log
import onlymash.flexbooru.BuildConfig

object Logger {
    fun d(tag: String, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg ?: "null")
        }
    }
    fun i(tag: String, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg ?: "null")
        }
    }
    fun w(tag: String, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg ?: "null")
        }
    }
    fun e(tag: String, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg ?: "null")
        }
    }
}