/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import net.glxn.qrgen.android.QRCode
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R

//https://github.com/shadowsocks/shadowsocks-android/blob/master/mobile/src/main/java/com/github/shadowsocks/ProfilesFragment.kt
class QRCodeDialog() : DialogFragment()  {

    @SuppressLint("ValidFragment")
    constructor(url: String) : this() {
        arguments = bundleOf(Pair(Constants.URL_KEY, url))
    }

    private val url get() = arguments?.getString(Constants.URL_KEY)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val image = ImageView(context)
        image.layoutParams = LinearLayout.LayoutParams(-1, -1)
        val size = resources.getDimensionPixelSize(R.dimen.qr_code_size)
        image.setImageBitmap((QRCode.from(url).withSize(size, size) as QRCode).bitmap())
        return image
    }
}