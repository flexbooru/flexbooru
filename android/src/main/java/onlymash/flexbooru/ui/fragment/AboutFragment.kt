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

package onlymash.flexbooru.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.R
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.extension.openAppInMarket
import onlymash.flexbooru.ui.CopyrightActivity


class AboutFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)
        findPreference<Preference>("about_app_version")?.summary = BuildConfig.VERSION_NAME
    }
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "about_author_website" -> {
                context?.launchUrl("https://blog.fiepi.com")
            }
            "about_author_email" -> {
                val email = "mailto:im@fiepi.me"
                context?.startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_feedback_github" -> {
                context?.launchUrl("https://github.com/flexbooru/flexbooru/issues")
            }
            "about_feedback_telegram" -> {
                context?.launchUrl("https://t.me/Flexbooru")
            }
            "about_feedback_email" -> {
                val email = "mailto:feedback@fiepi.me"
                context?.startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_donation_paypal" -> {
                context?.launchUrl("https://www.paypal.me/fiepi")
            }
            "about_donation_alipay" -> {
                val text = "im@fiepi.com"
                context?.copyText(text)
                view?.let { Snackbar.make(it, getString(R.string.snackbar_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_donation_btc" -> {
                val text = "bc1qxanfk3hc853787a9ctm28x9ff0pvcyy6vpmgpz"
                context?.copyText(text)
                view?.let { Snackbar.make(it, getString(R.string.snackbar_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_app_rate" -> {
                activity?.run {
                    openAppInMarket(applicationContext.packageName)
                }
            }
            "about_app_translation" -> {
                context?.launchUrl("https://crowdin.com/project/flexbooru")
            }
            "about_licenses" -> {
                context?.run {
                    startActivity(Intent(this, CopyrightActivity::class.java))
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}