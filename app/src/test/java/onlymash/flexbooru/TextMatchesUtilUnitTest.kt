package onlymash.flexbooru

import onlymash.flexbooru.util.TextMatchesUtil
import org.junit.Assert
import org.junit.Test

class TextMatchesUtilUnitTest {

    @Test
    fun isValidHost() {
        Assert.assertTrue(TextMatchesUtil.isHost("fiepi.me"))
    }

    @Test
    fun isInvalidHost() {
        Assert.assertFalse(TextMatchesUtil.isHost("https://fiepi.me"))
    }

    @Test
    fun isValidNumber() {
        Assert.assertTrue(TextMatchesUtil.isNumber("101"))
    }

    @Test
    fun isInvalidNumber() {
        Assert.assertFalse(TextMatchesUtil.isNumber("abc"))
    }
}