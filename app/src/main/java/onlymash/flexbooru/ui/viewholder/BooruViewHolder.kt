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

package onlymash.flexbooru.ui.viewholder

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.ui.BooruActivity
import onlymash.flexbooru.ui.BooruConfigActivity
import onlymash.flexbooru.ui.fragment.BooruConfigFragment
import onlymash.flexbooru.ui.fragment.QRCodeDialog
import onlymash.flexbooru.widget.AutoCollapseTextView

class BooruViewHolder(itemView: View,
                      private val activity: Activity) : RecyclerView.ViewHolder(itemView) {

    lateinit var booru: Booru

    private val booruName: TextView = itemView.findViewById(R.id.booru_name)
    private val booruShare: AppCompatImageView = itemView.findViewById(R.id.booru_share)
    private val booruEdit: AppCompatImageView = itemView.findViewById(R.id.booru_edit)
    private val booruUrl: AutoCollapseTextView = itemView.findViewById(R.id.booru_url)
    private val booruType: AutoCollapseTextView = itemView.findViewById(R.id.booru_type)

    init {
        booruEdit.setOnClickListener {
            startConfig(booru)
        }
        TooltipCompat.setTooltipText(booruEdit, booruEdit.contentDescription)
        booruShare.setOnClickListener {
            PopupMenu(activity, booruShare).apply {
                menuInflater.inflate(R.menu.booru_share_popup, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_booru_share_qr_code -> {
                            (activity as BooruActivity).supportFragmentManager
                                .beginTransaction()
                                .add(QRCodeDialog(booru.toString()), "")
                                .commitAllowingStateLoss()
                            true
                        }
                        R.id.action_booru_share_clipboard -> {
                            (activity as BooruActivity).clipboard.primaryClip = ClipData.newPlainText(booru.name, booru.toString())
                            true
                        }
                        else -> false
                    }
                }
            }
                .show()
        }
        TooltipCompat.setTooltipText(booruShare, booruShare.contentDescription)
    }
    /**
     * Bind [Booru] data
     * */
    fun bind(booru: Booru) {
        this.booru = booru
        booruName.text = booru.name
        booruUrl.text = String.format("%s://%s", booru.scheme, booru.host)
        when (booru.type) {
            Constants.TYPE_DANBOORU -> booruType.setText(R.string.booru_type_danbooru)
            Constants.TYPE_MOEBOORU -> booruType.setText(R.string.booru_type_moebooru)
            else -> booruType.text = String.format("%s", "Unknown Type")
        }
    }

    private fun startConfig(booru: Booru) {
        BooruConfigFragment.set(booru)
        val intent = Intent(activity, BooruConfigActivity::class.java)
            .putExtra(Constants.EXTRA_BOORU_UID, booru.uid)
        activity.startActivityForResult(intent, Constants.REQUEST_EDIT_CODE)
    }
}