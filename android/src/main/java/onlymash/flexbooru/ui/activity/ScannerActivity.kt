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

package onlymash.flexbooru.ui.activity

import android.Manifest
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.ui.base.BaseActivity

//https://github.com/shadowsocks/shadowsocks-android/blob/master/mobile/src/main/java/com/github/shadowsocks/ScannerActivity.kt
class ScannerActivity : BaseActivity(), ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().apply {
        setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    }.build())
    private val imageAnalysis by lazy {
        ImageAnalysis.Builder().apply {
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            setBackgroundExecutor(Dispatchers.Default.asExecutor())
        }.build().also { it.setAnalyzer(Dispatchers.Main.immediate.asExecutor(), this) }
    }

   @OptIn(ExperimentalGetImage::class)
   override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return
        lifecycleScope.launch {
            val result = try {
                process { InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees) }.also {
                    if (it) imageAnalysis.clearAnalyzer()
                }
            } catch (_: Exception) {
                return@launch
            } finally {
                image.close()
            }
            if (result) onSupportNavigateUp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 25) {
            getSystemService<ShortcutManager>()?.reportShortcutUsed("scan")
        }
        setContentView(R.layout.activity_scanner)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.scaner_scan_qr_code)
        }
        lifecycle.addObserver(scanner)
        requestCamera.launch(Manifest.permission.CAMERA)
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            lifecycleScope.launch {
                val cameraProvider = try {
                    ProcessCameraProvider.getInstance(this@ScannerActivity).get()
                } catch (_: Exception) {
                    null
                }
                if (cameraProvider != null) {
                    val selector =
                        if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else CameraSelector.DEFAULT_FRONT_CAMERA
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(findViewById<PreviewView>(R.id.barcode).surfaceProvider)
                    try {
                        cameraProvider.bindToLifecycle(
                            this@ScannerActivity,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: IllegalArgumentException) {

                    }
                }
            }
        } else {
            Toast.makeText(
                this,
                R.string.scaner_add_booru_permission_required,
                Toast.LENGTH_SHORT
            ).show()
            onSupportNavigateUp()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend inline fun process(crossinline image: () -> InputImage): Boolean {
        val barcodes = withContext(Dispatchers.Default) { scanner.process(image()).await() }
        var result = false
        barcodes.mapNotNull { it.rawValue }.forEach { url ->
            Booru.url2Booru(url)?.let(BooruManager::createBooru)
            result = true
        }
        return result
    }
}