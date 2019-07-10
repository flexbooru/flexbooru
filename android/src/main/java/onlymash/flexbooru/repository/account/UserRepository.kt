/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.repository.account

import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.entity.common.User
import onlymash.flexbooru.extension.NetResult

interface UserRepository {
    suspend fun findUserByName(username: String, booru: Booru): NetResult<User>
    suspend fun findUserById(id: Int, booru: Booru): NetResult<User>
    suspend fun gelLogin(username: String, password: String, booru: Booru): NetResult<User>
}