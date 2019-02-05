package onlymash.flexbooru.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_booru.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.ui.adapter.BooruAdapter

class BooruActivity : BaseActivity() {

    private val booruAdapter by lazy { BooruAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru)
        initToolbar()
        BooruManager.isNotEmpty()
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val animator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
        booru_list.apply {
            setLayoutManager(layoutManager)
            addItemDecoration(DividerItemDecoration(this@BooruActivity, layoutManager.orientation))
            itemAnimator = animator
            adapter = booruAdapter
        }
        BooruManager.listeners.add(booruAdapter)
    }
    private fun initToolbar(){
        toolbar.setTitle(R.string.title_manage_boorus)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.booru)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_booru_add -> {
                }
                R.id.action_booru_add_qr -> {

                }
                R.id.action_booru_add_clipboard -> {

                }
                R.id.action_booru_add_manual -> {
                    addConfig()
                }
                else -> {
                    // unknown id
                }
            }
            true
        }
    }

    private fun addConfig() {
        BooruConfigFragment.reset()
        startActivity(Intent(this, BooruConfigActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_EDIT_CODE -> {
                val result = data?.getStringExtra(Constants.EXTRA_RESULT_KEY)
                when (result) {
                    Constants.RESULT_DELETE -> {

                    }
                    Constants.RESULT_UPDATE -> {

                    }
                }
            }
            Constants.REQUEST_ADD_CODE -> {
                val result = data?.getStringExtra(Constants.EXTRA_RESULT_KEY)
                if (result == Constants.RESULT_ADD) {

                }
            }
        }
    }

    override fun onDestroy() {
        BooruManager.listeners.remove(booruAdapter)
        super.onDestroy()
    }
}
