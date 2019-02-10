package onlymash.flexbooru.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.User
import onlymash.flexbooru.ui.PopularFragment
import onlymash.flexbooru.ui.PostFragment

class NavPagerAdapter(fragmentManager: FragmentManager,
                      private val booru: Booru, private val user: User?) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PostFragment.newInstance(booru = booru, user = user, keyword = "")
            else -> PopularFragment.newInstance(booru = booru, user = user)
        }
    }

    override fun getCount(): Int = 2

}