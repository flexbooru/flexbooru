package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class EditTextPreference : EditTextPreference {

    constructor(context: Context?) :
            super(context)
    constructor(context: Context?, attrs: AttributeSet?) :
            super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private val summaryProvider by lazy { EditTextPreferenceSummaryProvider() }

    override fun getSummary(): CharSequence {
        return summaryProvider.provideSummary(this)
    }
}