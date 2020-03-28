package onlymash.flexbooru.data.utils

import onlymash.flexbooru.common.Values.DATE_PATTERN_DAN as PATTERN_DAN
import onlymash.flexbooru.common.Values.DATE_PATTERN_GEL as PATTERN_GEL
import onlymash.flexbooru.extension.formatDate
import java.text.SimpleDateFormat
import java.util.*

fun String.toSafeUrl(scheme: String, host: String): String {
    var url = this
    if (contains("""\/""")) {
        url = url.replace("""\/""", "/")
    }
    return when {
        url.startsWith("http") -> url
        url.startsWith("//") -> "$scheme:$url"
        url.startsWith("/") -> "$scheme://$host$url"
        else -> url
    }
}

fun String.formatDateDan(): CharSequence? =
    SimpleDateFormat(PATTERN_DAN, Locale.ENGLISH)
        .parse(this)?.time?.formatDate()

fun String.formatDateGel(): CharSequence? =
    SimpleDateFormat(PATTERN_GEL, Locale.ENGLISH)
        .parse(this)?.time?.formatDate()
