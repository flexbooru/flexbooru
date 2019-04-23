package onlymash.flexbooru.widget

import androidx.preference.ListPreference
import androidx.preference.Preference

class ListPreferenceSummaryProvider : Preference.SummaryProvider<ListPreference> {
    override fun provideSummary(preference: ListPreference?): CharSequence {
        return preference?.entry ?: ""
    }
    companion object {
        private var instance : ListPreferenceSummaryProvider? = null
        fun getInstance(): ListPreferenceSummaryProvider {
            if (instance == null) {
                instance = ListPreferenceSummaryProvider()
            }
            return instance!!
        }
    }
}