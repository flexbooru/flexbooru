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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.Executor

class DispatchersIOUnitTest {

    private lateinit var exe1: Executor
    private lateinit var exe2: Executor

    @Before
    fun setup() {
        exe1 = Dispatchers.IO.asExecutor()
        exe2 = Dispatchers.IO.asExecutor()
    }

    @Test
    fun checkExecutor() {
        val code1 = exe1.hashCode()
        val code2 = exe2.hashCode()
        println(code1)
        println(code2)
        assertEquals(code1, code2)
    }
}
