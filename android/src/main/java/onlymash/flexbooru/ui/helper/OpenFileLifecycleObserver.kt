/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui.helper

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class OpenFileLifecycleObserver(
    private val registry: ActivityResultRegistry,
    private val handleUriCallback: (Uri) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var getFile: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        getFile = registry.register("open_dcoument_file", ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                handleUriCallback.invoke(uri)
            }
        }
    }

    fun openDocument(mimeTypes: Array<String>) {
        getFile.launch(mimeTypes)
    }

    fun openDocument(mimeType: String) {
        openDocument(arrayOf(mimeType))
    }
}