package onlymash.flexbooru.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import onlymash.flexbooru.Constants
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.ui.PopularFragment
import onlymash.flexbooru.ui.PostFragment

class NavPagerAdapter(fragmentManager: FragmentManager,
                      private val booru: Booru) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PostFragment.newInstance(booru = booru, tags = Constants.EMPTY_STRING_VALUE)
            else -> PopularFragment.newInstance(booru)
        }
    }

    override fun getCount(): Int = 2

}