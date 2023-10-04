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

package onlymash.flexbooru.extension

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.base.PathActivity
import java.util.*

fun PathActivity.getSaveUri(fileName: String): Uri? = getFileUri("save", fileName)

fun PathActivity.getPoolUri(fileName: String): Uri? = getFileUri("pools", fileName)

fun PathActivity.getDownloadUri(host: String, fileName: String): Uri? = getFileUri(host, fileName)

fun ContentResolver.getFileUriByDocId(docId: String): Uri? {
    val treeUri = getTreeUri() ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
}

fun ContentResolver.getTreeUri(): Uri? {
    val permissions = persistedUriPermissions
    val index = permissions.indexOfFirst { permission ->
        permission.isReadPermission && permission.isWritePermission
    }
    if (index < 0) {
        return null
    }
    return permissions[index].uri
}

private fun PathActivity.getFileUri(dirName: String, fileName: String): Uri? {
    val treeUri = contentResolver.getTreeUri()
    if (treeUri == null) {
        pickDir()
        return null
    }
    val treeDir = DocumentFile.fromTreeUri(this, treeUri)
    if (treeDir == null || !treeDir.exists() || treeDir.isFile ||
        !treeDir.canRead() || !treeDir.canWrite()) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        pickDir()
        return null
    }
    val treeId = DocumentsContract.getTreeDocumentId(treeUri)
    val dirId = getDocumentFileId(treeId, dirName)
    val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirId) ?: return null
    val dir = DocumentFile.fromSingleUri(this, dirUri)
    var tmpUri: Uri? = null
    try {
        if (dir == null || !dir.exists()) {
            tmpUri = treeDir.createDirectory(dirName)?.uri ?: return null
        } else if (dir.isFile) {
            dir.delete()
            tmpUri = treeDir.createDirectory(dirName)?.uri ?: return null
        }
    } catch (_: Exception) {
        return null
    }
    if (tmpUri != null && tmpUri != dirUri) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        pickDir()
        return null
    }
    val fileId= getDocumentFileId(dirId, fileName)
    var fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId)
    var file = DocumentFile.fromSingleUri(this, fileUri)
    try {
        if (file == null || !file.exists()) {
            fileUri = DocumentsContract.createDocument(
                contentResolver,
                dirUri,
                fileName.getMimeType(),
                fileName
            ) ?: return null
        } else if (file.isDirectory) {
            file.delete()
            fileUri = DocumentsContract.createDocument(
                contentResolver,
                dirUri,
                fileName.getMimeType(),
                fileName
            ) ?: return null
        }
    } catch (_: Exception) {
        return null
    }
    file = DocumentFile.fromSingleUri(this, fileUri)
    if (file == null || !file.exists() || !file.canWrite()) {
        return null
    }
    return fileUri
}

private fun getDocumentFileId(prentId: String, fileName: String): String {
    return if (prentId.endsWith(":")) {
        prentId + fileName
    } else {
        "$prentId/$fileName"
    }
}

fun String.getMimeType(): String {
    var extension = fileExt()
    // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
    extension = extension.lowercase(Locale.getDefault())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
}

fun String.fileExt(): String {
    val start = lastIndexOf('.') + 1
    val end = indexOfFirst { it == '?' }
    if (start == 0) {
        return ""
    }
    return if (end > start) {
        substring(start, end)
    } else {
        substring(start)
    }
}

fun String.fileName(): String {
    val start = lastIndexOf('/') + 1
    val end = indexOfFirst { it == '?' }
    val encodeFileName = if (end > start) {
        substring(start, end)
    } else {
        substring(start)
    }
    return encodeFileName.toDecodedString()
        .replace("?", "")
        .replace("!", "")
        .replace(":", "_")
        .replace("\"","_")
}

fun String.isImage(): Boolean {
    return isGifImage() || isStillImage()
}

fun String.isVideo(): Boolean {
    val ext = fileExt()
    return ext.equals("mp4", ignoreCase = true) ||
            ext.equals("webm", ignoreCase = true) ||
            ext.equals("mkv", ignoreCase = true) ||
            ext.equals("avi", ignoreCase = true) ||
            ext.equals("mov", ignoreCase = true) ||
            ext.equals("wmv", ignoreCase = true) ||
            ext.equals("flv", ignoreCase = true)
}

fun String.isStillImage(): Boolean {
    val ext = fileExt()
    return ext.equals("jpg", ignoreCase = true) ||
            ext.equals("png", ignoreCase = true) ||
            ext.equals("jpeg", ignoreCase = true) ||
            ext.equals("webp", ignoreCase = true)
}

fun String.isGifImage(): Boolean = fileExt().equals("gif", ignoreCase = true)

fun Uri.toDecodedString(): String = toString().toDecodedString()

fun String.toDecodedString(): String = Uri.decode(this)