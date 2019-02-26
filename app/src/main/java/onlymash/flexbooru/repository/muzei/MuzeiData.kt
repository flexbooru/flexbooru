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

package onlymash.flexbooru.repository.muzei

import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import onlymash.flexbooru.database.dao.MuzeiDao
import onlymash.flexbooru.entity.Muzei
import java.util.concurrent.Executor

/**
 * Muzei data source
 * */
class MuzeiData(private val muzeiDao: MuzeiDao,
                private val ioExecutor: Executor) : MuzeiRepository {

    override val muzeiOutcome: MediatorLiveData<MutableList<Muzei>> = MediatorLiveData()

    private val handler = Handler()

    override fun loadMuzei(booruUid: Long) {
        ioExecutor.execute {
            muzeiOutcome.addSource(muzeiDao.getMuzeiByBooruUidLiveData(booruUid)) {
                handler.post {
                    muzeiOutcome.postValue(it)
                }
            }
        }
    }
}