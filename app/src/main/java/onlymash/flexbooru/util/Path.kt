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

const val APP_DIR_NAME = "Flexbooru"

private fun closeQuietly(closeable: AutoCloseable?) {
    if (closeable == null) return
    try {
        closeable.close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (_: Exception) { }
}

fun String.safeStringToUri(subName: String? = null): Uri {
    val uri = Uri.parse(this)
    val type = DocumentsContract.getTreeDocumentId(uri).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    val start = lastIndexOf(":") + 1
    var path = ""
    if (start in 1 until length) {
        path = substring(start)
    }
    var docId = "$type:$path"
    if (subName != null && subName.isNotEmpty()) {
        if (path.isEmpty()) {
            docId += subName
        } else {
            docId = "$docId/$subName"
        }
    }
    return DocumentsContract.buildDocumentUriUsingTree(uri, docId)
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

fun Activity.getAppDirUri(): Uri? {
    val basePath = Settings.instance().downloadDirPath
    if (basePath == null || !basePath.startsWith(ContentResolver.SCHEME_CONTENT)) {
        openDocumentTree()
        return null
    }
    val baseDocUri = basePath.safeStringToUri()
    val docDir = DocumentFile.fromSingleUri(this, baseDocUri) ?: return null
    if (!docDir.canWrite()) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        try {
            openDocumentTree()
        } catch (_: ActivityNotFoundException) {}
        return null
    }
    val uri = basePath.safeStringToUri(APP_DIR_NAME)
    val appDir = DocumentFile.fromSingleUri(this, uri) ?: return null
    if (!appDir.exists()) {
        DocumentsContract.createDocument(
            contentResolver,
            baseDocUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            APP_DIR_NAME)
    }
    return uri
}

fun Activity.getSaveUri(fileName: String): Uri? {
    val appUri = getAppDirUri() ?: return null
    if (appUri.scheme == ContentResolver.SCHEME_CONTENT) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            appUri,
            DocumentsContract.getDocumentId(appUri))
        val childrenCursor = contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ),
            null,
            null,
            null)
        var uri: Uri? = null
        try {
            if (childrenCursor != null) {
                while (childrenCursor.moveToNext()) {
                    if (childrenCursor.getString(0) == "save") {
                        uri = DocumentsContract.buildDocumentUriUsingTree(childrenUri, childrenCursor.getString(1))
                        break
                    }
                }
            }
        } finally {
            closeQuietly(childrenCursor)
        }
        val saveDirUri = uri ?: DocumentsContract.createDocument(
            contentResolver, appUri,
            DocumentsContract.Document.MIME_TYPE_DIR, "save") ?: appUri
        val fileUri = Uri.decode(saveDirUri.toString()).safeStringToUri(fileName)
        val file = DocumentFile.fromSingleUri(this, fileUri)
        if (file != null && file.exists() && file.isFile) {
            return fileUri
        }
        return DocumentsContract.createDocument(
            contentResolver,
            saveDirUri,
            fileName.getMimeType(),
            fileName)
    } else {
        return null
    }
}

fun Activity.getDownloadUri(host: String, fileName: String): Uri? {
    val appUri = getAppDirUri() ?: return null
    if (appUri.scheme == ContentResolver.SCHEME_CONTENT) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            appUri,
            DocumentsContract.getDocumentId(appUri))
        val childrenCursor = contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ),
            null,
            null,
            null)
        var uri: Uri? = null
        try {
            if (childrenCursor != null) {
                while (childrenCursor.moveToNext()) {
                    if (childrenCursor.getString(0) == host) {
                        uri = DocumentsContract.buildDocumentUriUsingTree(childrenUri, childrenCursor.getString(1))
                        break
                    }
                }
            }
        } finally {
            closeQuietly(childrenCursor)
        }
        val downloadDirUri = uri ?: DocumentsContract.createDocument(
            contentResolver, appUri,
            DocumentsContract.Document.MIME_TYPE_DIR, host) ?: appUri
        val fileUri = Uri.decode(downloadDirUri.toString()).safeStringToUri(fileName)
        val file = DocumentFile.fromSingleUri(this, fileUri)
        if (file != null && file.exists() && file.isFile) {
            return fileUri
        }
        return DocumentsContract.createDocument(
            contentResolver,
            downloadDirUri,
            fileName.getMimeType(),
            fileName)
    } else {
        return null
    }
}