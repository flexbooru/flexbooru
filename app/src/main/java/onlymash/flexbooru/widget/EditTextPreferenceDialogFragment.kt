package onlymash.flexbooru.widget

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.annotation.RestrictTo
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.PreferenceDialogFragment

import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.util.TextMatchesUtil

class EditTextPreferenceDialogFragment : PreferenceDialogFragment(), TextWatcher {

    private var key = ""
    private var beforeText = ""

    private var editText: EditText? = null

    private val editTextPreference: EditTextPreference
        get() = preference as EditTextPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            key = it.getString(PreferenceDialogFragment.ARG_KEY, "")
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        editText = view.findViewById(android.R.id.edit)

        if (editText == null) {
            throw IllegalStateException("Dialog view must contain an EditText with id" + " @android:id/edit")
        }

        editText!!.setSingleLine(editTextPreference.isSingleLine)
        editText!!.setSelectAllOnFocus(editTextPreference.isSelectAllOnFocus)

        if (editTextPreference.inputType != InputType.TYPE_CLASS_TEXT)
            editText!!.inputType = editTextPreference.inputType

        editText!!.hint = editTextPreference.hint
        editText!!.setText(editTextPreference.text)

        editText!!.requestFocus()

        editText!!.post {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        if (editTextPreference.isCommitOnEnter) {
            editText!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
                    onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                    dismiss()
                    return@OnEditorActionListener true
                }
                false
            })
        }
        editText!!.addTextChangedListener(this)
    }

    @RestrictTo(LIBRARY_GROUP)
    override fun needInputMethod(): Boolean {
        // We want the input method to show, if possible, when dialog is displayed
        return true
    }
    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value = editText!!.text.toString()
            if (key == Constants.BOORU_CONFIG_HASH_SALT_KEY && !value.contains(Constants.HASH_SALT_CONTAINED)) {
                Toast.makeText(this.requireContext(), R.string.booru_config_hash_salt_must_contain_yp, Toast.LENGTH_LONG).show()
            } else {
                if (editTextPreference.callChangeListener(value)) {
                    editTextPreference.text = value
                }
            }
        }
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (s.isNullOrEmpty()) return
        beforeText = s.toString()
    }
    override fun afterTextChanged(s: Editable?) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.isNullOrEmpty()) return
        val afterText = s.toString()
        var isValid = true
        when (key) {
            Constants.BOORU_CONFIG_HOST_KEY -> {
                if (afterText.isNotEmpty()) isValid = TextMatchesUtil.isHost(afterText)
            }
        }
        if (!isValid) {
            if (beforeText.isNotEmpty() && TextMatchesUtil.isHost(beforeText))
                editText!!.setText(beforeText)
            else
                editText!!.setText("")
        }
    }
}
