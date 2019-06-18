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

package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.Intent
import android.provider.DocumentsContract
import androidx.appcompat.app.AppCompatActivity
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.extension.toDecodedString
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

abstract class BaseActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )
            Settings.downloadDirPath = uri.toDecodedString()
            Settings.downloadDirPathTreeId = DocumentsContract.getTreeDocumentId(uri)
            Settings.downloadDirPathAuthority = uri.authority
        }
    }
}