package onlymash.flexbooru.util

import android.os.Build
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.Constants

import java.util.Locale

/**
 * Constructs a User-Agent string.
 */
object UserAgent {

    /**
     * Constructs a User-Agent string including application name and version,
     * system build version, model and Id, and Cronet version.
     * @return User-Agent string.
     */
    fun get(): String {

        val builder = StringBuilder().apply {
            append("Mozilla/5.0 (Linux; U; Android ")
            append(Build.VERSION.RELEASE)
            append("; ")
            append(Locale.getDefault().toString())
        }

        val model = Build.MODEL
        if (model.isNotEmpty()) {
            builder.apply {
                append("; ")
                append(model)
            }
        }

        val id = Build.ID
        if (id.isNotEmpty()) {
            builder.apply {
                append("; Build/")
                append(id)
            }
        }

        builder.apply {
            append("; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 ")
            append(BuildConfig.APPLICATION_ID)
            append("/")
            append(BuildConfig.VERSION_NAME)
            append(" Mobile Safari/537.36")
        }

        return builder.toString()
    }
}