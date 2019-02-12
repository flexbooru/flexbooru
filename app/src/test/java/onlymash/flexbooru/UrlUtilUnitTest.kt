package onlymash.flexbooru

import onlymash.flexbooru.util.UrlUtil
import org.junit.Assert
import org.junit.Test

class UrlUtilUnitTest {

    private val url = "https://fiepi.me/ooxx.mp4"
    private val url2 = "https://fiepi.me/ooxx.jpg"

    @Test
    fun verifySuffix() {
        Assert.assertEquals("mp4", UrlUtil.parseSuffix(url))
    }

    @Test
    fun verifyMp4() {
        Assert.assertTrue(UrlUtil.isMP4(url))
    }

    @Test
    fun verifyImage() {
        Assert.assertFalse(UrlUtil.isImage(url))
        Assert.assertTrue(UrlUtil.isImage(url2))
    }
}