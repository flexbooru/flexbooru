package onlymash.flexbooru.util

object TextMatchesUtil {

    private val REGEX_HOST = "^(?=.{1,255}\$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?\$".toRegex()

    private val REGEX_NUMBER = "-?\\d+(\\.\\d+)?".toRegex()

    fun isHost(host: String): Boolean = host.matches(REGEX_HOST)

    fun isNumber(number: String): Boolean = number.matches(REGEX_NUMBER)

}