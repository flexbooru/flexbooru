package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import com.takisoft.preferencex.SimpleMenuPreference

class SimpleMenuPreference : SimpleMenuPreference {
    constructor(context: Context?) :
            super(context)
    constructor(context: Context?, attrs: AttributeSet?) :
            super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun getSummary(): CharSequence =
        ListPreferenceSummaryProvider.getInstance().provideSummary(this)
}