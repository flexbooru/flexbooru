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

//https://github.com/shadowsocks/shadowsocks-android/blob/master/mobile/src/main/java/com/github/shadowsocks/ScannerActivity.kt#L111
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
        finish()
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
        if (Build.VERSION.SDK_INT >= 25) getSystemService<ShortcutManager>()!!.reportShortcutUsed("scan")
        if (try {
                getSystemService<CameraManager>()?.cameraIdList?.isEmpty()
            } catch (_: CameraAccessException) {
                true
            } != false) {
            return
        }
        setContentView(R.layout.activity_scanner)
        toolbar.setTitle(R.string.scaner_scan_qr_code)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        val capture = supportFragmentManager.findFragmentById(R.id.barcode) as BarcodeCapture
        capture.setCustomDetector(detector)
        capture.setRetrieval(this)
    }

    override fun onRetrieved(barcode: Barcode) = runOnUiThread {
        val b = Booru.url2Booru(barcode.rawValue)
        if (b != null) BooruManager.createBooru(b)
        finish()
    }
    override fun onRetrievedMultiple(closetToClick: Barcode?, barcode: MutableList<BarcodeGraphic>?) = check(false)
    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>?) { }
    override fun onRetrievedFailed(reason: String?) {
        Log.e(TAG, reason)
    }
    override fun onPermissionRequestDenied() {
        Toast.makeText(this, R.string.scaner_add_booru_permission_required, Toast.LENGTH_SHORT).show()
    }
}