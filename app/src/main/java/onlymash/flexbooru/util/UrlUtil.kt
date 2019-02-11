package onlymash.flexbooru.util

import java.util.regex.Pattern

object UrlUtil {

    private val pattern = Pattern.compile("\\S*[?]\\S*")

    /**
     *
     * @return 链接扩展名
     */
    fun parseSuffix(url: String): String {

        val matcher = pattern.matcher(url)

        val spUrl = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val len = spUrl.size
        val endUrl = spUrl[len - 1]

        if (matcher.find()) {
            val spEndUrl = endUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return spEndUrl[0].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        }
        return endUrl.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    fun isMP4(url: String): Boolean {
        val suffix = parseSuffix(url)
        return suffix == "mp4" || suffix == "MP4"
    }
}
