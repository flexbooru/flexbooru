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