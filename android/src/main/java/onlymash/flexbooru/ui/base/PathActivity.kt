/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.ui.base

import android.content.Intent
import android.os.Bundle
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.extension.toDecodedString
import onlymash.flexbooru.ui.helper.StorageFolderLifecycleObserver

abstract class PathActivity : BaseActivity() {

    private lateinit var observer: StorageFolderLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observer = StorageFolderLifecycleObserver(activityResultRegistry) { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.apply {
                persistedUriPermissions.forEach { permission ->
                    if (permission.isWritePermission && permission.uri != uri) {
                        releasePersistableUriPermission(permission.uri, flags)
                    }
                }
                takePersistableUriPermission(uri, flags)
            }
            Settings.downloadDirPath = uri.toDecodedString()
        }
        lifecycle.addObserver(observer)
    }

    fun pickDir() {
        observer.openDocumentTree(this)
    }
}