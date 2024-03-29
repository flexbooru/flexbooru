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

package onlymash.flexbooru.ui.helper

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class CreateFileLifecycleObserver(
    private val registry: ActivityResultRegistry,
    private val handleUriCallback: (Uri) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var createFile: ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        createFile = registry.register("create_dcoument_file", ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null) {
                handleUriCallback.invoke(uri)
            }
        }
    }

    fun createDocument(filename: String) {
        createFile.launch(filename)
    }
}