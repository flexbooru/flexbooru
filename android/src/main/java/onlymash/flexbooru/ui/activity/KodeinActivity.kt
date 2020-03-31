package onlymash.flexbooru.ui.activity

import androidx.appcompat.app.AppCompatActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

abstract class KodeinActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()

}