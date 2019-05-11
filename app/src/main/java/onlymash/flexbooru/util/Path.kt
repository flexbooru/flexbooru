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

package onlymash.flexbooru.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings

fun getFileUri(dirName: String, fileName: String): Uri? {
    val treeId = Settings.downloadDirPathTreeId ?: return null
    val treeUri = DocumentsContract.buildTreeDocumentUri(Settings.downloadDirPathAuthority, treeId) ?: return null
    val docId = if (treeId.endsWith(":")) "$treeId$dirName/$fileName" else "$treeId/$dirName/$fileName"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
}

fun Activity.openDocumentTree() {
    try {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            },
            Constants.REQUEST_CODE_OPEN_DIRECTORY)
    } catch (_: ActivityNotFoundException) {}
}

private fun Activity.getUri(dirName: String, fileName: String): Uri? {
    val basePath = Settings.downloadDirPath
    val treeId = Settings.downloadDirPathTreeId
    val authority = Settings.downloadDirPathAuthority
    if (basePath == null || !basePath.startsWith(ContentResolver.SCHEME_CONTENT) ||
        treeId.isNullOrEmpty() || authority.isNullOrEmpty()) {
        openDocumentTree()
        return null
    }
    val treeUri = DocumentsContract.buildTreeDocumentUri(authority, treeId)
    val treeDir = DocumentFile.fromTreeUri(this, treeUri)
    if (treeDir == null || !treeDir.canWrite()) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        openDocumentTree()
        return null
    }
    val dirDocId: String
    val fileDocId: String
    if (treeId.endsWith(":")) {
        dirDocId = treeId + dirName
        fileDocId = "$treeId$dirName/$fileName"
    } else {
        dirDocId = "$treeId/$dirName"
        fileDocId = "$treeId/$dirName/$fileName"
    }
    val dirDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirDocId)
    val dir = DocumentFile.fromSingleUri(this, dirDocUri) ?: return null
    if (!dir.exists()) {
        treeDir.createFile(DocumentsContract.Document.MIME_TYPE_DIR, dirName)
    }
    val fileDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileDocId)
    val file = DocumentFile.fromSingleUri(this, fileDocUri) ?: return null
    if (!file.exists()) {
        DocumentsContract.createDocument(contentResolver, dirDocUri, fileName.getMimeType(), fileName)
    }
    return fileDocUri
}

fun Activity.getSaveUri(fileName: String): Uri? = getUri("save", fileName)

fun Activity.getDownloadUri(host: String, fileName: String): Uri? = getUri(host, fileName)

fun Activity.getPoolUri(fileName: String): Uri? = getUri("pools", fileName)