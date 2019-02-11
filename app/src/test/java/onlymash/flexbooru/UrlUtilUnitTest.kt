package onlymash.flexbooru

import onlymash.flexbooru.util.UrlUtil
import org.junit.Assert
import org.junit.Test

class UrlUtilUnitTest {

    private val url = "https://sakugabooru.com/data/9b06e769b6e33bcf379d0c0c3586853a.mp4"
    @Test
    fun verifySuffix() {
        Assert.assertEquals("mp4",
            UrlUtil.parseSuffix(url))
    }

    @Test
    fun verifyMp4() {
        Assert.assertTrue(UrlUtil.isMP4(url))
    }

}