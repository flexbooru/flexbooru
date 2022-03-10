/*
 * Copyright (C) 2019 by onlymash <fiepi.dev@gmail.com>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru

import onlymash.flexbooru.extension.fileExt
import onlymash.flexbooru.extension.isHost
import onlymash.flexbooru.extension.isImage
import onlymash.flexbooru.extension.isNumber
import org.junit.Assert
import org.junit.Test

class TextExtUnitTest {

    private val url = "https://fiepi.me/ooxx.mp4"
    private val url2 = "https://fiepi.me/ooxx.jpg"

    @Test
    fun isValidHost() {
        Assert.assertTrue("fiepi.me".isHost())
    }

    @Test
    fun isInvalidHost() {
        Assert.assertFalse("https://fiepi.me".isHost())
    }

    @Test
    fun isValidNumber() {
        Assert.assertTrue("101".isNumber())
    }

    @Test
    fun isInvalidNumber() {
        Assert.assertFalse("abc".isNumber())
    }

    @Test
    fun verifySuffix() {
        Assert.assertEquals("mp4", url.fileExt())
    }

    @Test
    fun verifyImage() {
        Assert.assertFalse(url.isImage())
        Assert.assertTrue(url2.isImage())
    }
}