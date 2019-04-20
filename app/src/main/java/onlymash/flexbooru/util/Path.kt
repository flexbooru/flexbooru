package onlymash.flexbooru.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import java.io.File

const val APP_DIR_NAME = "Flexbooru"

private fun getDefaultBasePath(): String =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath

fun getBasePath(): String {
    val path = Settings.instance().downloadDirPath
    if (path.isNullOrEmpty()) {
        return getDefaultBasePath()
    }
    return path
}
private fun closeQuietly(closeable: AutoCloseable?) {
    if (closeable == null) return
    try {
        closeable.close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (_: Exception) { }
}

fun String.safeStringToUri(subDirName: String? = null): Uri {
    val uri = Uri.parse(this)
    val type = DocumentsContract.getTreeDocumentId(uri).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    val start = lastIndexOf(":") + 1
    var path = ""
    if (start in 1 until length) {
        path = substring(start)
    }
    var docId = "$type:$path"
    if (subDirName != null && subDirName.isNotEmpty()) {
        if (path.isEmpty()) {
            docId += subDirName
        } else {
            docId = "$docId/$subDirName"
        }
    }
    return DocumentsContract.buildDocumentUriUsingTree(uri, docId)
}

fun Activity.getAppDirUri(): Uri? {
    val basePath = getBasePath()
    if (basePath.startsWith(ContentResolver.SCHEME_CONTENT)) {
        val pUri = basePath.safeStringToUri()
        val uri = basePath.safeStringToUri(APP_DIR_NAME)
        val pDoc = DocumentFile.fromSingleUri(this, pUri) ?: return null
        if (!pDoc.canWrite()) {
            Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
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
            return null
        }
        val doc = DocumentFile.fromSingleUri(this, uri) ?: return null
        if (!doc.exists()) {
            DocumentsContract.createDocument(
                contentResolver,
                pUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                APP_DIR_NAME)
        }
        return uri
    } else {
        try {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), Constants.REQUEST_CODE_OPEN_DIRECTORY)
        } catch (_: ActivityNotFoundException) {}
        return null
    }
}

fun Activity.getSaveUri(fileName: String): Uri? {
    val appUri = getAppDirUri() ?: return null
    return if (appUri.scheme == ContentResolver.SCHEME_CONTENT) {
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
        DocumentsContract.createDocument(
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
    return if (appUri.scheme == ContentResolver.SCHEME_CONTENT) {
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
        DocumentsContract.createDocument(
            contentResolver,
            downloadDirUri,
            fileName.getMimeType(),
            fileName)
    } else {
        return null
    }
}