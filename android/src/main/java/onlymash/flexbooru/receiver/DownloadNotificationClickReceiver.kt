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

package onlymash.flexbooru.receiver

import android.content.*
import android.net.Uri
import androidx.work.Data
import onlymash.flexbooru.R
import onlymash.flexbooru.extension.getMimeType
import onlymash.flexbooru.worker.DownloadWorker

class DownloadNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        val uri = intent.data
        val data = intent.getByteArrayExtra(DownloadWorker.INPUT_DATA_KEY)
        when {
            uri != null -> openUri(context, uri)
            data != null -> DownloadWorker.runWork(Data.fromByteArray(data))
        }
    }

    private fun openUri(context: Context, uri: Uri) {
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
            return
        }
        val newIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            setDataAndType(uri, uri.toString().getMimeType())
        }
        try {
            context.startActivity(
                Intent.createChooser(
                    newIntent,
                    context.getString(R.string.share_via)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {

        } catch (_: RuntimeException) {

        }
    }
}