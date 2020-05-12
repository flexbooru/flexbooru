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

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.activity.invoke
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

private const val EXTERNAL_STORAGE_PRIMARY_EMULATED_ROOT_ID = "primary"
private const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"

class StorageFolderLifecycleObserver(
    private val registry: ActivityResultRegistry,
    private val handleUriCallback: (Uri) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var getDocumentTree : ActivityResultLauncher<Uri?>

    override fun onCreate(owner: LifecycleOwner) {
        getDocumentTree = registry.register("open_document_tree", ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                handleUriCallback.invoke(uri)
            }
        }
    }

    fun openDocumentTree(context: Context) {
        getDocumentTree.invoke(context.getRootUri())
    }

    private fun Context.getRootUri(): Uri? {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val sv = getSystemService(StorageManager::class.java)?.primaryStorageVolume ?: return null
                val rootId = if (sv.isEmulated) {
                    EXTERNAL_STORAGE_PRIMARY_EMULATED_ROOT_ID
                } else {
                    sv.uuid
                } ?: return null
                DocumentsContract.buildRootUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, rootId)
            }
            else -> null
        }
    }
}