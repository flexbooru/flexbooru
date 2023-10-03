/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.databinding.DialogInfoBinding
import onlymash.flexbooru.extension.getScreenHeightPixels
import onlymash.flexbooru.ui.adapter.InfoAdapter
import onlymash.flexbooru.ui.base.BaseBottomSheetDialog


class InfoDialog : BaseBottomSheetDialog() {

    companion object {
        fun create(postId: Int): InfoDialog {
            return InfoDialog().apply {
                arguments = Bundle().apply {
                    putInt(Keys.POST_ID, postId)
                }
            }
        }
    }


    private var _binding: DialogInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>

    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            postId = getInt(Keys.POST_ID, -1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = DialogInfoBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        behavior = dialog.behavior
        behavior.isHideable = true
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

        })
        val height = activity?.getScreenHeightPixels()
        if (height != null) {
            binding.root.layoutParams.height = height
        }
        binding.infoPager.adapter = InfoAdapter(postId, this)
        binding.infoPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.infoPager.children.find { it is RecyclerView }?.let {
                    (it as RecyclerView).isNestedScrollingEnabled = position != 0
                }
            }
        })
        TabLayoutMediator(binding.tabs, binding.infoPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.browse_info) else getString(R.string.browse_tags)
        }.attach()
        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}