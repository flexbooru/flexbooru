package onlymash.flexbooru.widget

import androidx.preference.EditTextPreference
import androidx.preference.Preference

class EditTextPreferenceSummaryProvider : Preference.SummaryProvider<EditTextPreference> {
    override fun provideSummary(preference: EditTextPreference?): CharSequence =
            preference?.text ?: ""
}