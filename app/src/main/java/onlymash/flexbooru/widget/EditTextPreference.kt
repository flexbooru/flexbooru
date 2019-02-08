package onlymash.flexbooru.widget

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.PreferenceDialogFragment

class EditTextPreference : EditTextPreference {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun onCreateDialogFragment(key: String?): DialogFragment {
        return EditTextPreferenceDialogFragment().apply {
            arguments = Bundle(1).apply {
                putString(PreferenceDialogFragment.ARG_KEY, key)
            }
        }
    }
}
