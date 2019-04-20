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

package onlymash.flexbooru.widget.preference

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.PreferenceDialogFragment
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.fragment.BooruConfigFragment
import onlymash.flexbooru.util.TextMatchesUtil

class EditTextPreferenceDialogFragment : PreferenceDialogFragment(), TextWatcher {

    private var key = ""
    private var beforeText = ""

    private lateinit var editText: EditText

    private val editTextPreference: EditTextPreference
        get() = preference as EditTextPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            key = it.getString(ARG_KEY, "")
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        editText = view.findViewById(android.R.id.edit) ?: return

        editText.apply {
            if (editTextPreference.inputType != InputType.TYPE_CLASS_TEXT) {
                inputType = editTextPreference.inputType
            }
            setSingleLine(editTextPreference.isSingleLine)
            setSelectAllOnFocus(editTextPreference.isSelectAllOnFocus)
            hint = editTextPreference.hint
            setText(editTextPreference.text)
            requestFocus()
            post {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
            if (editTextPreference.isCommitOnEnter) {
                setOnEditorActionListener(TextView.OnEditorActionListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
                        onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                        dismiss()
                        return@OnEditorActionListener true
                    }
                    false
                })
            }
            addTextChangedListener(this@EditTextPreferenceDialogFragment)
        }
    }

    @RestrictTo(LIBRARY_GROUP)
    override fun needInputMethod(): Boolean {
        // We want the input method to show, if possible, when dialog is displayed
        return true
    }
    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value = editText.text.toString()
            if (key == BooruConfigFragment.BOORU_CONFIG_HASH_SALT_KEY && !value.contains(Constants.HASH_SALT_CONTAINED, false)) {
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
            BooruConfigFragment.BOORU_CONFIG_HOST_KEY -> {
                if (afterText.isNotEmpty()) isValid = TextMatchesUtil.isHost(afterText)
            }
        }
        if (!isValid) {
            if (beforeText.isNotEmpty() && TextMatchesUtil.isHost(beforeText))
                editText.setText(beforeText)
            else
                editText.setText("")
        }
    }
}
