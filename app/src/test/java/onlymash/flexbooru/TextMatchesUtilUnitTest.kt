/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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