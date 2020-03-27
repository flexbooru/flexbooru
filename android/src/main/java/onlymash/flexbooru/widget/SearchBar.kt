package onlymash.flexbooru.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.card.MaterialCardView
import onlymash.flexbooru.R

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr), View.OnClickListener, TextWatcher {

    private val editText: AppCompatEditText
    private val menuView: ActionMenuView
    private val leftButton: ImageButton
    private val title: AppCompatTextView

    private val dividerHeader: View
    private val listView: ListView
    private val listContainer: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this)
        editText = findViewById(R.id.search_edit_text)
        menuView = findViewById(R.id.search_bar_menu_view)
        leftButton = findViewById(R.id.menu_button)
        title = findViewById(R.id.search_title)
        dividerHeader = findViewById(R.id.divider_header)
        listView = findViewById(R.id.list_view)
        listContainer = findViewById(R.id.list_container)
    }

    override fun onClick(v: View?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}