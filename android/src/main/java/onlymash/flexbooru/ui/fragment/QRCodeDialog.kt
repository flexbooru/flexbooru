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

package onlymash.flexbooru.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.BOORU_URL
import java.nio.charset.StandardCharsets

//https://github.com/shadowsocks/shadowsocks-android/blob/master/mobile/src/main/java/com/github/shadowsocks/ProfilesFragment.kt
class QRCodeDialog() : DialogFragment()  {

    companion object {
        private val iso88591 = StandardCharsets.ISO_8859_1.newEncoder()
    }

    @SuppressLint("ValidFragment")
    constructor(url: String) : this() {
        arguments = bundleOf(Pair(BOORU_URL, url))
    }

    private val url get() = arguments?.getString(BOORU_URL)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val url = url!!
        val size = resources.getDimensionPixelSize(R.dimen.qr_code_size)
        val hints = mutableMapOf<EncodeHintType, Any>()
        if (!iso88591.canEncode(url)) hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8.name()
        val qrBits = try {
            MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints)
        } catch (_: WriterException) {
            null
        }
        if (qrBits == null) {
            dismiss()
            return null
        }
        return ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(size, size)
            setImageBitmap(Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                for (x in 0 until size) for (y in 0 until size) {
                    setPixel(x, y, if (qrBits.get(x, y)) Color.BLACK else Color.WHITE)
                }
            })
        }
    }
}