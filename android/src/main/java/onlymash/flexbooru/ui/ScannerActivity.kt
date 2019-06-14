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

package onlymash.flexbooru.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ShortcutManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.toolbar.*
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.entity.Booru

//https://github.com/shadowsocks/shadowsocks-android/blob/master/mobile/src/main/java/com/github/shadowsocks/ScannerActivity.kt
class ScannerActivity : BaseActivity(), BarcodeRetriever {
    companion object {
        private const val TAG = "ScannerActivity"
        private const val REQUEST_GOOGLE_API = 4
    }

    private lateinit var detector: BarcodeDetector

    private fun fallback() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=tw.com.quickmark")))
        } catch (_: ActivityNotFoundException) { }
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
        if (!detector.isOperational) {
            val availability = GoogleApiAvailability.getInstance()
            val dialog = availability.getErrorDialog(this, availability.isGooglePlayServicesAvailable(this),
                REQUEST_GOOGLE_API)
            if (dialog == null) {
                Toast.makeText(this, R.string.common_google_play_services_notification_ticker, Toast.LENGTH_SHORT)
                    .show()
                fallback()
            } else {
                dialog.setOnDismissListener { fallback() }
                dialog.show()
            }
            return
        }
        if (Build.VERSION.SDK_INT >= 25) getSystemService<ShortcutManager>()?.reportShortcutUsed("scan")
        if (try {
                getSystemService<CameraManager>()?.cameraIdList?.isEmpty()
            } catch (_: CameraAccessException) {
                true
            } != false) {
            return
        }
        setContentView(R.layout.activity_scanner)
        toolbar.setTitle(R.string.scaner_scan_qr_code)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val capture = supportFragmentManager.findFragmentById(R.id.barcode) as BarcodeCapture
        capture.setCustomDetector(detector)
        capture.setRetrieval(this)
    }

    override fun onStop() {
        super.onStop()
        detector.release()
    }

    override fun onRetrieved(barcode: Barcode) = runOnUiThread {
        Booru.url2Booru(barcode.rawValue)?.let {
            BooruManager.createBooru(it)
        }
        onBackPressed()
    }
    override fun onRetrievedMultiple(closetToClick: Barcode?, barcode: MutableList<BarcodeGraphic>?) = check(false)
    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>?) { }
    override fun onRetrievedFailed(reason: String?) {
        Log.e(TAG, reason.toString())
    }
    override fun onPermissionRequestDenied() {
        Toast.makeText(this, R.string.scaner_add_booru_permission_required, Toast.LENGTH_SHORT).show()
    }
}