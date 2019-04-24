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

package onlymash.flexbooru

import android.os.Build

object BuildInfo {
    /**
     * Checks if the device is running on a pre-release version of Android Q or newer.
     *
     *
     * **Note:** This method will return `false` on devices running release
     * versions of Android. When Android Q is finalized for release, this method will be deprecated
     * and all calls should be replaced with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q`.
     *
     * @return `true` if Q APIs are available for use, `false` otherwise
     */
    fun isAtLeastQ(): Boolean =
        Build.VERSION.CODENAME.length == 1 && Build.VERSION.CODENAME[0] >= 'Q'
                && Build.VERSION.CODENAME[0] <= 'Z'

    /**
     * Checks if the application targets pre-release SDK Q
     */
    fun targetsAtLeastQ(): Boolean =
        isAtLeastQ() && App.app.applicationContext.applicationInfo.targetSdkVersion == Build.VERSION_CODES.CUR_DEVELOPMENT
}