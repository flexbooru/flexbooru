package onlymash.flexbooru.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.ui.fragment.*

class NavPagerAdapter(fragmentManager: FragmentManager,
                      private val booru: Booru, private val user: User?) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PostFragment.newInstance(keyword = "", booru = booru, user = user)
            1 -> PopularFragment.newInstance(booru = booru, user = user)
            2 -> PoolFragment.newInstance(keyword = "", booru = booru, user = user)
            3 -> TagFragment()
            else -> ArtistFragment()
        }
    }

    override fun getCount(): Int = 5

}