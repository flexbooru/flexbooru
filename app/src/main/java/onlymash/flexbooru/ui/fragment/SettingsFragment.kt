package onlymash.flexbooru.ui.fragment

import android.os.Bundle
import moe.shizuku.preference.PreferenceFragment
import onlymash.flexbooru.R

class SettingsFragment : PreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
    }
    override fun onCreateItemDecoration(): DividerDecoration? {
        return CategoryDivideDividerDecoration()
    }
}