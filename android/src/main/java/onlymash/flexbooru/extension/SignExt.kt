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

package onlymash.flexbooru.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun Context.getSignMd5(): String? {
    val paramArrayOfByte = try {
        getSignature().toByteArray()
    } catch (_: NoSuchAlgorithmException) {
        null
    } catch (_: NullPointerException) {
        null
    } ?: return null
    val localMessageDigest = MessageDigest.getInstance("MD5")
    localMessageDigest.update(paramArrayOfByte)
    return toHexString(localMessageDigest.digest())
}

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
private fun Context.getSignature(): Signature {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        if (packageInfo.signingInfo.hasMultipleSigners()) {
            packageInfo.signingInfo.apkContentsSigners[0]
        } else {
            packageInfo.signingInfo.signingCertificateHistory[0]
        }
    } else {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        packageInfo.signatures[0]
    }
}

private fun toHexString(paramArrayOfByte: ByteArray?): String? {
    paramArrayOfByte ?: return null
    val localStringBuilder = StringBuilder(2 * paramArrayOfByte.size)
    var i = 0
    while (true) {
        if (i >= paramArrayOfByte.size) {
            return localStringBuilder.toString()
        }
        var str = (0xFF and paramArrayOfByte[i].toInt()).toString(16)
        if (str.length == 1) {
            str = "0$str"
        }
        localStringBuilder.append(str)
        i++
    }
}