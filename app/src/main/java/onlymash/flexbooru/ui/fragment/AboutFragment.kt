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

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import moe.shizuku.preference.Preference
import moe.shizuku.preference.PreferenceFragment
import onlymash.flexbooru.App
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.R
import onlymash.flexbooru.util.launchUrl

class AboutFragment : PreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)
        findPreference("about_app_version")?.summary = BuildConfig.VERSION_NAME
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
    }
    override fun onCreateItemDecoration(): DividerDecoration? {
        return CategoryDivideDividerDecoration()
    }
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "about_author_website" -> {
                requireContext().launchUrl("https://blog.fiepi.com")
            }
            "about_author_email" -> {
                val email = "mailto:im@fiepi.me"
                requireContext().startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_feedback_github" -> {
                requireContext().launchUrl("https://github.com/flexbooru/flexbooru/issues")
            }
            "about_feedback_telegram" -> {
                requireContext().launchUrl("https://t.me/Flexbooru")
            }
            "about_feedback_email" -> {
                val email = "mailto:feedback@fiepi.me"
                requireContext().startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_donation_paypal" -> {
                requireContext().launchUrl("https://www.paypal.me/fiepi")
            }
            "about_donation_alipay" -> {
                val text = "im@fiepi.com"
                App.app.clipboard.primaryClip = ClipData.newPlainText("alipay", text)
                view?.let { Snackbar.make(it, getString(R.string.snackbar_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_donation_btc" -> {
                val text = "bc1qxanfk3hc853787a9ctm28x9ff0pvcyy6vpmgpz"
                App.app.clipboard.primaryClip = ClipData.newPlainText("btc", text)
                view?.let { Snackbar.make(it, getString(R.string.snackbar_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_app_rate" -> {
                try {
                    requireContext().startActivity(Intent.createChooser(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().applicationContext.packageName}")),
                        getString(R.string.share_via)))
                } catch (_: ActivityNotFoundException) { }
            }
            "about_app_translation" -> {
                requireContext().launchUrl("https://onlymash.oneskyapp.com/collaboration/project?id=331005")
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}