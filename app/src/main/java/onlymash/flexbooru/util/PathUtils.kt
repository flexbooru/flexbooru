package onlymash.flexbooru.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object PathUtils {

    fun getPath(context: Context, uri: Uri): String? {
        // DocumentProvider
        when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                when {
                    uri.isExternalStorageDocument() -> {
                        when {
                            type.equals("primary", ignoreCase = true) -> {
                                return if (split.size > 1) {
                                    Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                                } else {
                                    Environment.getExternalStorageDirectory().absolutePath
                                }
                            }
                            type.equals("home", ignoreCase = true) -> {
                                return if (split.size > 1) {
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/" + split[1]
                                } else {
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
                                }
                            }
                            else -> {
                                val storageDefinition = if (Environment.isExternalStorageRemovable()) {
                                    "EXTERNAL_STORAGE"
                                } else {
                                    "SECONDARY_STORAGE"
                                }
                                val path = System.getenv(storageDefinition) ?: return null
                                return  path + "/" + split[1]
                            }
                        }

                    }
                    uri.isDownloadsDocument() -> {// DownloadsProvider
                        return if (split.size > 1) {
                            split[1]
                        } else {
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                        }
                    }
                    uri.isMediaDocument() -> {
                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])

                        if (contentUri == null) return null

                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }
            "content".equals(uri.scheme, ignoreCase = true) -> // MediaStore (and general)

                // Return the remote address

                return if (uri.isGooglePhotosUri())
                    uri.lastPathSegment
                else
                    getDataColumn(context, uri, null, null)
            "file".equals(uri.scheme, ignoreCase = true) -> return uri.path
        }

        return null
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun Uri.isExternalStorageDocument(): Boolean =
        "com.android.externalstorage.documents" == authority

    private fun Uri.isDownloadsDocument(): Boolean =
        "com.android.providers.downloads.documents" == authority

    private fun Uri.isMediaDocument(): Boolean =
        "com.android.providers.media.documents" == authority

    private fun Uri.isGooglePhotosUri(): Boolean =
        "com.google.android.apps.photos.content" == authority
}